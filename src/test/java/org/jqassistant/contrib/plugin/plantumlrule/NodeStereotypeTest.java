package org.jqassistant.contrib.plugin.plantumlrule;

import org.jqassistant.contrib.plugin.plantumlrule.model.NodeStereotype;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class NodeStereotypeTest {

    private String value;

    private String expectedModifier;

    private String expectedLabel;

    public NodeStereotypeTest(String value, String expectedModifier, String expectedLabel) {
        this.value = value;
        this.expectedModifier = expectedModifier;
        this.expectedLabel = expectedLabel;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
            {null, null, null},
            {"Root", null, "Root"},
            {"(+)Root", "+", "Root"},
            {"(+) Root ", "+", "Root"},
        });
    }

    @Test
    public void parse() {
        NodeStereotype nodeStereotype = NodeStereotype.of(value);
        if (value == null) {
            assertThat(nodeStereotype, nullValue());
        } else {
            assertThat(nodeStereotype.getModifier(), equalTo(expectedModifier));
            assertThat(nodeStereotype.getLabel(), equalTo(expectedLabel));
        }
    }
}
