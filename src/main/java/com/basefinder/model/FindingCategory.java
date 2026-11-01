package com.basefinder.model;

/** Broad category of a finding, used for radar grouping, colours and icons. */
public enum FindingCategory {
    BASE("Base candidate"),
    TERRAIN_ANOMALY("Terrain anomaly"),
    STORAGE("Storage"),
    AMETHYST("Amethyst"),
    SPAWNER("Spawner"),
    ENTITY("Entity"),
    PLAYER("Player"),
    REDSTONE("Redstone"),
    PORTAL("Portal"),
    STRUCTURE("Structure"),
    SIGN("Sign"),
    BED("Bed"),
    BEACON("Beacon"),
    LIGHT("Light anomaly"),
    ILLEGAL("Illegal block"),
    HIGHWAY("Nether highway"),
    PATH("Path / breadcrumb"),
    ACTIVITY("Live activity"),
    ENTITY_STACK("Entity stack"),
    REDSTONE_CLOCK("Redstone clock"),
    LIGHTING("Lighting anomaly"),
    SPAWN_PROOF("Spawn-proofing"),
    STASH_ACTIVITY("Stash activity"),
    NETHER_ROOF("Nether roof"),
    HIDDEN_ENTRANCE("Hidden entrance"),
    SECURITY("Security system"),
    MARKER("Marker block"),
    HOLLOW_SHELL("Hollow shell"),
    DECORATION("Decoration"),
    FARM("Farm"),
    ACCESS_SHAFT("Access shaft"),
    PLATFORM("Flat platform"),
    MINING_AREA("Mining area");

    private final String label;

    FindingCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
