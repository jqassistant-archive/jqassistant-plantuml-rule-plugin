package org.jqassistant.contrib.plugin.plantumlrule;

import org.jqassistant.contrib.plugin.plantumlrule.model.NodeLabel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class NodeLabelTest {

    public static Collection<Object[]> data() {
        return asList(new Object[][]{
            {null, null, null},
            {"", null, null},
            {"t", "t", null},
            {"t{x:1}", "t", "{x:1}"},
            {"t{x:1,y:\"foo\"}", "t", "{x:1,y:\"foo\"}"},
            {"t  { x:1, y:\"foo\" }", "t", "{ x:1, y:\"foo\" }"}
        });
    }

    @ParameterizedTest
    @MethodSource("data")
    public void parse(String value, String expectedAlias, String expectedFilter) {
        NodeLabel nodeLabel = NodeLabel.of(value);
        if (value == null) {
            assertThat(nodeLabel).isNull();
        } else {
            assertThat(nodeLabel.getAlias()).isEqualTo(expectedAlias);
            assertThat(nodeLabel.getFilter()).isEqualTo(expectedFilter);
        }
    }
}
