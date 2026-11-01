package com.basefinder.guidance;

import com.basefinder.model.BaseCandidate;
import com.basefinder.world.Vec3d;

import java.util.List;
import java.util.Optional;

/**
 * Directional guidance: picks the most relevant visible base candidate (pinned
 * first, then highest fused confidence) and computes a bearing relative to the
 * player's view, so the overlay can draw an arrow/compass pointing to it.
 */
public final class DirectionalGuidance {

    /** A target to point at. {@code relativeBearingDeg} is 0 = straight ahead, +right / -left. */
    public record GuidanceTarget(String candidateId, Vec3d pos, double distance,
                                 double relativeBearingDeg, int confidence) {
    }

    public Optional<GuidanceTarget> compute(List<BaseCandidate> candidates, Vec3d playerPos, float yawDeg, int floor) {
        BaseCandidate best = null;
        for (BaseCandidate c : candidates) {
            if (c.forcedHidden() || !c.visible() || c.fusedConfidence() < floor) {
                continue;
            }
            if (best == null || better(c, best)) {
                best = c;
            }
        }
        if (best == null) {
            return Optional.empty();
        }
        Vec3d target = new Vec3d(best.center().x() + 0.5, best.center().y(), best.center().z() + 0.5);
        double dx = target.x() - playerPos.x();
        double dz = target.z() - playerPos.z();
        double distance = Math.sqrt(dx * dx + dz * dz);
        // Minecraft yaw: 0 = +Z (south), increases clockwise. Bearing to target:
        double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
        double relative = wrap180(targetYaw - yawDeg);
        return Optional.of(new GuidanceTarget(best.id(), target, distance, relative, best.fusedConfidence()));
    }

    private static boolean better(BaseCandidate a, BaseCandidate b) {
        if (a.pinned() != b.pinned()) {
            return a.pinned();
        }
        return a.fusedConfidence() > b.fusedConfidence();
    }

    private static double wrap180(double deg) {
        double d = deg % 360.0;
        if (d > 180) {
            d -= 360;
        } else if (d < -180) {
            d += 360;
        }
        return d;
    }
}
