package org.jqassistant.contrib.plugin.plantumlrule;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.regex.Matcher;

import org.junit.Test;

public class LabelPatternTest {

    @Test
    public void matchWithAlias() {
        List<String> expressions = asList("A", "A {x:\"foo\"}", "A {x:\"foo\",y:\"bar\"}", "A {value:42}");
        for (String expression : expressions) {
            Matcher matcher = Label.LABEL_PATTERN.matcher(expression);
            assertThat(matcher.matches(), equalTo(true));
            assertThat(matcher.group(1), equalTo("A"));
        }
    }

    @Test
    public void matchWithoutAlias() {
        List<String> expressions = asList("{x:\"foo\"}", "{x:\"foo\",y:\"bar\"}", "{value:42}");
        for (String expression : expressions) {
            Matcher matcher = Label.LABEL_PATTERN.matcher(expression);
            assertThat(matcher.matches(), equalTo(true));
            String filter = matcher.group(2);
            assertThat(filter, startsWith("{"));
            assertThat(filter, endsWith("}"));
        }
    }
}
