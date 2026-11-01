package com.basefinder.scanner.impl;

import com.basefinder.world.Vec3i;

import java.util.ArrayList;
import java.util.List;

/** Simple radius-based greedy clustering of block positions. */
final class Clustering {

    private Clustering() {
    }

    record Cluster(Vec3i center, int count) {
    }

    static List<Cluster> cluster(List<Vec3i> points, int radius) {
        List<Cluster> out = new ArrayList<>();
        boolean[] used = new boolean[points.size()];
        long r2 = (long) radius * radius;
        for (int i = 0; i < points.size(); i++) {
            if (used[i]) {
                continue;
            }
            List<Vec3i> group = new ArrayList<>();
            Vec3i a = points.get(i);
            for (int j = 0; j < points.size(); j++) {
                if (!used[j] && a.distanceSq(points.get(j)) <= r2) {
                    group.add(points.get(j));
                    used[j] = true;
                }
            }
            out.add(new Cluster(ScannerText.centroid(group), group.size()));
        }
        return out;
    }
}
