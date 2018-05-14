package org.jqassistant.contrib.plugin.plantumlrule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class RelationshipParameterTest {

    @Test
    public void relationshipAlias() {
        RelationshipParameter a = RelationshipParameter.getRelationshipParameter("EXTENDS");
        assertThat(a.getAlias(), equalTo("EXTENDS"));
        assertThat(a.getHops(), nullValue());
        assertThat(a.getFilter(), nullValue());
    }

    @Test
    public void relationshipAliasWithSingleAttributeFilter() {
        RelationshipParameter a = RelationshipParameter.getRelationshipParameter("A {x:\"foo\"}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getHops(), nullValue());
        assertThat(a.getFilter(), equalTo("{x:\"foo\"}"));
    }

    @Test
    public void relationshipAliasWithMultiAttributeFilter() {
        RelationshipParameter a = RelationshipParameter.getRelationshipParameter("A {x:\"foo\", y:\"bar\"}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getHops(), nullValue());
        assertThat(a.getFilter(), equalTo("{x:\"foo\", y:\"bar\"}"));
    }

    @Test
    public void relationshipAliasWithNumberAttributeFilter() {
        RelationshipParameter a = RelationshipParameter.getRelationshipParameter("A {value:42}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getHops(), nullValue());
        assertThat(a.getFilter(), equalTo("{value:42}"));
    }

    @Test
    public void relationshipFilterOnly() {
        RelationshipParameter a = RelationshipParameter.getRelationshipParameter("{value:42}");
        assertThat(a.getAlias(), nullValue());
        assertThat(a.getHops(), nullValue());
        assertThat(a.getFilter(), equalTo("{value:42}"));
    }

    @Test
    public void emptyRelationship() {
        RelationshipParameter a = RelationshipParameter.getRelationshipParameter("");
        assertThat(a.getAlias(), nullValue());
        assertThat(a.getHops(), nullValue());
        assertThat(a.getFilter(), nullValue());
    }

    @Test
    public void nullRelationshipNode() {
        RelationshipParameter a = RelationshipParameter.getRelationshipParameter(null);
        assertThat(a, nullValue());
    }

    @Test
    public void relationshipAliasWithHopsAndAttributeFilter() {
        RelationshipParameter a = RelationshipParameter.getRelationshipParameter("A* {x:\"foo\", y:\"bar\"}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getHops(), equalTo("*"));
        assertThat(a.getFilter(), equalTo("{x:\"foo\", y:\"bar\"}"));
    }

    @Test
    public void relationshipAliasWithLowerBoundHopsAndAttributeFilter() {
        RelationshipParameter a = RelationshipParameter.getRelationshipParameter("A*0.. {x:\"foo\", y:\"bar\"}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getHops(), equalTo("*0.."));
        assertThat(a.getFilter(), equalTo("{x:\"foo\", y:\"bar\"}"));
    }

    @Test
    public void relationshipAliasWithUpperBoundHopsAndAttributeFilter() {
        RelationshipParameter a = RelationshipParameter.getRelationshipParameter("A*..3 {x:\"foo\", y:\"bar\"}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getHops(), equalTo("*..3"));
        assertThat(a.getFilter(), equalTo("{x:\"foo\", y:\"bar\"}"));
    }

    @Test
    public void relationshipAliasWithLowerAndUpperBoundHopsAndAttributeFilter() {
        RelationshipParameter a = RelationshipParameter.getRelationshipParameter("A*0..3 {x:\"foo\", y:\"bar\"}");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getHops(), equalTo("*0..3"));
        assertThat(a.getFilter(), equalTo("{x:\"foo\", y:\"bar\"}"));
    }

    @Test
    public void relationshipAliasWithLowerAndUpperBoundHops() {
        RelationshipParameter a = RelationshipParameter.getRelationshipParameter("A *0..3");
        assertThat(a.getAlias(), equalTo("A"));
        assertThat(a.getHops(), equalTo("*0..3"));
        assertThat(a.getFilter(), nullValue());
    }
}
