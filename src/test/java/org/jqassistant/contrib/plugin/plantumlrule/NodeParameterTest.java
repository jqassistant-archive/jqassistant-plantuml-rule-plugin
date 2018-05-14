package org.jqassistant.contrib.plugin.plantumlrule;

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
public class NodeParameterTest {

    private String label;

    private String expectedAlias;

    private String expectedFilter;

    public NodeParameterTest(String label, String expectedAlias, String expectedFilter) {
        this.label = label;
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
        NodeParameter nodeParameter = NodeParameter.getNodeParameter(label);
        if (label == null) {
            assertThat(nodeParameter, nullValue());
        } else {
            assertThat(nodeParameter.getAlias(), equalTo(expectedAlias));
            assertThat(nodeParameter.getFilter(), equalTo(expectedFilter));
        }
    }
}
