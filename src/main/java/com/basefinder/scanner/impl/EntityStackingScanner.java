package com.basefinder.scanner.impl;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.EntityRef;
import com.basefinder.world.Vec3d;
import com.basefinder.world.Vec3i;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects abnormally stacked entities. Stacked chest-vehicles are hidden storage;
 * stacked mobs or items are farms / lag machines. Counting is per entity type:
 * more than {@value #DEFAULT_THRESHOLD} entities of the SAME type packed within
 * ~1 block flags a stack. Vehicles, mobs and dropped items are considered; which
 * kinds are active is configurable via subsystem params.
 */
public final class EntityStackingScanner extends AbstractScanner {

    private static final int DEFAULT_THRESHOLD = 6; // "more than 5"
    private static final double STACK_RADIUS = 1.5;
    private static final double VEHICLE_WEIGHT = 0.85;
    private static final double MOB_WEIGHT = 0.45;
    private static final double ITEM_WEIGHT = 0.4;

    public EntityStackingScanner() {
        super("entity_stack", "Entity Stacking Scanner",
                "Detects many entities of one type packed into a tiny space: stacked chest-vehicles "
                        + "(hidden storage), stacked mobs or items (farms / lag machines).",
                FindingCategory.ENTITY_STACK);
    }

    @Override
    public void scan(ScanContext ctx, FindingSink sink) {
        int threshold = Math.max(2, ctx.settings().paramInt("stack.threshold", DEFAULT_THRESHOLD));
        boolean doVehicles = ctx.settings().paramBool("stack.vehicles", true);
        boolean doMobs = ctx.settings().paramBool("stack.mobs", true);
        boolean doItems = ctx.settings().paramBool("stack.items", true);

        // Group entities by type, keeping only kinds we care about.
        Map<String, List<EntityRef>> byType = new LinkedHashMap<>();
        for (EntityRef e : ctx.world().entities()) {
            if (!ctx.scope().contains(e.pos().toBlock())) {
                continue;
            }
            Kind kind = classify(e.type());
            if ((kind == Kind.VEHICLE && doVehicles)
                    || (kind == Kind.MOB && doMobs)
                    || (kind == Kind.ITEM && doItems)) {
                byType.computeIfAbsent(e.type(), k -> new ArrayList<>()).add(e);
            }
        }

        for (Map.Entry<String, List<EntityRef>> entry : byType.entrySet()) {
            emitStacks(ctx, entry.getKey(), entry.getValue(), threshold, sink);
        }
    }

    private void emitStacks(ScanContext ctx, String type, List<EntityRef> list, int threshold, FindingSink sink) {
        boolean[] used = new boolean[list.size()];
        double r2 = STACK_RADIUS * STACK_RADIUS;
        for (int i = 0; i < list.size(); i++) {
            if (used[i]) {
                continue;
            }
            List<EntityRef> group = new ArrayList<>();
            Vec3d a = list.get(i).pos();
            for (int j = 0; j < list.size(); j++) {
                if (!used[j] && a.distanceSq(list.get(j).pos()) <= r2) {
                    group.add(list.get(j));
                    used[j] = true;
                }
            }
            if (group.size() >= threshold) {
                Kind kind = classify(type);
                Vec3i center = centroid(group);
                int over = group.size() - threshold;
                int conf = Math.min(100, 70 + over * 5);
                sink.emit(Finding.builder(id(), FindingCategory.ENTITY_STACK, center)
                        .precisePos(new Vec3d(center.x() + 0.5, center.y() + 0.5, center.z() + 0.5))
                        .confidence(conf)
                        .signalWeight(weightFor(kind))
                        .verification(VerificationStatus.VERIFIED)
                        .severity(kind == Kind.VEHICLE ? Severity.HIGH : Severity.MEDIUM)
                        .label(group.size() + "x " + ScannerText.pretty(type) + " stacked")
                        .source("Entity stack")
                        .detail(group.size() + " " + type + " within " + STACK_RADIUS + " blocks")
                        .observedAtTick(ctx.tick())
                        .meta("type", type)
                        .meta("count", String.valueOf(group.size()))
                        .build());
            }
        }
    }

    private enum Kind {VEHICLE, MOB, ITEM, OTHER}

    private static Kind classify(String type) {
        if (type == null) {
            return Kind.OTHER;
        }
        if (type.contains("minecart") || type.contains("boat") || type.contains("raft")) {
            return Kind.VEHICLE;
        }
        if (type.equals("minecraft:item")) {
            return Kind.ITEM;
        }
        if (type.equals("minecraft:player") || type.equals("minecraft:experience_orb")
                || type.equals("minecraft:armor_stand") || type.endsWith("arrow")
                || type.contains("falling_block") || type.contains("area_effect")) {
            return Kind.OTHER;
        }
        return Kind.MOB;
    }

    private static double weightFor(Kind kind) {
        return switch (kind) {
            case VEHICLE -> VEHICLE_WEIGHT;
            case MOB -> MOB_WEIGHT;
            default -> ITEM_WEIGHT;
        };
    }

    private static Vec3i centroid(List<EntityRef> group) {
        double x = 0, y = 0, z = 0;
        for (EntityRef e : group) {
            x += e.pos().x();
            y += e.pos().y();
            z += e.pos().z();
        }
        int n = group.size();
        return new Vec3i((int) Math.floor(x / n), (int) Math.floor(y / n), (int) Math.floor(z / n));
    }
}
