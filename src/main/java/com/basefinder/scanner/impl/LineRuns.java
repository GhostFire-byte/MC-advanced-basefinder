package com.basefinder.scanner.impl;

import com.basefinder.world.Vec3i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Detects long straight axis-aligned runs among block positions (roads, rails, tunnels). */
final class LineRuns {

    private LineRuns() {
    }

    /** Longest consecutive run of positions along X or Z sharing the other two coords. */
    static int longestRun(List<Vec3i> pts) {
        return Math.max(longestAlongX(pts), longestAlongZ(pts));
    }

    private static int longestAlongX(List<Vec3i> pts) {
        Map<Long, TreeSet<Integer>> lines = new HashMap<>();
        for (Vec3i p : pts) {
            long k = (((long) p.y()) << 32) ^ (p.z() & 0xffffffffL);
            lines.computeIfAbsent(k, x -> new TreeSet<>()).add(p.x());
        }
        return maxConsecutive(lines);
    }

    private static int longestAlongZ(List<Vec3i> pts) {
        Map<Long, TreeSet<Integer>> lines = new HashMap<>();
        for (Vec3i p : pts) {
            long k = (((long) p.y()) << 32) ^ (p.x() & 0xffffffffL);
            lines.computeIfAbsent(k, x -> new TreeSet<>()).add(p.z());
        }
        return maxConsecutive(lines);
    }

    private static int maxConsecutive(Map<Long, TreeSet<Integer>> lines) {
        int best = 0;
        for (TreeSet<Integer> line : lines.values()) {
            int run = 0;
            Integer prev = null;
            for (int v : line) {
                if (prev != null && v - prev == 1) {
                    run++;
                } else {
                    run = 1;
                }
                best = Math.max(best, run);
                prev = v;
            }
        }
        return best;
    }
}
