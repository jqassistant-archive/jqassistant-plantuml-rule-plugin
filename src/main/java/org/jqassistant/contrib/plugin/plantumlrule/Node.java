package org.jqassistant.contrib.plugin.plantumlrule;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.util.Set;

@Builder
@Getter
@ToString
class Node {

    private String id;

    private NodeParameter nodeParameter;

    @Singular
    private Set<String> matchLabels;

    @Singular
    private Set<String> mergeLabels;
}
