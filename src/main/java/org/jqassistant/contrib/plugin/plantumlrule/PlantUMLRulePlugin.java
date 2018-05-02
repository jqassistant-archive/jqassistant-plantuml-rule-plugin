package org.jqassistant.contrib.plugin.plantumlrule;

import static java.util.Collections.singletonList;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.RuleLanguagePlugin;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.classdiagram.AbstractEntityDiagram;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.cucadiagram.ILeaf;
import net.sourceforge.plantuml.cucadiagram.Link;
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
        if (diagram instanceof AbstractEntityDiagram) {
            return evaluateDescriptionDiagram((AbstractEntityDiagram) diagram);
        }
        throw new RuleException("Rule type " + diagram.getClass().getName() + " is not supported.");
    }

    private <T extends ExecutableRule<?>> Result<T> evaluateDescriptionDiagram(AbstractEntityDiagram descriptionDiagram) {
        Collection<ILeaf> leaves = descriptionDiagram.getLeafsvalues();
        List<Link> links = descriptionDiagram.getLinks();
        return null;
    }

    private <T extends ExecutableRule<?>> String getDiagramSource(T executableRule) throws RuleException {
        AbstractBlock abstractBlock = (AbstractBlock) executableRule.getExecutable().getSource();
        String fileName = (String) abstractBlock.getAttr("target");
        File diagramFile = new File(fileName);
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
}
