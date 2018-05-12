package org.jqassistant.contrib.plugin.plantumlrule;

import static java.util.Collections.singletonList;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.impl.AbstractCypherLanguagePlugin;
import com.buschmais.jqassistant.core.shared.asciidoc.AsciidoctorFactory;

import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.cucadiagram.CucaDiagram;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.ILeaf;
import net.sourceforge.plantuml.cucadiagram.Link;
import net.sourceforge.plantuml.png.MetadataTag;
import org.asciidoctor.ast.AbstractBlock;

public class PlantUMLRulePlugin extends AbstractCypherLanguagePlugin {

    private static final Pattern PLANTUML_PATTERN = Pattern.compile("^\\s*(@startuml\\s+.*@enduml)\\s.*", Pattern.DOTALL);

    private static final StatementBuilder STATEMENT_BUILDER = new StatementBuilder();

    @Override
    public Collection<String> getLanguages() {
        return singletonList("plantuml");
    }

    @Override
    public <T extends ExecutableRule<?>> boolean accepts(T executableRule) {
        return AbstractBlock.class.isAssignableFrom(executableRule.getExecutable().getSource().getClass());
    }

    @Override
    public <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> ruleParameters, Severity severity, AnalyzerContext context)
        throws RuleException {
        String diagramSource = getDiagramSource(executableRule);
        SourceStringReader reader = new SourceStringReader(diagramSource);
        List<BlockUml> blocks = reader.getBlocks();
        Diagram diagram = blocks.get(0).getDiagram();
        if (diagram instanceof CucaDiagram) {
            return evaluate((CucaDiagram) diagram, executableRule, ruleParameters, severity, context);
        }
        throw new RuleException("Rule type " + diagram.getClass().getName() + " is not supported.");
    }

    private <T extends ExecutableRule<?>> Result<T> evaluate(CucaDiagram diagram, T executableRule, Map<String, Object> ruleParameters, Severity severity,
                                                             AnalyzerContext context) throws RuleException {
        Map<String, Node> nodes = getNodes(diagram);
        Map<String, Relationship> relationships = getRelationships(diagram, nodes);
        String statement = STATEMENT_BUILDER.create(nodes, relationships);
        context.getLogger().info(executableRule + "\n----\n" + statement + "\n----");
        return execute(statement, executableRule, ruleParameters, severity, context);
    }

    private <T extends ExecutableRule<?>> String getDiagramSource(T executableRule) throws RuleException {
        AbstractBlock abstractBlock = (AbstractBlock) executableRule.getExecutable().getSource();
        File imagesDirectory = getImagesDirectory(abstractBlock);
        String fileName = (String) abstractBlock.getAttr("target");
        File diagramFile = new File(imagesDirectory, fileName);
        if (!diagramFile.exists()) {
            throw new RuleException("Cannot find generated PlantUML diagram " + diagramFile.getAbsolutePath());
        }
        String diagramMetadata;
        try {
            diagramMetadata = new MetadataTag(diagramFile, "plantuml").getData();
        } catch (IOException e) {
            throw new RuleException("Cannot extract metadata from diagram.", e);
        }
        Matcher matcher = PLANTUML_PATTERN.matcher(diagramMetadata);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new RuleException("Cannot read diagram source from metadata, expecting '@startuml ... @enduml', got '" + diagramMetadata + "'.");
    }

    private File getImagesDirectory(AbstractBlock abstractBlock) {
        String imagesDirectory = (String) abstractBlock.getDocument().getAttributes().get(AsciidoctorFactory.ATTRIBUTE_IMAGES_OUT_DIR);
        return new File(imagesDirectory);
    }

    private Map<String, Node> getNodes(CucaDiagram diagram) {
        Map<String, Node> nodes = new LinkedHashMap<>();
        for (ILeaf leaf : diagram.getLeafsvalues()) {
            Node.NodeBuilder nodeBuilder = Node.builder().id(leaf.getUid());
            for (String stereoType : leaf.getStereotype().getMultipleLabels()) {
                String label = trimAndReplaceUnderScore(stereoType);
                if (label.startsWith("+")) {
                    nodeBuilder.mergeLabel(label.substring(1));
                } else {
                    nodeBuilder.matchLabel(label);
                }
            }
            nodeBuilder.entityParameter(getEntityParameter(leaf.getDisplay()));
            Node node = nodeBuilder.build();
            nodes.put(node.getId(), node);
        }
        return nodes;
    }

    private EntityParameter getEntityParameter(Display display) {
        for (CharSequence charSequence : display) {
            EntityParameter entity = EntityParameter.getEntityParameter(charSequence);
            if (entity != null) {
                return entity;
            }
        }
        return EntityParameter.builder().build();
    }

    private String trimAndReplaceUnderScore(String value) {
        return value.trim().replace(' ', '_');
    }

    private Map<String, Relationship> getRelationships(CucaDiagram diagram, Map<String, Node> nodes) throws RuleException {
        LinkedHashMap<String, Relationship> relationships = new LinkedHashMap<>();
        for (Link link : diagram.getLinks()) {
            Relationship.RelationshipBuilder builder = Relationship.builder().id(link.getUid().toLowerCase());
            Display display = link.getLabel();
            String relationType;
            if (display.size() == 1) {
                CharSequence charSequence = display.get(0);
                relationType = trimAndReplaceUnderScore(new StringBuffer(charSequence).toString()).toUpperCase();
            } else {
                throw new RuleException("Expecting a type on relation " + link);
            }
            builder.entityParameter(getEntityParameter(display));
            if (relationType.startsWith("+")) {
                builder.mergeType(relationType.substring(1));
            } else {
                builder.matchType(relationType);
            }
            Node entity1 = nodes.get(link.getEntity1().getUid());
            Node entity2 = nodes.get(link.getEntity2().getUid());
            if (link.isInverted()) {
                builder.from(entity2);
                builder.to(entity1);
            } else {
                builder.from(entity1);
                builder.to(entity2);
            }
            Relationship relationship = builder.build();
            relationships.put(relationship.getId(), relationship);
        }
        return relationships;
    }
}
