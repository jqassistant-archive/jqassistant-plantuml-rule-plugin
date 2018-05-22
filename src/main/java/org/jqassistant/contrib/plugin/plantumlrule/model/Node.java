package org.jqassistant.contrib.plugin.plantumlrule.model;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

/**
 * Represents a node parsed from an entity diagram.
 */
@Builder
@Getter
@ToString
public class Node {

    private String id;

    private NodeParameter nodeParameter;

    @Singular
    private Set<String> matchLabels;

    @Singular
    private Set<String> mergeLabels;
}
