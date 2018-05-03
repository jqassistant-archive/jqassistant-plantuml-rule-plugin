package org.jqassistant.contrib.plugin.plantumlrule;

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

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;

public class PlantUMLRulePlugin extends AbstractCypherLanguagePlugin {

    private static final Pattern PLANTUML_PATTERN = Pattern.compile("^\\s*(@startuml\\s+.*@enduml)\\s.*", Pattern.DOTALL);

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

    private <T extends ExecutableRule<?>> Result<T> evaluate(CucaDiagram diagram, T executableRule, Map<String, Object> ruleParameters, Severity severity, AnalyzerContext context) throws RuleException {
        Map<String, Node> nodes = getNodes(diagram);
        Map<String, Relationship> relationships = getRelationships(diagram, nodes);
        String statement = createStatement(nodes, relationships);
        return execute(statement, executableRule, ruleParameters, severity, context);
    }

    private String createStatement(Map<String, Node> nodes, Map<String, Relationship> relationships) {
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder mergeBuilder = new StringBuilder();
        for (Node node : nodes.values()) {
            Set<String> matchLabels = node.getMatchLabels();
            if (!matchLabels.isEmpty()) {
                commaNewLine(matchBuilder);
                indent(matchBuilder);
                matchBuilder.append("(").append(node.getId());
                addNodeLabels(matchBuilder, matchLabels);
                matchBuilder.append(")");
            }
            Set<String> mergeLabels = node.getMergeLabels();
            if (!mergeLabels.isEmpty()) {
                newLine(mergeBuilder);
                mergeBuilder.append("SET");
                newLine(mergeBuilder);
                indent(mergeBuilder);
                mergeBuilder.append(node.getId());
                addNodeLabels(mergeBuilder, mergeLabels);
            }
        }
        for (Relationship relationship : relationships.values()) {
            if (relationship.getMatchType() != null) {
                commaNewLine(matchBuilder);
                indent(matchBuilder);
                addRelationship(relationship, relationship.getMatchType(), matchBuilder);
            }
            if (relationship.getMergeType() != null) {
                newLine(mergeBuilder);
                mergeBuilder.append("MERGE");
                newLine(mergeBuilder);
                indent(mergeBuilder);
                addRelationship(relationship, relationship.getMergeType(), mergeBuilder);
            }
        }
        StringBuilder statement = new StringBuilder();
        statement.append("MATCH");
        newLine(statement);
        statement.append(matchBuilder);
        newLine(statement);
        statement.append(mergeBuilder);
        newLine(statement);
        statement.append("RETURN");
        newLine(statement);
        indent(statement);
        statement.append("*");
        return statement.toString();
    }

    private void addRelationship(Relationship relationship, String type, StringBuilder builder) {
        builder.append('(').append(relationship.getFrom().getId()).append(')');
        builder.append("-[").append(relationship.getId()).append(":").append(type).append("]->");
        builder.append('(').append(relationship.getTo().getId()).append(')');
    }

    private void addNodeLabels(StringBuilder builder, Set<String> labels) {
        for (String label : labels) {
            builder.append(':').append(label);
        }
    }

    private void commaNewLine(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append(',').append("\n");
        }
    }

    private void newLine(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append("\n");
        }
    }

    private void indent(StringBuilder builder) {
        builder.append("  ");
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
            Node node = nodeBuilder.build();
            nodes.put(node.getId(), node);
            for (CharSequence charSequence : leaf.getDisplay()) {
                // Extract attributes
            }
        }
        return nodes;
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
                relationType = trimAndReplaceUnderScore(new StringBuffer(display.get(0)).toString()).toUpperCase();
            } else {
                throw new RuleException("Expecting a type on relation " + link);
            }
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
