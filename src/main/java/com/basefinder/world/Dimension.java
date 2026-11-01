package com.basefinder.world;

/** The dimension a snapshot was taken in, plus the overworld/nether coordinate scale. */
public enum Dimension {
    OVERWORLD("minecraft:overworld", 1.0),
    NETHER("minecraft:the_nether", 8.0),
    END("minecraft:the_end", 1.0),
    UNKNOWN("unknown", 1.0);

    private final String id;
    private final double horizontalScale;

    Dimension(String id, double horizontalScale) {
        this.id = id;
        this.horizontalScale = horizontalScale;
    }

    public String id() {
        return id;
    }

    /** Overworld blocks per block in this dimension (8 in the Nether). */
    public double horizontalScale() {
        return horizontalScale;
    }

    public static Dimension fromId(String id) {
        if (id == null) {
            return UNKNOWN;
        }
        for (Dimension d : values()) {
            if (d.id.equals(id)) {
                return d;
            }
        }
        return UNKNOWN;
    }
}
