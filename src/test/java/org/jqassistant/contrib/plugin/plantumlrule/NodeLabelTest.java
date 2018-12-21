package org.jqassistant.contrib.plugin.plantumlrule;

import org.jqassistant.contrib.plugin.plantumlrule.model.NodeLabel;
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
public class NodeLabelTest {

    private String value;

    private String expectedAlias;

    private String expectedFilter;

    public NodeLabelTest(String value, String expectedAlias, String expectedFilter) {
        this.value = value;
        this.expectedAlias = expectedAlias;
        this.expectedFilter = expectedFilter;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
            {null, null, null},
            {"", null, null},
            {"t", "t", null},
            {"t{x:1}", "t", "{x:1}"},
            {"t{x:1,y:\"foo\"}", "t", "{x:1,y:\"foo\"}"},
            {"t { x:1, y:\"foo\" }", "t", "{ x:1, y:\"foo\" }"}
        });
    }

    @Test
    public void parse() {
        NodeLabel nodeLabel = NodeLabel.of(value);
        if (value == null) {
            assertThat(nodeLabel, nullValue());
        } else {
            assertThat(nodeLabel.getAlias(), equalTo(expectedAlias));
            assertThat(nodeLabel.getFilter(), equalTo(expectedFilter));
        }
    }
}
