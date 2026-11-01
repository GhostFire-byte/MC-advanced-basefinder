package com.basefinder.scanner.impl;

import com.basefinder.world.Vec3i;

import java.util.List;

/** Small shared helpers for scanners (label formatting, centroids). */
final class ScannerText {

    private ScannerText() {
    }

    static String pretty(String id) {
        if (id == null) {
            return "?";
        }
        String s = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        s = s.replace('_', ' ').trim();
        return s.isEmpty() ? id : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static Vec3i centroid(List<Vec3i> pts) {
        long x = 0, y = 0, z = 0;
        for (Vec3i p : pts) {
            x += p.x();
            y += p.y();
            z += p.z();
        }
        int n = Math.max(1, pts.size());
        return new Vec3i((int) (x / n), (int) (y / n), (int) (z / n));
    }
}
