package org.jqassistant.contrib.plugin.plantumlrule;

import net.sourceforge.plantuml.cucadiagram.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public class CucaDiagramParser {

    private Map<String, Node> nodes;

    private Map<String, Relationship> relationships;

    public CucaDiagramParser(CucaDiagram diagram) {
        this.nodes = unmodifiableMap(getNodes(diagram));
        this.relationships = unmodifiableMap(getRelationships(diagram, nodes));
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public Map<String, Relationship> getRelationships() {
        return relationships;
    }

    private Map<String, Node> getNodes(CucaDiagram diagram) {
        IGroup rootGroup = diagram.getRootGroup();
        return getNodes(rootGroup);
    }

    private Map<String, Node> getNodes(IGroup group) {
        Map<String, Node> nodes = new LinkedHashMap<>();
        for (IGroup child: group.getChildren()) {
            addNode(child, nodes);
            nodes.putAll(getNodes(child));
        }
        for (ILeaf leaf : group.getLeafsDirect()) {
            addNode(leaf, nodes);
        }
        return nodes;
    }

    private void addNode(IEntity entity, Map<String, Node> nodes) {
        Node.NodeBuilder nodeBuilder = Node.builder().id(entity.getUid());
        for (String stereoType : entity.getStereotype().getMultipleLabels()) {
            String label = stereoType.trim();
            if (label.startsWith("+")) {
                nodeBuilder.mergeLabel(label.substring(1));
            } else {
                nodeBuilder.matchLabel(label);
            }
        }
        nodeBuilder.nodeParameter(getNodeParameter(entity.getDisplay()));
        Node node = nodeBuilder.build();
        nodes.put(node.getId(), node);
    }

    private Map<String, Relationship> getRelationships(CucaDiagram diagram, Map<String, Node> nodes) {
        LinkedHashMap<String, Relationship> relationships = new LinkedHashMap<>();
        for (Link link : diagram.getLinks()) {
            Relationship.RelationshipBuilder builder = Relationship.builder().id(link.getUid().toLowerCase());
            Display display = link.getLabel();
            RelationshipParameter relationshipParameter = getRelationshipParameter(display);
            builder.relationshipParameter(relationshipParameter);
            String relationType = relationshipParameter.getType();
            if (relationType != null) {
                if (relationType.startsWith("+")) {
                    builder.mergeType(relationType.substring(1));
                } else {
                    builder.matchType(relationType);
                }
            }
            Node entity1 = nodes.get(link.getEntity1().getUid());
            Node entity2 = nodes.get(link.getEntity2().getUid());
            if (link.isInverted()) {
                builder.from(entity2);
                builder.to(entity1);
            } else {
                builder.from(entity1);
                builder.to(entity2);
            }
            Relationship relationship = builder.build();
            relationships.put(relationship.getId(), relationship);
        }
        return relationships;
    }

    private NodeParameter getNodeParameter(Display display) {
        for (CharSequence charSequence : display) {
            NodeParameter nodeParameter = NodeParameter.getNodeParameter(charSequence);
            if (nodeParameter != null) {
                return nodeParameter;
            }
        }
        return NodeParameter.DEFAULT;
    }

    private RelationshipParameter getRelationshipParameter(Display display) {
        for (CharSequence charSequence : display) {
            RelationshipParameter relationshipParameter = RelationshipParameter.getRelationshipParameter(charSequence);
            if (relationshipParameter != null) {
                return relationshipParameter;
            }
        }
        return RelationshipParameter.DEFAULT;
    }

}
