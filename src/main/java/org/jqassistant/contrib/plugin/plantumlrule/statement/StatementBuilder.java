package org.jqassistant.contrib.plugin.plantumlrule.statement;

import org.jqassistant.contrib.plugin.plantumlrule.model.Node;
import org.jqassistant.contrib.plugin.plantumlrule.model.NodeLabel;
import org.jqassistant.contrib.plugin.plantumlrule.model.Relationship;
import org.jqassistant.contrib.plugin.plantumlrule.model.RelationshipLabel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A builder that creates a {@link Statement} from the given {@link Node}s and {@link Relationship}s.
 */
public class StatementBuilder {

    private Map<String, Node> nodes;
    private Map<String, Relationship> relationships;

    /**
     * Constructor.
     *
     * @param nodes         The {@link Node}s.
     * @param relationships The {@link Relationship}s.
     */
    public StatementBuilder(Map<String, Node> nodes, Map<String, Relationship> relationships) {
        this.nodes = nodes;
        this.relationships = relationships;
    }

    /**
     * Create the {@link Statement}.
     *
     * @return The {@link Statement}.
     */
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
            List<String> matchLabels = node.getStereotypes().stream().filter(stereotype -> stereotype.getModifier() == null).map(stereotype -> stereotype.getLabel()).collect(Collectors.toList());
            List<String> mergeLabels = node.getStereotypes().stream().filter(stereotype -> "+".equals(stereotype.getModifier())).map(stereotype -> stereotype.getLabel()).collect(Collectors.toList());
            NodeLabel nodeLabel = node.getNodeLabel();
            String alias = getAlias(node, returnSegment);
            matchSegment.commaNewLine();
            matchSegment.indent();
            matchSegment.append("(");
            matchSegment.append(alias);
            if (!matchLabels.isEmpty()) {
                addNodeLabels(matchSegment, matchLabels);
            }
            String filter = nodeLabel.getFilter();
            if (filter != null) {
                matchSegment.append(filter);
            }
            matchSegment.append(")");
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
            RelationshipLabel relationshipLabel = relationship.getRelationshipLabel();
            String alias = relationshipLabel.getAlias();
            addAlias(returnSegment, alias);
            String type = relationshipLabel.getType();
            if ("+".equals(relationshipLabel.getModifier())) {
                mergeSegment.newLine();
                mergeSegment.append("MERGE");
                mergeSegment.newLine();
                mergeSegment.indent();
                addRelationship(alias, type, null, null, mergeSegment, relationship.getFrom(), relationship.getTo());
            } else {
                matchSegment.commaNewLine();
                matchSegment.indent();
                addRelationship(alias, type, relationshipLabel.getHops(), relationshipLabel.getFilter(),
                    matchSegment, relationship.getFrom(), relationship.getTo());
            }
        }
    }

    private String getAlias(Node node, Segment returnSegment) {
        String alias = node.getNodeLabel().getAlias();
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

    private void addRelationship(String alias, String type, String hops, String filter, Segment segment, Node from, Node to) {
        addRelationshipNode(from, segment);
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
        addRelationshipNode(to, segment);
    }

    private void addRelationshipNode(Node node, Segment segment) {
        segment.append('(');
        String toAlias = node.getNodeLabel() != null ? node.getNodeLabel().getAlias() : null;
        if (toAlias != null) {
            segment.append(toAlias);
        } else {
            segment.append(node.getId());
        }
        segment.append(')');
    }

    private void addNodeLabels(Segment segment, List<String> labels) {
        for (String label : labels) {
            segment.append(':').append(label);
        }
    }

}
