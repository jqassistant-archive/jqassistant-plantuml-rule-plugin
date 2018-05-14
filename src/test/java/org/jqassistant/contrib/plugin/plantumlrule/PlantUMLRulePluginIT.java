package org.jqassistant.contrib.plugin.plantumlrule;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.Test;

public class PlantUMLRulePluginIT extends AbstractJavaPluginIT {

    @Test
    public void asciidocComponentDiagram() throws IOException, RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRulePluginIT.class));
        assertThat(applyConcept("plantuml-rule:ComponentDiagramAdoc").getStatus(), equalTo(SUCCESS));
    }

    @Test
    public void asciidocNestedPackageDiagram() throws IOException, RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRulePluginIT.class));
        Result<Concept> result = applyConcept("plantuml-rule:NestedPackageDiagramAdoc");
        store.beginTransaction();
        assertThat(result.getStatus(), equalTo(SUCCESS));
        assertThat(result.getColumnNames(), equalTo(asList("layer")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(3));
        store.commitTransaction();
    }

    @Test
    public void asciidocClassDiagram() throws IOException, RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRulePluginIT.class));
        assertThat(applyConcept("plantuml-rule:ClassDiagramAdoc").getStatus(), equalTo(SUCCESS));
    }
}
