package com.basefinder.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * The kind of evidence a finding category represents, used by the upgraded
 * fusion model to decide whether a cluster is a real base. High base confidence
 * requires {@link #HABITATION} or a strong {@link #MARKER}; clusters made only of
 * {@link #ACTIVITY}/{@link #AMBIENT} evidence are treated as activity/mining areas
 * rather than bases.
 */
public enum EvidenceClass {
    /** People live/store here: storage, beds, beacons, crafting, decoration, farms. */
    HABITATION,
    /** Rarely-natural anchor blocks that almost always mark a base. */
    MARKER,
    /** Defensive / alarm systems around a base. */
    SECURITY,
    /** Signs of activity or traversal: light, redstone, paths, mining, platforms. */
    ACTIVITY,
    /** Weak/ambient hints (entities, generic anomalies). */
    AMBIENT;

    private static final Map<FindingCategory, EvidenceClass> MAP = new EnumMap<>(FindingCategory.class);

    static {
        // Habitation
        put(FindingCategory.STORAGE, HABITATION);
        put(FindingCategory.BED, HABITATION);
        put(FindingCategory.BEACON, HABITATION);
        put(FindingCategory.DECORATION, HABITATION);
        put(FindingCategory.FARM, HABITATION);
        put(FindingCategory.ENTITY_STACK, HABITATION);
        put(FindingCategory.STASH_ACTIVITY, HABITATION);
        put(FindingCategory.HOLLOW_SHELL, HABITATION);
        put(FindingCategory.NETHER_ROOF, HABITATION);
        put(FindingCategory.HIDDEN_ENTRANCE, HABITATION);
        put(FindingCategory.SIGN, HABITATION);
        put(FindingCategory.SPAWN_PROOF, HABITATION);

        // Markers
        put(FindingCategory.MARKER, MARKER);
        put(FindingCategory.PORTAL, MARKER);

        // Security
        put(FindingCategory.SECURITY, SECURITY);

        // Activity / traversal
        put(FindingCategory.REDSTONE, ACTIVITY);
        put(FindingCategory.REDSTONE_CLOCK, ACTIVITY);
        put(FindingCategory.LIGHT, ACTIVITY);
        put(FindingCategory.LIGHTING, ACTIVITY);
        put(FindingCategory.ACTIVITY, ACTIVITY);
        put(FindingCategory.PATH, ACTIVITY);
        put(FindingCategory.HIGHWAY, ACTIVITY);
        put(FindingCategory.ACCESS_SHAFT, ACTIVITY);
        put(FindingCategory.PLATFORM, ACTIVITY);
        put(FindingCategory.MINING_AREA, ACTIVITY);
        put(FindingCategory.ILLEGAL, ACTIVITY);

        // Ambient
        put(FindingCategory.TERRAIN_ANOMALY, AMBIENT);
        put(FindingCategory.AMETHYST, AMBIENT);
        put(FindingCategory.SPAWNER, AMBIENT);
        put(FindingCategory.ENTITY, AMBIENT);
        put(FindingCategory.PLAYER, AMBIENT);
        put(FindingCategory.STRUCTURE, AMBIENT);
        put(FindingCategory.BASE, HABITATION);
    }

    private static void put(FindingCategory c, EvidenceClass e) {
        MAP.put(c, e);
    }

    public static EvidenceClass of(FindingCategory category) {
        return MAP.getOrDefault(category, AMBIENT);
    }
}
