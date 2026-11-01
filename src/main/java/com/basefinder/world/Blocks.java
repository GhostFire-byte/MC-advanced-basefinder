package com.basefinder.world;

import java.util.Set;

/**
 * Catalog of Minecraft block/entity registry ids grouped by meaning. Centralising
 * this keeps every scanner free of Minecraft classes and free of scattered magic
 * strings. Ids use the deobfuscated {@code minecraft:} registry names shipped
 * from 26.1 onward. Matching is by exact id or by suffix family where noted.
 */
public final class Blocks {

    private Blocks() {
    }

    /** Containers and storage block entities. */
    public static final Set<String> STORAGE = Set.of(
            "minecraft:chest", "minecraft:trapped_chest", "minecraft:ender_chest",
            "minecraft:barrel", "minecraft:hopper", "minecraft:dispenser", "minecraft:dropper",
            "minecraft:furnace", "minecraft:blast_furnace", "minecraft:smoker",
            "minecraft:brewing_stand", "minecraft:enchanting_table", "minecraft:anvil",
            "minecraft:chipped_anvil", "minecraft:damaged_anvil");

    /** Shulker boxes come in 17 colour variants; matched by suffix. */
    public static boolean isShulkerBox(String id) {
        return id != null && id.endsWith("shulker_box");
    }

    public static boolean isStorage(String id) {
        return id != null && (STORAGE.contains(id) || isShulkerBox(id));
    }

    public static final Set<String> SPAWNERS = Set.of(
            "minecraft:spawner", "minecraft:trial_spawner");

    public static final Set<String> AMETHYST = Set.of(
            "minecraft:budding_amethyst", "minecraft:amethyst_cluster",
            "minecraft:large_amethyst_bud", "minecraft:medium_amethyst_bud",
            "minecraft:small_amethyst_bud", "minecraft:amethyst_block");

    public static final Set<String> BEDS_FAMILY_SUFFIX = Set.of("_bed");

    public static boolean isBed(String id) {
        return id != null && id.endsWith("_bed");
    }

    public static boolean isRespawnPoint(String id) {
        return isBed(id) || "minecraft:respawn_anchor".equals(id);
    }

    public static final Set<String> BEACON = Set.of("minecraft:beacon");

    public static final Set<String> PORTAL = Set.of(
            "minecraft:nether_portal", "minecraft:end_portal",
            "minecraft:end_gateway", "minecraft:end_portal_frame");

    /** Light-emitting blocks the LiDAR scanner keys on. */
    public static final Set<String> LIGHT_SOURCES = Set.of(
            "minecraft:torch", "minecraft:wall_torch", "minecraft:soul_torch",
            "minecraft:soul_wall_torch", "minecraft:lantern", "minecraft:soul_lantern",
            "minecraft:sea_lantern", "minecraft:glowstone", "minecraft:shroomlight",
            "minecraft:jack_o_lantern", "minecraft:redstone_lamp", "minecraft:froglight",
            "minecraft:ochre_froglight", "minecraft:verdant_froglight", "minecraft:pearlescent_froglight",
            "minecraft:end_rod", "minecraft:lava", "minecraft:campfire", "minecraft:soul_campfire");

    /** Blocks unobtainable/unplaceable in normal survival (anarchy markers). */
    public static final Set<String> ILLEGAL = Set.of(
            "minecraft:command_block", "minecraft:chain_command_block", "minecraft:repeating_command_block",
            "minecraft:barrier", "minecraft:structure_block", "minecraft:structure_void",
            "minecraft:jigsaw", "minecraft:light", "minecraft:debug_stick",
            "minecraft:petrified_oak_slab", "minecraft:infested_stone", "minecraft:infested_cobblestone",
            "minecraft:command_block_minecart");

    /** Redstone / contraption components. */
    public static final Set<String> REDSTONE = Set.of(
            "minecraft:hopper", "minecraft:dispenser", "minecraft:dropper", "minecraft:piston",
            "minecraft:sticky_piston", "minecraft:observer", "minecraft:comparator",
            "minecraft:repeater", "minecraft:redstone_wire", "minecraft:redstone_block",
            "minecraft:redstone_torch", "minecraft:lever", "minecraft:note_block", "minecraft:crafter");

