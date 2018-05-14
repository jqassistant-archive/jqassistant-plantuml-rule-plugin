package org.jqassistant.contrib.plugin.plantumlrule;

import java.util.Map;
import java.util.Set;

public class StatementBuilder {

    public String create(Map<String, Node> nodes, Map<String, Relationship> relationships) {
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder mergeBuilder = new StringBuilder();
        StringBuilder returnBuilder = new StringBuilder();
        addNodes(nodes, matchBuilder, mergeBuilder, returnBuilder);
        addRelationships(relationships, matchBuilder, mergeBuilder, returnBuilder);
        StringBuilder statement = new StringBuilder();
        statement.append("MATCH");
        newLine(statement);
        statement.append(matchBuilder);
        newLine(statement);
        statement.append(mergeBuilder);
        newLine(statement);
        statement.append("RETURN");
        newLine(statement);
        if (returnBuilder.length() == 0) {
            returnBuilder.append("count(*)");
        }
        statement.append(returnBuilder);
        return statement.toString();
    }

    private void addNodes(Map<String, Node> nodes, StringBuilder matchBuilder, StringBuilder mergeBuilder, StringBuilder returnBuilder) {
        for (Node node : nodes.values()) {
            NodeParameter nodeParameter = node.getNodeParameter();
            String alias = getAlias(node, returnBuilder);
            Set<String> matchLabels = node.getMatchLabels();
            commaNewLine(matchBuilder);
            indent(matchBuilder);
            matchBuilder.append("(");
            matchBuilder.append(alias);
            if (!matchLabels.isEmpty()) {
                addNodeLabels(matchBuilder, matchLabels);
            }
            String filter = nodeParameter.getFilter();
            if (filter != null) {
                matchBuilder.append(filter);
            }
            matchBuilder.append(")");
            Set<String> mergeLabels = node.getMergeLabels();
            if (!mergeLabels.isEmpty()) {
                newLine(mergeBuilder);
                mergeBuilder.append("SET");
                newLine(mergeBuilder);
                indent(mergeBuilder);
                mergeBuilder.append(alias);
                addNodeLabels(mergeBuilder, mergeLabels);
            }
        }
    }

    private void addRelationships(Map<String, Relationship> relationships, StringBuilder matchBuilder, StringBuilder mergeBuilder, StringBuilder returnBuilder) {
        for (Relationship relationship : relationships.values()) {
            RelationshipParameter relationshipParameter = relationship.getRelationshipParameter();
            String alias = relationshipParameter.getAlias();
            addAlias(returnBuilder, alias);
            if (relationship.getMergeType() != null) {
                newLine(mergeBuilder);
                mergeBuilder.append("MERGE");
                newLine(mergeBuilder);
                indent(mergeBuilder);
                addRelationship(relationship, alias, relationship.getMergeType(), null, null, mergeBuilder);
            } else {
                commaNewLine(matchBuilder);
                indent(matchBuilder);
                addRelationship(relationship, alias, relationship.getMatchType(), relationshipParameter.getHops(), relationshipParameter.getFilter(), matchBuilder);
            }
        }
    }

    private String getAlias(Node node, StringBuilder returnBuilder) {
        String alias = node.getNodeParameter().getAlias();
        addAlias(returnBuilder, alias);
        return alias != null ? alias : node.getId();
    }

    private void addAlias(StringBuilder returnBuilder, String alias) {
        if (alias != null) {
            if (returnBuilder.length() == 0) {
                indent(returnBuilder);
            } else {
                comma(returnBuilder);
            }
            returnBuilder.append(alias);
        }
    }

    private void addRelationship(Relationship relationship, String alias, String type, String hops, String filter, StringBuilder builder) {
        addRelationshipNode(relationship.getFrom(), builder);
        builder.append("-[");
        if (alias != null) {
            builder.append(alias);
        }
        if (type != null) {
            builder.append(":").append(type);
        }
        if (hops != null) {
            builder.append(hops);
        }
        if (filter != null) {
            builder.append(filter);
        }
        builder.append("]->");
        addRelationshipNode(relationship.getTo(), builder);
    }

    private void addRelationshipNode(Node node, StringBuilder builder) {
        builder.append('(');
        String toAlias = node.getNodeParameter().getAlias();
        if (toAlias != null) {
            builder.append(toAlias);
        } else {
            builder.append(node.getId());
        }
        builder.append(')');
    }

    private void addNodeLabels(StringBuilder builder, Set<String> labels) {
        for (String label : labels) {
            builder.append(':').append(label);
        }
    }

    private void commaNewLine(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append(',').append("\n");
        }
    }

    private void comma(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append(", ");
        }
    }


    private void newLine(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append("\n");
        }
    }

    private void indent(StringBuilder builder) {
        builder.append("  ");
    }
}
