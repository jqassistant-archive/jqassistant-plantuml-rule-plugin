package org.jqassistant.contrib.plugin.plantumlrule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@ToString
public class EntityParameter {

    public static final Pattern LABEL_PATTERN = Pattern.compile("(\\w+)?\\s?(\\{.*})?");

    private String alias;

    private String filter;

    public static EntityParameter getEntityParameter(CharSequence label) {
        if (label == null) {
            return null;
        }
        Matcher matcher = LABEL_PATTERN.matcher(label);
        if (!matcher.matches()) {
            return null;
        }
        return EntityParameter.builder().alias(matcher.group(1)).filter(matcher.group(2)).build();
    }

}
