package org.jqassistant.contrib.plugin.plantumlrule.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.*;

/**
 * Represents the parameters that are present at a relationship of an entity
 * diagram.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@ToString
public class RelationshipParameter {

    // EXTENDS*0..1{key1:1,key2:"test"}
    public static final Pattern RELATIONSHIP_PATTERN = Pattern
        .compile("(?<modifier>\\+)?\\s?" + "(?<alias>\\w+)?\\s?" + "(:\\s?(?<type>\\w+))?\\s?" + "(?<hops>\\*([0-9]?(..)?[0-9]?))" + "?\\s?(?<filter>\\{.*})?");

    public static final RelationshipParameter DEFAULT = RelationshipParameter.builder().build();

    private String modifier;

    /**
     * The alias for the relationship.
     */
    private String alias;

    /**
     * The type of the relationship.
     */
    private String type;

    /**
     * The hops pattern, e.g. "*0..3".
     */
    private String hops;

    /**
     * The filter for the {@link Relationship}, e.g. {virtual: true}.
     */
    private String filter;

    public static RelationshipParameter getRelationshipParameter(CharSequence label) {
        if (label == null) {
            return null;
        }
        Matcher matcher = RELATIONSHIP_PATTERN.matcher(label);
        if (!matcher.matches()) {
            return null;
        }
        return RelationshipParameter.builder().modifier(matcher.group("modifier")).alias(matcher.group("alias")).type(matcher.group("type")).hops(matcher.group("hops"))
            .filter(matcher.group("filter")).build();
    }

}
