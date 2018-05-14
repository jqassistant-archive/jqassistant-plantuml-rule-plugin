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

    // EXTENDS*0..1{value:1}
    public static final Pattern RELATIONSHIP_PATTERN = Pattern.compile("(?<alias>\\w+)?\\s?" + "(?<hops>\\*([0-9]?(..)?[0-9]?))" + "?\\s?(?<filter>\\{.*})?");

    private String alias;

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
        return RelationshipParameter.builder().alias(matcher.group("alias")).hops(matcher.group("hops")).filter(matcher.group("filter")).build();
    }

}
