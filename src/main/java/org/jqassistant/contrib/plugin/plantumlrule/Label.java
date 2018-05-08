package org.jqassistant.contrib.plugin.plantumlrule;

import lombok.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@ToString
public class Label {

    public static final Pattern LABEL_PATTERN = Pattern.compile("(\\w+)?\\s?(\\{.*})?");

    private String alias;

    private String filter;

    public static Label getLabel(CharSequence label) {
        Matcher matcher = LABEL_PATTERN.matcher(label);
        if (!matcher.matches()) {
            return null;
        }
        return Label.builder().alias(matcher.group(1)).filter(matcher.group(2)).build();
    }


}
