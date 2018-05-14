package org.jqassistant.contrib.plugin.plantumlrule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@ToString
public class RelationshipParameter {

    // EXTENDS*0..1{key1:1,key2:"test"}
    public static final Pattern RELATIONSHIP_PATTERN = Pattern.compile("(?<alias>\\w+)?\\s?" + "(:(?<type>\\+?\\w+))?\\s?" + "(?<hops>\\*([0-9]?(..)?[0-9]?))" + "?\\s?(?<filter>\\{.*})?");

    public static final RelationshipParameter DEFAULT = RelationshipParameter.builder().build();

    private String alias;

    private String type;

    private String hops;

    private String filter;

    public static RelationshipParameter getRelationshipParameter(CharSequence label) {
        if (label == null) {
            return null;
        }
        Matcher matcher = RELATIONSHIP_PATTERN.matcher(label);
        if (!matcher.matches()) {
            return null;
        }
        return RelationshipParameter.builder().alias(matcher.group("alias")).type(matcher.group("type")).hops(matcher.group("hops")).filter(matcher.group("filter")).build();
    }

}
