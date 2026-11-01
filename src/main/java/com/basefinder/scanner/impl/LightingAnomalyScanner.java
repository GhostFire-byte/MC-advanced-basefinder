package com.basefinder.scanner.impl;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractBlockScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.Blocks;
import com.basefinder.world.Dimension;
import com.basefinder.world.Vec3i;

import java.util.ArrayList;
import java.util.List;

/**
 * Flags lighting that does not match the light a location would naturally have.
 *
 * <p>Rather than a fixed light level, it computes an EXPECTED natural light for
 * each cluster: deep underground (and away from lava/fire/glow) the expected
 * block-light is ~0, so any strong player lighting there is anomalous. Natural
 * emitters are excluded so they never false-positive. Four sub-types (all
 * individually toggleable) are reported:
 * <ul>
 *   <li><b>light in darkness</b> &ndash; player light where it should be pitch black;</li>
 *   <li><b>too bright</b> &ndash; measured light far exceeds the expected baseline;</li>
 *   <li><b>high density</b> &ndash; many light sources packed into a small area;</li>
 *   <li><b>isolated light</b> &ndash; a lone light deep down with no natural source (a marker).</li>
 * </ul>
 */
public final class LightingAnomalyScanner extends AbstractBlockScanner {

    private static final int CLUSTER_RADIUS = 8;
    private static final int UNDERGROUND_Y = 50;
    private static final int NATURAL_PROXIMITY = 8;
    private static final double WEIGHT = 0.5;

    private final List<Lit> lights = new ArrayList<>();
    private final List<Vec3i> naturalEmitters = new ArrayList<>();
    private Dimension dimension = Dimension.UNKNOWN;
    private boolean doDarkness, doBright, doDensity, doIsolated;
    private long tick;

    public LightingAnomalyScanner() {
        super("lighting", "Lighting Anomaly Scanner",
                "Flags light that doesn't match a location's expected natural light: light in the dark, "
                        + "unusually bright or dense clusters, and isolated deep lights.",
                FindingCategory.LIGHTING);
    }

    @Override
    public void begin(ScanContext ctx) {
        lights.clear();
        naturalEmitters.clear();
        dimension = ctx.world().dimension();
        tick = ctx.tick();
        doDarkness = ctx.settings().paramBool("light.darkness", true);
        doBright = ctx.settings().paramBool("light.too_bright", true);
        doDensity = ctx.settings().paramBool("light.density", true);
        doIsolated = ctx.settings().paramBool("light.isolated", true);
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (Blocks.isNaturalLight(id, dimension)) {
            naturalEmitters.add(new Vec3i(x, y, z));
        } else if (Blocks.isPlayerLight(id, dimension)) {
            lights.add(new Lit(new Vec3i(x, y, z), Math.max(light, estimatedEmission(id))));
        }
    }

    @Override
    public void finish(FindingSink sink) {
        for (List<Lit> cluster : clusterLights()) {
            Vec3i center = centroid(cluster);
            if (nearNaturalEmitter(center)) {
                continue; // expected light is naturally high here; not an anomaly
            }
            int count = cluster.size();
            int maxLight = 0;
            long spread = 0;
            for (Lit l : cluster) {
                maxLight = Math.max(maxLight, l.light);
                spread = Math.max(spread, center.distanceSq(l.pos));
            }
            boolean underground = center.y() < UNDERGROUND_Y;
            int footprint = (int) Math.ceil(Math.sqrt(spread));

            // Expected natural block-light here is ~0 (no natural emitter nearby),
            // so the anomaly magnitude is essentially the measured light.
            List<String> reasons = new ArrayList<>();
            int score = 0;
            if (doDarkness && underground) {
                reasons.add("light in darkness");
                score += 35 + maxLight * 2;
            }
            if (doBright && maxLight >= 13 && underground) {
                reasons.add("too bright");
                score += 20;
            }
            if (doDensity && count >= 6 && footprint <= CLUSTER_RADIUS) {
                reasons.add("high density (" + count + ")");
                score += Math.min(30, count * 4);
            }
            if (doIsolated && underground && count <= 2) {
                reasons.add("isolated light");
                score += 15;
            }
            if (reasons.isEmpty() || score <= 0) {
                continue;
            }
            int conf = Math.min(100, score);
            sink.emit(Finding.builder(id(), FindingCategory.LIGHTING, center)
                    .confidence(conf)
                    .signalWeight(WEIGHT)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(conf >= 70 ? Severity.MEDIUM : Severity.LOW)
                    .label("Lighting anomaly: " + String.join(", ", reasons))
                    .source("Expected-light check")
                    .detail(count + " player lights, max level " + maxLight + (underground ? ", underground" : ""))
                    .observedAtTick(tick)
                    .build());
        }
    }

    private List<List<Lit>> clusterLights() {
        List<List<Lit>> clusters = new ArrayList<>();
        boolean[] used = new boolean[lights.size()];
        long r2 = (long) CLUSTER_RADIUS * CLUSTER_RADIUS;
        for (int i = 0; i < lights.size(); i++) {
            if (used[i]) {
                continue;
            }
            List<Lit> group = new ArrayList<>();
            Vec3i a = lights.get(i).pos;
            for (int j = 0; j < lights.size(); j++) {
                if (!used[j] && a.distanceSq(lights.get(j).pos) <= r2) {
                    group.add(lights.get(j));
                    used[j] = true;
                }
            }
            clusters.add(group);
        }
        return clusters;
    }

    private boolean nearNaturalEmitter(Vec3i center) {
        long r2 = (long) NATURAL_PROXIMITY * NATURAL_PROXIMITY;
        for (Vec3i e : naturalEmitters) {
            if (center.distanceSq(e) <= r2) {
                return true;
            }
        }
        return false;
    }

    /** Rough emission level for sources whose own position light may read low in the snapshot. */
    private static int estimatedEmission(String id) {
        return switch (id) {
            case "minecraft:beacon", "minecraft:sea_lantern", "minecraft:glowstone",
                 "minecraft:shroomlight", "minecraft:redstone_lamp", "minecraft:lantern",
                 "minecraft:jack_o_lantern", "minecraft:froglight", "minecraft:ochre_froglight",
                 "minecraft:verdant_froglight", "minecraft:pearlescent_froglight", "minecraft:end_rod" -> 15;
            case "minecraft:torch", "minecraft:wall_torch" -> 14;
            case "minecraft:soul_torch", "minecraft:soul_lantern", "minecraft:soul_wall_torch" -> 10;
            default -> 7;
        };
    }

    private static Vec3i centroid(List<Lit> group) {
        long x = 0, y = 0, z = 0;
        for (Lit l : group) {
            x += l.pos.x();
            y += l.pos.y();
            z += l.pos.z();
        }
        int n = group.size();
        return new Vec3i((int) (x / n), (int) (y / n), (int) (z / n));
    }

    private static final class Lit {
        final Vec3i pos;
        final int light;

        Lit(Vec3i pos, int light) {
            this.pos = pos;
            this.light = light;
        }
    }
}