    /** Fluids (source/flowing handled by adapter via id). */
    public static final Set<String> FLUIDS = Set.of("minecraft:water", "minecraft:lava");

    /** Ice-road materials used by the nether-highway detector. */
    public static final Set<String> ICE_ROAD = Set.of(
            "minecraft:ice", "minecraft:packed_ice", "minecraft:blue_ice", "minecraft:frosted_ice");

    /** Rarely-natural anchor/marker blocks that almost always mark a base. */
    public static final Set<String> MARKERS = Set.of(
            "minecraft:lodestone", "minecraft:conduit", "minecraft:respawn_anchor",
            "minecraft:dragon_egg");

    /** Base-security / alarm blocks. */
    public static final Set<String> SECURITY = Set.of(
            "minecraft:sculk_sensor", "minecraft:calibrated_sculk_sensor", "minecraft:tripwire",
            "minecraft:tripwire_hook", "minecraft:target", "minecraft:observer");

    /** Deep-dark blocks that ARE natural (so sculk is only suspicious outside the deep dark). */
    public static final Set<String> SCULK_NATURAL = Set.of(
            "minecraft:sculk", "minecraft:sculk_vein", "minecraft:sculk_catalyst", "minecraft:sculk_shrieker");

    /** Decorative blocks that cluster inside inhabited builds. */
    public static boolean isDecoration(String id) {
        if (id == null) {
            return false;
        }
        return id.equals("minecraft:item_frame") || id.equals("minecraft:glow_item_frame")
                || id.equals("minecraft:painting") || id.equals("minecraft:flower_pot")
                || id.equals("minecraft:armor_stand") || id.equals("minecraft:bookshelf")
                || id.equals("minecraft:chiseled_bookshelf") || id.equals("minecraft:lectern")
                || id.equals("minecraft:item_frame") || id.endsWith("_banner")
                || id.endsWith("_candle") || id.equals("minecraft:decorated_pot");
    }

    /** Farmland / crop / food-production blocks. */
    public static boolean isFarmBlock(String id) {
        if (id == null) {
            return false;
        }
        return id.equals("minecraft:farmland") || id.equals("minecraft:wheat")
                || id.equals("minecraft:carrots") || id.equals("minecraft:potatoes")
                || id.equals("minecraft:beetroots") || id.equals("minecraft:composter")
                || id.equals("minecraft:melon_stem") || id.equals("minecraft:pumpkin_stem")
                || id.equals("minecraft:sweet_berry_bush") || id.equals("minecraft:nether_wart")
                || id.equals("minecraft:pitcher_crop") || id.equals("minecraft:torchflower_crop");
    }

    /** Vertical access blocks (shafts/elevators). */
    public static boolean isAccessBlock(String id) {
        if (id == null) {
            return false;
        }
        return id.equals("minecraft:scaffolding") || id.equals("minecraft:ladder")
                || id.equals("minecraft:vine") || id.equals("minecraft:twisting_vines")
                || id.equals("minecraft:water") || id.equals("minecraft:soul_sand")
                || id.equals("minecraft:bubble_column");
    }

    /** Slab/carpet types used for spawn-proofing floors. */
    public static boolean isSpawnProofCover(String id) {
        if (id == null) {
            return false;
        }
        return id.endsWith("_slab") || id.endsWith("_carpet") || id.equals("minecraft:carpet")
                || id.endsWith("_trapdoor") || id.equals("minecraft:snow");
    }

    /** Pistons (used by the hidden-entrance detector). */
    public static boolean isPiston(String id) {
        return id != null && (id.equals("minecraft:piston") || id.equals("minecraft:sticky_piston")
                || id.equals("minecraft:moving_piston"));
    }

    /** Gold-farm signature materials (common on the nether roof). */
    public static boolean isGoldFarmMaterial(String id) {
        if (id == null) {
            return false;
        }
        return id.equals("minecraft:magma_block") || id.endsWith("_trapdoor")
                || id.equals("minecraft:hopper") || id.equals("minecraft:water")
                || id.endsWith("_slab") || id.equals("minecraft:soul_sand");
    }

