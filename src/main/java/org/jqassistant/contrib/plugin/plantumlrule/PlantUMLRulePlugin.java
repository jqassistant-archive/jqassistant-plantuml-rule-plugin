package org.jqassistant.contrib.plugin.plantumlrule;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.RuleLanguagePlugin;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.shared.asciidoc.AsciidoctorFactory;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.cucadiagram.CucaDiagram;
import net.sourceforge.plantuml.cucadiagram.ILeaf;
import net.sourceforge.plantuml.cucadiagram.Link;
import net.sourceforge.plantuml.cucadiagram.Stereotype;
import net.sourceforge.plantuml.png.MetadataTag;
import org.asciidoctor.ast.AbstractBlock;

public class PlantUMLRulePlugin implements RuleLanguagePlugin {

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
            return evaluate((CucaDiagram) diagram, executableRule, severity, context);
        }
        throw new RuleException("Rule type " + diagram.getClass().getName() + " is not supported.");
    }

    private <T extends ExecutableRule<?>> Result<T> evaluate(CucaDiagram diagram, T executableRule, Severity severity, AnalyzerContext context) {
        Map<String, Node> nodes = getNodes(diagram);
        List<Link> links = diagram.getLinks();
        return new Result<>(executableRule, Result.Status.SUCCESS, severity, emptyList(), emptyList());
    }

    private Map<String, Node> getNodes(CucaDiagram diagram) {
        Collection<ILeaf> leaves = diagram.getLeafsvalues();
        Map<String, Node> nodes = new HashMap<>();
        for (ILeaf leaf : leaves) {
            Node.NodeBuilder nodeBuilder = Node.builder();
            nodeBuilder.id(leaf.getUid());
            Stereotype stereotype = leaf.getStereotype();
            for (String label : stereotype.getMultipleLabels()) {
                if (label.startsWith("+")) {
                    nodeBuilder.setLabel(label.substring(1));
                } else {
                    nodeBuilder.matchLabel(label);
                }
            }
            Node node = nodeBuilder.build();
            nodes.put(node.id, node);
            for (CharSequence charSequence : leaf.getDisplay()) {
                // Extract attributes
            }
        }
        return nodes;
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

    @Builder
    @Getter
    @ToString
    private static class Node {

        private String id;

        @Singular
        private Set<String> matchLabels;

        @Singular
        private Set<String> setLabels;
    }
}
