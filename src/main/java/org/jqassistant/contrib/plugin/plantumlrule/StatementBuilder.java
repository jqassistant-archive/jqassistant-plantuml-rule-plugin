package org.jqassistant.contrib.plugin.plantumlrule;

import java.util.Map;
import java.util.Set;

public class StatementBuilder {

    public String create(Map<String, Node> nodes, Map<String, Relationship> relationships) {
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder mergeBuilder = new StringBuilder();
        StringBuilder returnBuilder = new StringBuilder();
        for (Node node : nodes.values()) {
            EntityParameter entityParameter = node.getEntityParameter();
            String alias = getAlias(entityParameter, returnBuilder);
            Set<String> matchLabels = node.getMatchLabels();
            commaNewLine(matchBuilder);
            indent(matchBuilder);
            matchBuilder.append("(");
            if (alias != null) {
                matchBuilder.append(alias);
            }
            if (!matchLabels.isEmpty()) {
                addNodeLabels(matchBuilder, matchLabels);
                String filter = entityParameter.getFilter();
                if (filter != null) {
                    matchBuilder.append(filter);
                }
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
        for (Relationship relationship : relationships.values()) {
            if (relationship.getMatchType() != null) {
                commaNewLine(matchBuilder);
                indent(matchBuilder);
                addRelationship(relationship, relationship.getMatchType(), matchBuilder);
            }
            if (relationship.getMergeType() != null) {
                newLine(mergeBuilder);
                mergeBuilder.append("MERGE");
                newLine(mergeBuilder);
                indent(mergeBuilder);
                addRelationship(relationship, relationship.getMergeType(), mergeBuilder);
            }
        }
        StringBuilder statement = new StringBuilder();
        statement.append("MATCH");
        newLine(statement);
        statement.append(matchBuilder);
        newLine(statement);
        statement.append(mergeBuilder);
        newLine(statement);
        statement.append("RETURN");
        newLine(statement);
        indent(statement);
        statement.append("*");
        return statement.toString();
    }

    private String getAlias(EntityParameter entity, StringBuilder returnBuilder) {
        String alias = null;
        if (entity.getAlias() != null) {
            alias = entity.getAlias();
            commaNewLine(returnBuilder);
            indent(returnBuilder);
            returnBuilder.append(alias);
        }
        return alias;
    }

    private void addRelationship(Relationship relationship, String type, StringBuilder builder) {
        addRelationshipNode(relationship.getFrom(), builder);
        builder.append("-[").append(relationship.getId()).append(":").append(type).append("]->");
        addRelationshipNode(relationship.getTo(), builder);
    }

    private void addRelationshipNode(Node node, StringBuilder builder) {
        builder.append('(');
        String toAlias = node.getEntityParameter().getAlias();
        if (toAlias != null) {
            builder.append(toAlias);
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

    private void newLine(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append("\n");
        }
    }

    private void indent(StringBuilder builder) {
        builder.append("  ");
    }
}
