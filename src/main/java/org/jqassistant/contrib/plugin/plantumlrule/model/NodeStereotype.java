package org.jqassistant.contrib.plugin.plantumlrule.model;

import lombok.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the parameters that are present at a relationship of an entity
 * diagram.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@ToString
public class NodeStereotype {

    public static final Pattern NODE_STEREOTYPE_PATTERN = Pattern.compile("(\\((?<modifier>\\+)\\))?\\s?" + "(?<label>\\w+)?\\s?");

    public static final NodeStereotype DEFAULT = NodeStereotype.builder().build();

    /**
     * The modifier, currently only + is supported.
     */
    private String modifier;

    /**
     * The label to be matched or added.
     */
    private String label;

    public static NodeStereotype of(CharSequence value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = NODE_STEREOTYPE_PATTERN.matcher(value);
        if (!matcher.matches()) {
            return null;
        }
        return NodeStereotype.builder().modifier(matcher.group("modifier")).label(matcher.group("label")).build();
    }

}
