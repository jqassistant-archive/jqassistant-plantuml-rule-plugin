package org.jqassistant.contrib.plugin.plantumlrule;

import java.io.IOException;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.Test;

public class PlantUMLRulePluginIT extends AbstractJavaPluginIT {

    @Test
    public void asciidocComponentDiagram() throws IOException, RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRulePluginIT.class));
        applyConcept("plantuml-rule:ClassDiagramAdoc");
    }

    @Test
    public void asciidocClassDiagram() throws IOException, RuleException {
        scanClassPathDirectory(getClassesDirectory(PlantUMLRulePluginIT.class));
        applyConcept("plantuml-rule:ClassDiagramAdoc");
    }
}
