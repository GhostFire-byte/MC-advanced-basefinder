package com.basefinder.ui;

import com.basefinder.config.GlobalSettings;
import com.basefinder.diag.LogEntry;
import com.basefinder.engine.EngineMetrics;
import com.basefinder.guidance.DirectionalGuidance.GuidanceTarget;
import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Finding;
import com.basefinder.notify.Notification;
import com.basefinder.rules.Rule;
import com.basefinder.ui.theme.ThemeManager;
import com.basefinder.world.Vec3d;

import java.util.List;
import java.util.Optional;

/**
 * Read seam the UI draws from. The bundled overlay reads only this, so a host
 * client can supply its own implementation and reuse the whole overlay, or ignore
 * it entirely and read these same values to drive its own GUI.
 */
public interface UiModel {

    boolean engineRunning();

    boolean overlayEnabled();

    GlobalSettings globals();

    ThemeManager theme();

    /** Candidates at/above the confidence floor and visible (not force-hidden). */
    List<BaseCandidate> visibleCandidates();

    /** Every raw finding this cycle (for the Findings panel), regardless of threshold. */
    List<Finding> allFindings();

    /** World-space markers for the host adapter to draw in 3D. */
    List<EspMarker> espMarkers();

    List<ScannerInfo> scanners();

    EngineMetrics metrics();

    String dataSourceNote();

    boolean dataSourceAvailable();

    Optional<GuidanceTarget> guidance();

    List<Notification> notifications();

    List<LogEntry> logs();

    List<Rule> rules();

    Vec3d playerPos();

    float playerYaw();
}
