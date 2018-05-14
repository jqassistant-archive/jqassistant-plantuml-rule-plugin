package org.jqassistant.contrib.plugin.plantumlrule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class NodeParameterTest {

    @Test
    public void nodeAlias() {
        NodeParameter a = NodeParameter.getNodeParameter("A");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getFilter(), nullValue());
    }

    @Test
    public void nodeAliasWithSingleAttributeFilter() {
        NodeParameter a = NodeParameter.getNodeParameter("A {x:\"foo\"}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getFilter(), equalTo("{x:\"foo\"}"));
    }

    @Test
    public void nodeAliasWithMultiAttributeFilter() {
        NodeParameter a = NodeParameter.getNodeParameter("A {x:\"foo\", y:\"bar\"}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getFilter(), equalTo("{x:\"foo\", y:\"bar\"}"));
    }

    @Test
    public void nodeAliasWithNumberAttributeFilter() {
        NodeParameter a = NodeParameter.getNodeParameter("A {value:42}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getFilter(), equalTo("{value:42}"));
    }

    @Test
    public void nodeFilterOnly() {
        NodeParameter a = NodeParameter.getNodeParameter("{value:42}");
        assertThat(a.getAlias(), nullValue());
        assertThat(a.getFilter(), equalTo("{value:42}"));
    }

    @Test
    public void emptyNode() {
        NodeParameter a = NodeParameter.getNodeParameter("");
        assertThat(a.getAlias(), nullValue());
        assertThat(a.getFilter(), nullValue());
    }

    @Test
    public void nullNode() {
        NodeParameter a = NodeParameter.getNodeParameter(null);
        assertThat(a, nullValue());
    }
}
