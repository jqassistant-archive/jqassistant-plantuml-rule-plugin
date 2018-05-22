package org.jqassistant.contrib.plugin.plantumlrule.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.*;

/**
 * Represents the parameters that are present at a node of an entity diagram.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@ToString
public class NodeParameter {

    public static final Pattern NODE_PATTERN = Pattern.compile("(?<alias>\\w+)?\\s?(?<filter>\\{.*})?");

    public static final NodeParameter DEFAULT = NodeParameter.builder().build();

    private String alias;

    private String filter;

    public static NodeParameter getNodeParameter(CharSequence label) {
        if (label == null) {
            return null;
        }
        Matcher matcher = NODE_PATTERN.matcher(label);
        if (!matcher.matches()) {
            return null;
        }
        return NodeParameter.builder().alias(matcher.group("alias")).filter(matcher.group("filter")).build();
    }

}
