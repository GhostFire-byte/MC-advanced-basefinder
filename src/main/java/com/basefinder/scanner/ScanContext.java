package com.basefinder.scanner;

import com.basefinder.config.GlobalSettings;
import com.basefinder.config.SubsystemSettings;
import com.basefinder.engine.VillageContext;
import com.basefinder.world.ScanScope;
import com.basefinder.world.Vec3d;
import com.basefinder.world.WorldSnapshot;

/** Everything a scanner needs for one pass. Immutable and thread-confined per scan. */
public record ScanContext(
        WorldSnapshot world,
        ScanScope scope,
        GlobalSettings global,
        SubsystemSettings settings,
        long tick,
        ScanBudget budget,
        VillageContext village) {

    public Vec3d playerPos() {
        return world.playerPos();
    }

    /** Effective confidence floor for the owning scanner (stricter of global/override). */
    public int effectiveFloor() {
        return settings.effectiveFloor(global.confidenceFloor());
    }
}
