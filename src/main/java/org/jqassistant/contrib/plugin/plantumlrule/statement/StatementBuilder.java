package org.jqassistant.contrib.plugin.plantumlrule.statement;

import java.util.Map;
import java.util.Set;

import org.jqassistant.contrib.plugin.plantumlrule.model.Node;
import org.jqassistant.contrib.plugin.plantumlrule.model.NodeParameter;
import org.jqassistant.contrib.plugin.plantumlrule.model.Relationship;
import org.jqassistant.contrib.plugin.plantumlrule.model.RelationshipParameter;

public class StatementBuilder {

    private Map<String, Node> nodes;
    private Map<String, Relationship> relationships;

    /**
     * Constructor.
     *
     * @param nodes
     *            The {@link Node}s.
     * @param relationships
     *            The {@link Relationship}s.
     */
    public StatementBuilder(Map<String, Node> nodes, Map<String, Relationship> relationships) {
        this.nodes = nodes;
        this.relationships = relationships;
    }

    public Statement create() {
        Segment matchSegment = new Segment();
        Segment mergeSegment = new Segment();
        Segment returnSegment = new Segment();
        addNodes(nodes, matchSegment, mergeSegment, returnSegment);
        addRelationships(relationships, matchSegment, mergeSegment, returnSegment);
        return Statement.builder().matchSegment(matchSegment).mergeSegment(mergeSegment).returnSegment(returnSegment).build();
    }

    private void addNodes(Map<String, Node> nodes, Segment matchSegment, Segment mergeSegment, Segment returnSegment) {
        for (Node node : nodes.values()) {
            NodeParameter nodeParameter = node.getNodeParameter();
            String alias = getAlias(node, returnSegment);
            Set<String> matchLabels = node.getMatchLabels();
            matchSegment.commaNewLine();
            matchSegment.indent();
            matchSegment.append("(");
            matchSegment.append(alias);
            if (!matchLabels.isEmpty()) {
                addNodeLabels(matchSegment, matchLabels);
            }
            String filter = nodeParameter.getFilter();
            if (filter != null) {
                matchSegment.append(filter);
            }
            matchSegment.append(")");
            Set<String> mergeLabels = node.getMergeLabels();
            if (!mergeLabels.isEmpty()) {
                mergeSegment.newLine();
                mergeSegment.append("SET");
                mergeSegment.newLine();
                mergeSegment.indent();
                mergeSegment.append(alias);
                addNodeLabels(mergeSegment, mergeLabels);
            }
        }
    }

    private void addRelationships(Map<String, Relationship> relationships, Segment matchSegment, Segment mergeSegment, Segment returnSegment) {
        for (Relationship relationship : relationships.values()) {
            RelationshipParameter relationshipParameter = relationship.getRelationshipParameter();
            String alias = relationshipParameter.getAlias();
            addAlias(returnSegment, alias);
            if (relationship.getMergeType() != null) {
                mergeSegment.newLine();
                mergeSegment.append("MERGE");
                mergeSegment.newLine();
                mergeSegment.indent();
                addRelationship(relationship, alias, relationship.getMergeType(), null, null, mergeSegment);
            } else {
                matchSegment.commaNewLine();
                matchSegment.indent();
                addRelationship(relationship, alias, relationship.getMatchType(), relationshipParameter.getHops(), relationshipParameter.getFilter(),
                        matchSegment);
            }
        }
    }

    private String getAlias(Node node, Segment returnSegment) {
        String alias = node.getNodeParameter().getAlias();
        addAlias(returnSegment, alias);
        return alias != null ? alias : node.getId();
    }

    private void addAlias(Segment returnSegment, String alias) {
        if (alias != null) {
            if (returnSegment.isEmpty()) {
                returnSegment.indent();
            } else {
                returnSegment.comma();
            }
            returnSegment.append(alias);
        }
    }

    private void addRelationship(Relationship relationship, String alias, String type, String hops, String filter, Segment segment) {
        addRelationshipNode(relationship.getFrom(), segment);
        segment.append("-[");
        if (alias != null) {
            segment.append(alias);
        }
        if (type != null) {
            segment.append(":").append(type);
        }
        if (hops != null) {
            segment.append(hops);
        }
        if (filter != null) {
            segment.append(filter);
        }
        segment.append("]->");
        addRelationshipNode(relationship.getTo(), segment);
    }

    private void addRelationshipNode(Node node, Segment segment) {
        segment.append('(');
        String toAlias = node.getNodeParameter().getAlias();
        if (toAlias != null) {
            segment.append(toAlias);
        } else {
            segment.append(node.getId());
        }
        segment.append(')');
    }

    private void addNodeLabels(Segment segment, Set<String> labels) {
        for (String label : labels) {
            segment.append(':').append(label);
        }
    }

}
