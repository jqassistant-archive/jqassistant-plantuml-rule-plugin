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
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.impl.AbstractCypherRuleInterpreterPlugin;
import com.buschmais.jqassistant.core.shared.asciidoc.AsciidoctorFactory;

import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.cucadiagram.CucaDiagram;
import net.sourceforge.plantuml.png.MetadataTag;
import org.asciidoctor.ast.AbstractBlock;
import org.jqassistant.contrib.plugin.plantumlrule.statement.StatementBuilder;

public class PlantUMLRuleInterpreterPlugin extends AbstractCypherRuleInterpreterPlugin {

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
        context.getLogger().info(executableRule + "\n----\n" + diagramSource + "\n----");
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
        CucaDiagramParser cucaDiagramParser = new CucaDiagramParser(diagram);
        StatementBuilder statementBuilder = new StatementBuilder(cucaDiagramParser.getNodes(), cucaDiagramParser.getRelationships());
        String statement = statementBuilder.create().get();
        context.getLogger().info("\n" + statement + "\n----");
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
}
