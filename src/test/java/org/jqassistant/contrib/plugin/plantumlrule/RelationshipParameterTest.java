package org.jqassistant.contrib.plugin.plantumlrule;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class RelationshipParameterTest {

    private String label;

    private String expectedAlias;

    private String expectedType;

    private String expectedHops;

    private String expectedFilter;

    public RelationshipParameterTest(String label, String expectedAlias, String expectedType, String expectedHops, String expectedFilter) {
        this.label = label;
        this.expectedAlias = expectedAlias;
        this.expectedType = expectedType;
        this.expectedHops = expectedHops;
        this.expectedFilter = expectedFilter;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
            {null, null, null, null, null},
            {"", null, null, null, null},
            {"e", "e", null, null, null},
            {":EXTENDS", null, "EXTENDS", null, null},
            {":+EXTENDS", null, "+EXTENDS", null, null},
            {"e:EXTENDS", "e", "EXTENDS", null, null},
            {"e:EXTENDS*", "e", "EXTENDS", "*", null},
            {"e:EXTENDS*0..", "e", "EXTENDS", "*0..", null},
            {"e:EXTENDS*..1", "e", "EXTENDS", "*..1", null},
            {"e:EXTENDS*..1{x:1}", "e", "EXTENDS", "*..1", "{x:1}"},
            {"e:EXTENDS*..1{x:1,y:\"foo\"}", "e", "EXTENDS", "*..1", "{x:1,y:\"foo\"}"},
            {"e :EXTENDS *..1 { x:1, y:\"foo\" }", "e", "EXTENDS", "*..1", "{ x:1, y:\"foo\" }"},
            {"e :+EXTENDS *..1 { x:1, y:\"foo\" }", "e", "+EXTENDS", "*..1", "{ x:1, y:\"foo\" }"}
        });
    }

    @Test
    public void parse() {
        RelationshipParameter relationshipParameter = RelationshipParameter.getRelationshipParameter(label);
        if (label == null) {
            assertThat(relationshipParameter, nullValue());
        } else {
            assertThat(relationshipParameter.getAlias(), equalTo(expectedAlias));
            assertThat(relationshipParameter.getType(), equalTo(expectedType));
            assertThat(relationshipParameter.getHops(), equalTo(expectedHops));
            assertThat(relationshipParameter.getFilter(), equalTo(expectedFilter));
        }
    }
}