    public static boolean isSign(String id) {
        return id != null && (id.endsWith("_sign") || id.endsWith("_hanging_sign"));
    }

    public static boolean isRail(String id) {
        return id != null && id.endsWith("rail");
    }

    /** Light sources that occur naturally, so the lighting scanner can exclude them. */
    private static final Set<String> NATURAL_LIGHT_OVERWORLD = Set.of(
            "minecraft:lava", "minecraft:fire", "minecraft:magma_block", "minecraft:glow_lichen");
    private static final Set<String> NATURAL_LIGHT_NETHER = Set.of(
            "minecraft:lava", "minecraft:fire", "minecraft:soul_fire", "minecraft:magma_block",
            "minecraft:glowstone", "minecraft:shroomlight", "minecraft:glow_lichen");

    public static boolean isNaturalLight(String id, Dimension dim) {
        if (id == null) {
            return false;
        }
        return switch (dim) {
            case NETHER -> NATURAL_LIGHT_NETHER.contains(id);
            default -> NATURAL_LIGHT_OVERWORLD.contains(id);
        };
    }

    /** A player-placed light source (a light emitter that is not natural for the dimension). */
    public static boolean isPlayerLight(String id, Dimension dim) {
        return LIGHT_SOURCES.contains(id) && !isNaturalLight(id, dim);
    }

    public static boolean isFluid(String id) {
        return FLUIDS.contains(id);
    }

    private static final Set<String> OVERWORLD_FOREIGN = Set.of(
            "minecraft:netherrack", "minecraft:soul_sand", "minecraft:soul_soil",
            "minecraft:nether_bricks", "minecraft:blackstone", "minecraft:basalt",
            "minecraft:crimson_nylium", "minecraft:warped_nylium", "minecraft:end_stone",
            "minecraft:purpur_block", "minecraft:crying_obsidian", "minecraft:magma_block");

    private static final Set<String> NETHER_FOREIGN = Set.of(
            "minecraft:grass_block", "minecraft:dirt", "minecraft:water", "minecraft:ice",
            "minecraft:packed_ice", "minecraft:blue_ice", "minecraft:sand", "minecraft:oak_log");

    /** A block clearly out of place for its dimension (strong player/transport signal). */
    public static boolean isForeignMaterial(String id, Dimension dim) {
        if (id == null) {
            return false;
        }
        return switch (dim) {
            case OVERWORLD -> OVERWORLD_FOREIGN.contains(id);
            case NETHER -> NETHER_FOREIGN.contains(id);
            default -> false;
        };
    }

    private static final Set<String> REFINED_EXACT = Set.of(
            "minecraft:crafting_table", "minecraft:glass", "minecraft:cobblestone",
            "minecraft:bookshelf", "minecraft:ladder", "minecraft:scaffolding",
            "minecraft:iron_bars", "minecraft:glowstone", "minecraft:smooth_stone",
            "minecraft:bricks", "minecraft:chiseled_bookshelf");

    private static final String[] REFINED_SUFFIXES = {
            "_planks", "_concrete", "concrete_powder", "_wool", "_carpet", "_glass",
            "_glass_pane", "_terracotta", "_slab", "_stairs", "_fence", "_fence_gate",
            "_wall", "_door", "_trapdoor", "_bricks", "_brick"};

    /**
     * Refined / crafted materials that natural world generation never places,
     * used as a strong "a player was here" signal by the terrain scanner.
     * (Obsidian and cobblestone can occur naturally near lava, so obsidian is
     * intentionally excluded here and handled by the material-mix detector.)
     */
    public static boolean isRefinedPlayerBlock(String id) {
        if (id == null) {
            return false;
        }
        if (REFINED_EXACT.contains(id)) {
            return true;
        }
        if (id.startsWith("minecraft:stripped_") && id.endsWith("_log")) {
            return true;
        }
        for (String suffix : REFINED_SUFFIXES) {
            if (id.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
}
