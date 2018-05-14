package org.jqassistant.contrib.plugin.plantumlrule;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
class Relationship {

    private String id;

    private Node from;

    private Node to;

    private RelationshipParameter relationshipParameter;

    private String matchType;

    private String mergeType;

}
