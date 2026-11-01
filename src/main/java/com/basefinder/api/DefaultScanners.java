package com.basefinder.api;

import com.basefinder.scanner.ScannerRegistry;
import com.basefinder.scanner.impl.AccessShaftScanner;
import com.basefinder.scanner.impl.AmethystScanner;
import com.basefinder.scanner.impl.BeaconScanner;
import com.basefinder.scanner.impl.BedScanner;
import com.basefinder.scanner.impl.BranchMineScanner;
import com.basefinder.scanner.impl.BreadcrumbScanner;
import com.basefinder.scanner.impl.DecorationScanner;
import com.basefinder.scanner.impl.EntityScanner;
import com.basefinder.scanner.impl.EntityStackingScanner;
import com.basefinder.scanner.impl.FlatPlatformScanner;
import com.basefinder.scanner.impl.FoodFarmScanner;
import com.basefinder.scanner.impl.HiddenEntranceScanner;
import com.basefinder.scanner.impl.HollowShellScanner;
import com.basefinder.scanner.impl.IllegalBlockScanner;
import com.basefinder.scanner.impl.LidarLightScanner;
import com.basefinder.scanner.impl.LightingAnomalyScanner;
import com.basefinder.scanner.impl.MarkerScanner;
import com.basefinder.scanner.impl.NetherHighwayScanner;
import com.basefinder.scanner.impl.NetherRoofScanner;
import com.basefinder.scanner.impl.PortalScanner;
import com.basefinder.scanner.impl.RealtimeActivityScanner;
import com.basefinder.scanner.impl.RedstoneFrequencyScanner;
import com.basefinder.scanner.impl.RedstoneScanner;
import com.basefinder.scanner.impl.SecurityScanner;
import com.basefinder.scanner.impl.SignScanner;
import com.basefinder.scanner.impl.SpawnProofScanner;
import com.basefinder.scanner.impl.SpawnerScanner;
import com.basefinder.scanner.impl.StashActivityScanner;
import com.basefinder.scanner.impl.StorageScanner;
import com.basefinder.scanner.impl.StructureScanner;
import com.basefinder.scanner.impl.TerrainAnomalyScanner;

/** Registers the full v1 scanner suite. */
public final class DefaultScanners {

    private DefaultScanners() {
    }

    public static void registerAll(ScannerRegistry registry) {
        registry.register(new TerrainAnomalyScanner());
        registry.register(new StorageScanner());
        registry.register(new AmethystScanner());
        registry.register(new SpawnerScanner());
        registry.register(new EntityScanner());
        registry.register(new RedstoneScanner());
        registry.register(new PortalScanner());
        registry.register(new StructureScanner());
        registry.register(new SignScanner());
        registry.register(new BedScanner());
        registry.register(new BeaconScanner());
        registry.register(new LidarLightScanner());
        registry.register(new IllegalBlockScanner());
        registry.register(new NetherHighwayScanner());
        registry.register(new BreadcrumbScanner());
        registry.register(new RealtimeActivityScanner());
        registry.register(new EntityStackingScanner());
        registry.register(new RedstoneFrequencyScanner());
        registry.register(new LightingAnomalyScanner());
        registry.register(new MarkerScanner());
        registry.register(new SpawnProofScanner());
        registry.register(new NetherRoofScanner());
        registry.register(new SecurityScanner());
        registry.register(new DecorationScanner());
        registry.register(new FoodFarmScanner());
        registry.register(new AccessShaftScanner());
        registry.register(new FlatPlatformScanner());
        registry.register(new BranchMineScanner());
        registry.register(new HollowShellScanner());
        registry.register(new HiddenEntranceScanner());
        registry.register(new StashActivityScanner());
    }
}
