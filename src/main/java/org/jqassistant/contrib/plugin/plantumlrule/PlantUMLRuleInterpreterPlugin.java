package org.jqassistant.contrib.plugin.plantumlrule;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.impl.AbstractCypherRuleInterpreterPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.shared.asciidoc.AsciidoctorFactory;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.cucadiagram.CucaDiagram;
import net.sourceforge.plantuml.png.MetadataTag;
import org.asciidoctor.ast.StructuralNode;
import org.jqassistant.contrib.plugin.plantumlrule.statement.Statement;
import org.jqassistant.contrib.plugin.plantumlrule.statement.StatementBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;

/**
 * A {@link RuleInterpreterPlugin} that parses PlantUML diagrams and translates
 * them into Cypher queries.
 */
public class PlantUMLRuleInterpreterPlugin extends AbstractCypherRuleInterpreterPlugin {

    private static final Pattern PLANTUML_PATTERN = Pattern.compile("^\\s*(@startuml\\s+.*@enduml)\\s.*", Pattern.DOTALL);

    @Override
    public Collection<String> getLanguages() {
        return singletonList("plantuml");
    }

    @Override
    public <T extends ExecutableRule<?>> boolean accepts(T executableRule) {
        return StructuralNode.class.isAssignableFrom(executableRule.getExecutable().getSource().getClass());
    }

    @Override
    public <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> ruleParameters, Severity severity, AnalyzerContext context)
            throws RuleException {
        String diagramSource = getDiagramSource(executableRule);
        context.getLogger().info(executableRule.toString());
        context.getLogger().info("----\n" + diagramSource + "\n----");
        SourceStringReader reader = new SourceStringReader(diagramSource);
        List<BlockUml> blocks = reader.getBlocks();
        Diagram diagram = blocks.get(0).getDiagram();
        Statement statement = getStatement(diagram);
        String cypher = statement.get();
        context.getLogger().info("----\n" + cypher + "\n----");
        // TODO AbstractCypherRuleInterpreterPlugin should allow passing the
        // Verification as parameter instead of providing protected #getStatus.
        return execute(cypher, executableRule, ruleParameters, severity, context);
    }

    /**
     * Evaluate the given diagram and create the corresponding cypher statement for
     * it.
     *
     * @param <T>
     *            The {@link ExecutableRule} type.
     * @param diagram
     *            The {@link Diagram}.
     * @return The {@link Statement}.
     * @throws RuleException
     *             If the diagram cannot be evaluated.
     */
    private <T extends ExecutableRule<?>> Statement getStatement(Diagram diagram) throws RuleException {
        if (diagram instanceof CucaDiagram) {
            return evaluate((CucaDiagram) diagram);
        }
        throw new RuleException("The diagram type " + diagram.getClass().getName() + " is not supported.");
    }

    /**
     * Evaluate a {@link CucaDiagram}.
     *
     * @param <T>
     *            The {@link ExecutableRule} type.
     * @param diagram
     *            The {@link CucaDiagram}.
     * @return The {@link Result}.
     */
    private <T extends ExecutableRule<?>> Statement evaluate(CucaDiagram diagram) {
        CucaDiagramParser cucaDiagramParser = new CucaDiagramParser(diagram);
        StatementBuilder statementBuilder = new StatementBuilder(cucaDiagramParser.getNodes(), cucaDiagramParser.getRelationships());
        return statementBuilder.create();
    }

    @Override
    protected <T extends ExecutableRule<?>> Result.Status getStatus(T executableRule, List<String> columnNames, List<Map<String, Object>> rows,
            AnalyzerContext context) throws RuleException {
        // TODO should evaluate Statement.isAggregation
        if (columnNames != null && columnNames.size() == 1 && columnNames.get(0).equals(Statement.COUNT)) {
            return context.verify(executableRule, columnNames, rows, AggregationVerification.builder().build());
        } else {
            return context.verify(executableRule, columnNames, rows, RowCountVerification.builder().build());
        }
    }

    /**
     * Extract the source code of the diagram from the given {@link ExecutableRule}.
     *
     * @param executableRule
     *            The {@link ExecutableRule}.
     * @param <T>
     *            The {@link ExecutableRule} type.
     * @return The extracted source code.
     * @throws RuleException
     *             If extraction fails.
     */
    private <T extends ExecutableRule<?>> String getDiagramSource(T executableRule) throws RuleException {
        StructuralNode abstractBlock = (StructuralNode) executableRule.getExecutable().getSource();
        // Asciidoctor delivers the rendered image which contains the diagram source
        // code as metadata.
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
        // The metadata contains more information than necessary, so using a regex to
        // extract the relevant part between @startuml and @enduml
        Matcher matcher = PLANTUML_PATTERN.matcher(diagramMetadata);
        if (!matcher.matches()) {
            throw new RuleException("Cannot find a PlantUML diagram, expecting '@startuml ... @enduml' but got '" + diagramMetadata + "'.");
        }
        return matcher.group(1);
    }

    private File getImagesDirectory(StructuralNode abstractBlock) {
        String imagesDirectory = (String) abstractBlock.getDocument().getAttributes().get(AsciidoctorFactory.ATTRIBUTE_IMAGES_OUT_DIR);
        return new File(imagesDirectory);
    }
}
