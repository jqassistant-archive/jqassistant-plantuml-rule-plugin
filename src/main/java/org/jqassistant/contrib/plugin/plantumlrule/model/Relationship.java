package org.jqassistant.contrib.plugin.plantumlrule.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents a relationship parsed from an entity diagram.
 */
@Builder
@Getter
@ToString
public class Relationship {

    private String id;

    private Node from;

    private Node to;

    private RelationshipLabel relationshipLabel;

//    private String matchType;

//    private String mergeType;

}
