package org.jqassistant.contrib.plugin.plantumlrule;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.jqassistant.contrib.plugin.plantumlrule.model.RelationshipLabel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class RelationshipLabelTest {

    private String value;

    private String expectedModifier;

    private String expectedAlias;

    private String expectedType;

    private String expectedHops;

    private String expectedFilter;

    public RelationshipLabelTest(String value, String expectedModifier, String expectedAlias, String expectedType, String expectedHops, String expectedFilter) {
        this.value = value;
        this.expectedModifier = expectedModifier;
        this.expectedAlias = expectedAlias;
        this.expectedType = expectedType;
        this.expectedHops = expectedHops;
        this.expectedFilter = expectedFilter;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
            {null, null, null, null, null, null},
            {"", null, null, null, null, null},
            {"e", null,"e", null, null, null},
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

    @Test
    public void parse() {
        RelationshipLabel relationshipLabel = RelationshipLabel.of(value);
        if (value == null) {
            assertThat(relationshipLabel, nullValue());
        } else {
            assertThat(relationshipLabel.getModifier(), equalTo(expectedModifier));
            assertThat(relationshipLabel.getAlias(), equalTo(expectedAlias));
            assertThat(relationshipLabel.getType(), equalTo(expectedType));
            assertThat(relationshipLabel.getHops(), equalTo(expectedHops));
            assertThat(relationshipLabel.getFilter(), equalTo(expectedFilter));
        }
    }
}
