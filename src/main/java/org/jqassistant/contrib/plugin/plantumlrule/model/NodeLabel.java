package org.jqassistant.contrib.plugin.plantumlrule.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.*;

/**
 * Represents the parameters that are present at a {@link Node} of an entity
 * diagram.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@ToString
public class NodeLabel {

    public static final Pattern NODE_LABEL_PATTERN = Pattern.compile("(?<alias>\\w+)?\\s*(?<filter>\\{.*})?");

    public static final NodeLabel DEFAULT = NodeLabel.builder().build();

    /**
     * The alias for the {@link Node}.
     */
    private String alias;

    /**
     * The filter for the {@link Node}, e.g. {name: "AbstractEntity"}.
     */
    private String filter;

    public static NodeLabel of(CharSequence value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = NODE_LABEL_PATTERN.matcher(value);
        if (!matcher.matches()) {
            return null;
        }
        return NodeLabel.builder().alias(matcher.group("alias")).filter(matcher.group("filter")).build();
    }

}
