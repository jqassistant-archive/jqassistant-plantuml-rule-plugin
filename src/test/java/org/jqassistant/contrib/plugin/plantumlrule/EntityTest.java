package org.jqassistant.contrib.plugin.plantumlrule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EntityTest {

    @Test
    public void alias() {
        EntityParameter a = EntityParameter.getEntity("A");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getFilter(), nullValue());
    }

    @Test
    public void aliasWithSingleAttributeFilter() {
        EntityParameter a = EntityParameter.getEntity("A {x:\"foo\"}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getFilter(), equalTo("{x:\"foo\")}"));
    }

    @Test
    public void aliasWithMultiAttributeFilter() {
        EntityParameter a = EntityParameter.getEntity("A {x:\"foo\", y:\"bar\"}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getFilter(), equalTo("{x:\"foo\", y:\"bar\"}"));
    }

    @Test
    public void aliasWithNumberAttributeFilter() {
        EntityParameter a = EntityParameter.getEntity("A {value:42}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getFilter(), equalTo("{value:42}"));
    }

    @Test
    public void filterOnly() {
        EntityParameter a = EntityParameter.getEntity("{value:42}");
        assertThat(a.getAlias(), nullValue());
        assertThat(a.getFilter(), equalTo("{value:42}"));
    }

    @Test
    public void empty() {
        EntityParameter a = EntityParameter.getEntity("");
        assertThat(a.getAlias(), nullValue());
        assertThat(a.getFilter(), nullValue());
    }

    @Test
    public void nul() {
        EntityParameter a = EntityParameter.getEntity(null);
        assertThat(a.getAlias(), nullValue());
        assertThat(a.getFilter(), nullValue());
    }
}
