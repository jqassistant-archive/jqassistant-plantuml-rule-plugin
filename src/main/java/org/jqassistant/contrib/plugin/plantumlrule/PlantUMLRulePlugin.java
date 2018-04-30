package org.jqassistant.contrib.plugin.plantumlrule;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.RuleLanguagePlugin;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

import org.asciidoctor.ast.AbstractBlock;

public class PlantUMLRulePlugin implements RuleLanguagePlugin {

    @Override
    public Set<String> getLanguages() {
        return new HashSet<>(asList("plantuml"));
    }

    @Override
    public <T extends ExecutableRule<?>> boolean accepts(T executableRule) {
        return AbstractBlock.class.isAssignableFrom(executableRule.getExecutable().getSource().getClass());
    }

    @Override
    public <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> ruleParameters, Severity severity, AnalyzerContext context)
            throws RuleException {
        return null;
    }
}
