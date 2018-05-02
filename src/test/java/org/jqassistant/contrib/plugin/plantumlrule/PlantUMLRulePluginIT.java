package org.jqassistant.contrib.plugin.plantumlrule;

import java.io.IOException;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PlantUMLRulePluginIT extends AbstractJavaPluginIT {

    @Test
    public void asciidocComponentDiagram() throws IOException, RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRulePluginIT.class));
        assertThat(applyConcept("plantuml-rule:ComponentDiagramAdoc").getStatus(), equalTo(SUCCESS));
    }

    @Test
    public void asciidocClassDiagram() throws IOException, RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRulePluginIT.class));
        assertThat(applyConcept("plantuml-rule:ClassDiagramAdoc").getStatus(), equalTo(SUCCESS));
    }
}
