package org.jqassistant.contrib.plugin.plantumlrule;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.xo.api.CompositeObject;

import org.jqassistant.contrib.plugin.plantumlrule.set.root.Root;
import org.jqassistant.contrib.plugin.plantumlrule.set.root.a.A;
import org.jqassistant.contrib.plugin.plantumlrule.set.root.b.B;
import org.jqassistant.contrib.plugin.plantumlrule.set.root.c.C;
import org.junit.Test;

public class PlantUMLRuleInterpreterPluginIT extends AbstractJavaPluginIT {

    public static final Package ROOT = Root.class.getPackage();
    public static final Package MODULE_A = A.class.getPackage();
    public static final Package MODULE_B = B.class.getPackage();
    public static final Package MODULE_C = C.class.getPackage();

    @Test
    public void asciidocComponentDiagram() throws IOException, RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRuleInterpreterPluginIT.class));
        Result<Concept> conceptResult = applyConcept("plantuml-rule:ComponentDiagram");
        assertThat(conceptResult.getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Map<String, Object>> resultRows = conceptResult.getRows();
        assertThat(resultRows.size(), equalTo(1));
        Map<String, Object> resultRow = resultRows.get(0);
        assertThat(resultRow.size(), equalTo(6));
        assertThat((PackageDescriptor) resultRow.get("root"), packageDescriptor(ROOT));
        assertThat((PackageDescriptor) resultRow.get("a"), packageDescriptor(MODULE_A));
        assertThat((PackageDescriptor) resultRow.get("b"), packageDescriptor(MODULE_B));
        assertThat((PackageDescriptor) resultRow.get("c"), packageDescriptor(MODULE_C));
        assertThat(resultRow.get("d1"), instanceOf(CompositeObject.class));
        assertThat(resultRow.get("d2"), instanceOf(CompositeObject.class));
        TestResult modulesResult = query("MATCH (m:Module) RETURN m");
        List<PackageDescriptor> modules = modulesResult.getColumn("m");
        assertThat(modules.size(), equalTo(3));
        assertThat(modules, hasItems(packageDescriptor(MODULE_A), packageDescriptor(MODULE_B), packageDescriptor(MODULE_C)));
        assertThat(query("MATCH (b:Module{name:'b'})-[:DEFINES_DEPENDENCY]->(a:Module{name:'a'}) RETURN b, a").getRows().size(), equalTo(1));
        assertThat(query("MATCH (c:Module{name:'c'})-[:DEFINES_DEPENDENCY]->(a:Module{name:'a'}) RETURN c, a").getRows().size(), equalTo(1));
        store.commitTransaction();
    }

    @Test
    public void asciidocComponentDiagramWithoutAliases() throws IOException, RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRuleInterpreterPluginIT.class));
        Result<Concept> conceptResult = applyConcept("plantuml-rule:ComponentDiagramWithoutAliases");
        assertThat(conceptResult.getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Map<String, Object>> resultRows = conceptResult.getRows();
        assertThat(resultRows.size(), equalTo(1));
        Map<String, Object> resultRow = resultRows.get(0);
        assertThat(resultRow.size(), equalTo(1));
        assertThat(resultRow.get("Count"), equalTo(1l));
        store.commitTransaction();
    }

    @Test
    public void asciidocNestedPackageDiagram() throws IOException, RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRuleInterpreterPluginIT.class));
        Result<Concept> result = applyConcept("plantuml-rule:NestedPackageDiagram");
        store.beginTransaction();
        assertThat(result.getStatus(), equalTo(SUCCESS));
        assertThat(result.getColumnNames(), equalTo(asList("layer")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(3));
        store.commitTransaction();
    }

    @Test
    public void asciidocClassDiagram() throws IOException, RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRuleInterpreterPluginIT.class));
        assertThat(applyConcept("plantuml-rule:ClassDiagram").getStatus(), equalTo(SUCCESS));
    }
}
