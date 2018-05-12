package org.jqassistant.contrib.plugin.plantumlrule;


import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class EntityParameterTest {

    @Test
    public void alias() {
        EntityParameter a = EntityParameter.getEntityParameter("A");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getFilter(), nullValue());
    }

    @Test
    public void aliasWithSingleAttributeFilter() {
        EntityParameter a = EntityParameter.getEntityParameter("A {x:\"foo\"}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getFilter(), equalTo("{x:\"foo\"}"));
    }

    @Test
    public void aliasWithMultiAttributeFilter() {
        EntityParameter a = EntityParameter.getEntityParameter("A {x:\"foo\", y:\"bar\"}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getFilter(), equalTo("{x:\"foo\", y:\"bar\"}"));
    }

    @Test
    public void aliasWithNumberAttributeFilter() {
        EntityParameter a = EntityParameter.getEntityParameter("A {value:42}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getFilter(), equalTo("{value:42}"));
    }

    @Test
    public void filterOnly() {
        EntityParameter a = EntityParameter.getEntityParameter("{value:42}");
        assertThat(a.getAlias(), nullValue());
        assertThat(a.getFilter(), equalTo("{value:42}"));
    }

    @Test
    public void empty() {
        EntityParameter a = EntityParameter.getEntityParameter("");
        assertThat(a.getAlias(), nullValue());
        assertThat(a.getFilter(), nullValue());
    }

    @Test
    public void nul() {
        EntityParameter a = EntityParameter.getEntityParameter(null);
        assertThat(a, nullValue());
    }
}
