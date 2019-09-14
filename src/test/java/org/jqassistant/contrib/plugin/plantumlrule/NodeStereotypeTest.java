package org.jqassistant.contrib.plugin.plantumlrule;

import org.jqassistant.contrib.plugin.plantumlrule.model.NodeStereotype;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


public class NodeStereotypeTest {

    public static Collection<Object[]> data() {
        return asList(new Object[][]{
            {null, null, null},
            {"Root", null, "Root"},
            {"(+)Root", "+", "Root"},
            {"(+) Root ", "+", "Root"},
        });
    }

    @ParameterizedTest
    @MethodSource("data")
    public void parse(String value, String expectedModifier, String expectedLabel) {
        NodeStereotype nodeStereotype = NodeStereotype.of(value);
        if (value == null) {
            assertThat(nodeStereotype).isNull();
        } else {
            assertThat(nodeStereotype.getModifier()).isEqualTo(expectedModifier);
            assertThat(nodeStereotype.getLabel()).isEqualTo(expectedLabel);
        }
    }
}
