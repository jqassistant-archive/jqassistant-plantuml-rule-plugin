package org.jqassistant.contrib.plugin.plantumlrule.statement;

import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents a Cypher statement consisting of segments.
 */
@Builder
@Getter
@AllArgsConstructor(access = PRIVATE)
@ToString
public class Statement {

    private Segment matchSegment;

    private Segment mergeSegment;

    private Segment returnSegment;

    public String get() {
        Segment statement = new Segment();
        statement.append("MATCH");
        statement.newLine();
        statement.append(matchSegment.get());
        statement.newLine();
        statement.append(mergeSegment.get());
        statement.newLine();
        statement.append("RETURN");
        statement.newLine();
        if (returnSegment.isEmpty()) {
            returnSegment.indent();
            returnSegment.append("count(*)");
        } else {
            statement.append(returnSegment.get());
        }
        return statement.get();
    }

}
