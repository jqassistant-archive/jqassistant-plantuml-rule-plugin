package org.jqassistant.contrib.plugin.plantumlrule;

import org.jqassistant.contrib.plugin.plantumlrule.model.RelationshipLabel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RelationshipLabelTest {

    public static Collection<Object[]> data() {
        return asList(new Object[][]{
            {null, null, null, null, null, null},
            {"", null, null, null, null, null},
            {"e", null, "e", null, null, null},
            {":EXTENDS", null, null, "EXTENDS", null, null},
            {"(+):EXTENDS", "+", null, "EXTENDS", null, null},
            {"e:EXTENDS", null, "e", "EXTENDS", null, null},
            {"e:EXTENDS*", null, "e", "EXTENDS", "*", null},
            {"e:EXTENDS*0..", null, "e", "EXTENDS", "*0..", null},
            {"e:EXTENDS*..1", null, "e", "EXTENDS", "*..1", null},
            {"e:EXTENDS*..1{x:1}", null, "e", "EXTENDS", "*..1", "{x:1}"},
            {"e:EXTENDS*..1{x:1,y:\"foo\"}", null, "e", "EXTENDS", "*..1", "{x:1,y:\"foo\"}"},
            {"e :EXTENDS *..1 { x:1, y:\"foo\" }", null, "e", "EXTENDS", "*..1", "{ x:1, y:\"foo\" }"},
            {"(+) e : EXTENDS *..1 { x:1, y:\"foo\" }", "+", "e", "EXTENDS", "*..1", "{ x:1, y:\"foo\" }"}
        });
    }

    @ParameterizedTest
    @MethodSource("data")
    public void parse(String value, String expectedModifier, String expectedAlias, String expectedType, String expectedHops, String expectedFilter) {
        RelationshipLabel relationshipLabel = RelationshipLabel.of(value);
        if (value == null) {
            assertThat(relationshipLabel).isNull();
        } else {
            assertThat(relationshipLabel.getModifier()).isEqualTo(expectedModifier);
            assertThat(relationshipLabel.getAlias()).isEqualTo(expectedAlias);
            assertThat(relationshipLabel.getType()).isEqualTo(expectedType);
            assertThat(relationshipLabel.getHops()).isEqualTo(expectedHops);
            assertThat(relationshipLabel.getFilter()).isEqualTo(expectedFilter);
        }
    }
}
