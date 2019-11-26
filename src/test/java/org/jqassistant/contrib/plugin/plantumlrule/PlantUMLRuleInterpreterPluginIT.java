package org.jqassistant.contrib.plugin.plantumlrule;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.AsciidocRuleParserPlugin;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.xo.api.CompositeObject;
import org.hamcrest.MatcherAssert;
import org.jqassistant.contrib.plugin.plantumlrule.set.root.Root;
import org.jqassistant.contrib.plugin.plantumlrule.set.root.a.A;
import org.jqassistant.contrib.plugin.plantumlrule.set.root.b.B;
import org.jqassistant.contrib.plugin.plantumlrule.set.root.c.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

public class PlantUMLRuleInterpreterPluginIT extends AbstractJavaPluginIT {

    public static final Package ROOT = Root.class.getPackage();
    public static final Package MODULE_A = A.class.getPackage();
    public static final Package MODULE_B = B.class.getPackage();
    public static final Package MODULE_C = C.class.getPackage();

    @BeforeEach
    public void initRules() throws RuleException {
        AsciidocRuleParserPlugin asciidocRuleParserPlugin = new AsciidocRuleParserPlugin();
        asciidocRuleParserPlugin.initialize();
        asciidocRuleParserPlugin.configure(RuleConfiguration.DEFAULT);
        RuleParser ruleParser = new RuleParser(singletonList(asciidocRuleParserPlugin));
        ruleSet = ruleParser.parse(singletonList(new FileRuleSource(new File("README.adoc"))));
    }

    @Test
    public void asciidocComponentDiagram() throws RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRuleInterpreterPluginIT.class));
        Result<Concept> conceptResult = applyConcept("plantuml-rule:ComponentDiagram");
        assertThat(conceptResult.getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        List<Map<String, Object>> resultRows = conceptResult.getRows();
        assertThat(resultRows.size()).isEqualTo(1);
        Map<String, Object> resultRow = resultRows.get(0);
        assertThat(resultRow.size()).isEqualTo(6);
        MatcherAssert.assertThat((PackageDescriptor) resultRow.get("root"), packageDescriptor(ROOT));
        MatcherAssert.assertThat((PackageDescriptor) resultRow.get("a"), packageDescriptor(MODULE_A));
        MatcherAssert.assertThat((PackageDescriptor) resultRow.get("b"), packageDescriptor(MODULE_B));
        MatcherAssert.assertThat((PackageDescriptor) resultRow.get("c"), packageDescriptor(MODULE_C));
        assertThat(resultRow.get("d1")).isInstanceOf(CompositeObject.class);
        assertThat(resultRow.get("d2")).isInstanceOf(CompositeObject.class);
        TestResult modulesResult = query("MATCH (m:Module) RETURN m");
        List<PackageDescriptor> modules = modulesResult.getColumn("m");
        assertThat(modules.size()).isEqualTo(3);
        MatcherAssert.assertThat(modules, hasItems(packageDescriptor(MODULE_A), packageDescriptor(MODULE_B), packageDescriptor(MODULE_C)));
        assertThat(query("MATCH (b:Module{name:'b'})-[:DEFINES_DEPENDENCY]->(a:Module{name:'a'}) RETURN b, a").getRows().size()).isEqualTo(1);
        assertThat(query("MATCH (c:Module{name:'c'})-[:DEFINES_DEPENDENCY]->(a:Module{name:'a'}) RETURN c, a").getRows().size()).isEqualTo(1);
        store.commitTransaction();
    }

    @Test
    public void asciidocComponentDiagramWithoutAliases() throws RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRuleInterpreterPluginIT.class));
        Result<Concept> conceptResult = applyConcept("plantuml-rule:ComponentDiagramWithoutAliases");
        assertThat(conceptResult.getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        List<Map<String, Object>> resultRows = conceptResult.getRows();
        assertThat(resultRows.size()).isEqualTo(1);
        Map<String, Object> resultRow = resultRows.get(0);
        assertThat(resultRow.size()).isEqualTo(1);
        assertThat(resultRow.get("Count")).isEqualTo(1l);
        store.commitTransaction();
    }

    @Test
    public void asciidocNestedPackageDiagram() throws RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRuleInterpreterPluginIT.class));
        Result<Concept> result = applyConcept("plantuml-rule:NestedPackageDiagram");
        store.beginTransaction();
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        assertThat(result.getColumnNames()).isEqualTo(asList("layer"));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size()).isEqualTo(3);
        store.commitTransaction();
    }

    @Test
    public void asciidocClassDiagram() throws RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRuleInterpreterPluginIT.class));
        assertThat(applyConcept("plantuml-rule:ClassDiagram").getStatus()).isEqualTo(SUCCESS);
    }
}
