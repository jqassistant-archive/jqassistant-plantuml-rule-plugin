package org.jqassistant.contrib.plugin.plantumlrule.statement;

/**
 * Represents a segment of a {@link Statement}.
 */
public class Segment {

    private StringBuilder builder = new StringBuilder();

    public Segment append(String value) {
        builder.append(value);
        return this;
    }

    public Segment append(char value) {
        builder.append(value);
        return this;
    }

    public Segment commaNewLine() {
        if (builder.length() > 0) {
            builder.append(',').append("\n");
        }
        return this;
    }

    public Segment comma() {
        if (builder.length() > 0) {
            builder.append(", ");
        }
        return this;
    }

    public Segment newLine() {
        if (builder.length() > 0) {
            builder.append("\n");
        }
        return this;
    }

    public Segment indent() {
        builder.append("  ");
        return this;
    }

    public boolean isEmpty() {
        return builder.length() == 0;
    }

    public String get() {
        return builder.toString();
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
