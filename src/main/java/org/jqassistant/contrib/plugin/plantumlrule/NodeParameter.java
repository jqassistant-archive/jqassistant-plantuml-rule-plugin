package org.jqassistant.contrib.plugin.plantumlrule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@ToString
public class NodeParameter {

    public static final Pattern NODE_PATTERN = Pattern.compile("(?<alias>\\w+)?\\s?(?<filter>\\{.*})?");

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
