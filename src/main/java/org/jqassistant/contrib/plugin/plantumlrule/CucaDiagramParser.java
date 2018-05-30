package org.jqassistant.contrib.plugin.plantumlrule;

import static java.util.Collections.unmodifiableMap;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sourceforge.plantuml.cucadiagram.*;
import org.jqassistant.contrib.plugin.plantumlrule.model.Node;
import org.jqassistant.contrib.plugin.plantumlrule.model.NodeParameter;
import org.jqassistant.contrib.plugin.plantumlrule.model.Relationship;
import org.jqassistant.contrib.plugin.plantumlrule.model.RelationshipParameter;

/**
 * Parses {@link CucaDiagram}s consisting of entities and relations
 */
public class CucaDiagramParser {

    private Map<String, Node> nodes;

    private Map<String, Relationship> relationships;

    /**
     * Constructor.
     *
     * @param diagram The {@link CucaDiagram} to parse.
     */
    public CucaDiagramParser(CucaDiagram diagram) {
        this.nodes = unmodifiableMap(getNodes(diagram.getRootGroup()));
        this.relationships = unmodifiableMap(getRelationships(diagram, nodes));
    }

    /**
     * Return the {@link Node}s of the diagram.
     *
     * @return The {@link Node}.
     */
    public Map<String, Node> getNodes() {
        return nodes;
    }

    /**
     * Return the {@link Relationship}s of the diagram.
     *
     * @return The {@link Relationship}s.
     */
    public Map<String, Relationship> getRelationships() {
        return relationships;
    }

    /**
     * Extracts the {@link Node}s of the given {@link IGroup}.
     *
     * <p>
     * Any nested groups are resolved recursively.
     * </p>
     *
     * @param group The {@link IGroup}.
     * @return The contained {@link Node}s.
     */
    private Map<String, Node> getNodes(IGroup group) {
        Map<String, Node> nodes = new LinkedHashMap<>();
        for (IGroup child : group.getChildren()) {
            addNode(child, nodes);
            nodes.putAll(getNodes(child));
        }
        for (ILeaf leaf : group.getLeafsDirect()) {
            addNode(leaf, nodes);
        }
        return nodes;
    }

    /**
     * Add a {@link Node} from the diagram..
     *
     * @param entity The {@link IEntity} representing the {@link Node}.
     * @param nodes  The {@link Map} of {@link Node}s.
     */
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

    /**
     * Get the {@link Relationship}s specified by the diagram.
     *
     * @param diagram The {@link CucaDiagram}.
     * @param nodes   The {@link Node}s of the diagram.
     * @return The {@link Map} of {@link Relationship}s.
     */
    private Map<String, Relationship> getRelationships(CucaDiagram diagram, Map<String, Node> nodes) {
        LinkedHashMap<String, Relationship> relationships = new LinkedHashMap<>();
        for (Link link : diagram.getLinks()) {
            Relationship.RelationshipBuilder builder = Relationship.builder().id(link.getUid().toLowerCase());
            Display display = link.getLabel();
            RelationshipParameter relationshipParameter = getRelationshipParameter(display);
            builder.relationshipParameter(relationshipParameter);
            String relationType = relationshipParameter.getType();
            if (relationType != null) {
                if ("+".equals(relationshipParameter.getModifier())) {
                    builder.mergeType(relationType);
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

    /**
     * Extract {@link NodeParameter} from the {@link Display}.
     *
     * @param display The {@link Display}.
     * @return The {@link NodeParameter}.
     */
    private NodeParameter getNodeParameter(Display display) {
        for (CharSequence charSequence : display) {
            NodeParameter nodeParameter = NodeParameter.getNodeParameter(charSequence);
            if (nodeParameter != null) {
                return nodeParameter;
            }
        }
        return NodeParameter.DEFAULT;
    }

    /**
     * Extract the {@link RelationshipParameter} from thi given {@link Display}.
     *
     * @param display The {@link Display}.
     * @return The {@link RelationshipParameter}.
     */
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
