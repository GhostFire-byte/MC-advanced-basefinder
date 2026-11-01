package com.basefinder.api;

import com.basefinder.config.GlobalSettings;
import com.basefinder.config.ScanAggressiveness;
import com.basefinder.config.Settings;
import com.basefinder.config.SettingsBridge;
import com.basefinder.config.SubsystemSettings;
import com.basefinder.diag.DiagnosticsLog;
import com.basefinder.diag.LogEntry;
import com.basefinder.engine.AnalysisEngine;
import com.basefinder.engine.EngineMetrics;
import com.basefinder.engine.WorldSource;
import com.basefinder.fusion.FusionManager;
import com.basefinder.guidance.DirectionalGuidance;
import com.basefinder.guidance.DirectionalGuidance.GuidanceTarget;
import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Finding;
import com.basefinder.model.Severity;
import com.basefinder.notify.NotificationListener;
import com.basefinder.notify.NotificationManager;
import com.basefinder.notify.Notification;
import com.basefinder.rules.NotifyBehavior;
import com.basefinder.rules.Rule;
import com.basefinder.rules.RuleEngine;
import com.basefinder.rules.RulePresets;
import com.basefinder.scanner.Scanner;
import com.basefinder.scanner.ScannerRegistry;
import com.basefinder.scanner.ScannerStatus;
import com.basefinder.ui.EspMarker;
import com.basefinder.ui.InputEvent;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.ScannerInfo;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.UiCommands;
import com.basefinder.ui.UiModel;
import com.basefinder.ui.theme.StyleMode;
import com.basefinder.ui.theme.ThemeManager;
import com.basefinder.ui.view.OverlayRoot;
import com.basefinder.world.Dimension;
import com.basefinder.world.Vec3d;
import com.basefinder.world.Vec3i;
import com.basefinder.world.WorldSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The single entry point a host client uses. Owns all subsystems and lifecycle,
 * and implements the UI read/write seams so the bundled overlay works out of the
 * box &mdash; or a host can ignore the overlay and drive its own GUI from the
 * same {@link UiModel}/{@link UiCommands}. See docs/INTEGRATION.md.
 */
public final class BaseFinderModule implements UiModel, UiCommands {

    private final Settings settings = new Settings();
    private final SettingsBridge bridge;
    private final ScannerRegistry registry = new ScannerRegistry();
    private final FusionManager fusion = new FusionManager();
    private final DiagnosticsLog log = new DiagnosticsLog();
    private final RuleEngine ruleEngine = new RuleEngine();
    private final NotificationManager notifications = new NotificationManager();
    private final DirectionalGuidance guidance = new DirectionalGuidance();
    private final ThemeManager themeManager = new ThemeManager();
    private final AnalysisEngine engine;
    private final OverlayRoot overlay;

    private volatile Vec3d playerPos = new Vec3d(0, 0, 0);
    private volatile float playerYaw;
    private volatile String dimensionId = Dimension.UNKNOWN.id();
    private volatile List<EspMarker> espMarkers = List.of();
    private volatile Optional<GuidanceTarget> guidanceTarget = Optional.empty();

    public BaseFinderModule(WorldSource worldSource, SettingsBridge bridge) {
        this.bridge = bridge;
        DefaultScanners.registerAll(registry);
        this.engine = new AnalysisEngine(registry, settings, worldSource, fusion, log);
        this.overlay = new OverlayRoot(this, this);
        this.overlay.onLayoutChanged(this::save);
        loadSettings();
        engine.setCallback(this::onCycle);
    }

    // ---- lifecycle -------------------------------------------------------

    public void start() {
        engine.start();
        log.info("module", "BaseFinder started");
    }

    public void stop() {
        engine.stop();
        log.info("module", "BaseFinder stopped");
    }

    /** Call once per client tick, on the client thread. */
    public void onClientTick(long tick, WorldSnapshot snapshotForView) {
        if (snapshotForView != null) {
            playerPos = snapshotForView.playerPos();
            playerYaw = snapshotForView.playerYaw();
            dimensionId = snapshotForView.dimension().id();
        }
        engine.onClientTick(tick);
    }

    public void onBlockChange(Vec3i pos, String oldId, String newId, long tick) {
        engine.onBlockChange(pos, oldId, newId, tick);
    }

    public void onBlockEvent(com.basefinder.engine.BlockEventType type, Vec3i pos, String blockId, long tick) {
        engine.onBlockEvent(type, pos, blockId, tick);
    }

    public void onWorldChanged(String reason) {
        engine.onWorldChanged(reason);
        notifications.reset();
        espMarkers = List.of();
        guidanceTarget = Optional.empty();
    }

    private void onCycle(List<BaseCandidate> candidates, List<Finding> raw, long tick) {
        Map<String, NotifyBehavior> overrides = ruleEngine.apply(candidates, playerPos, dimensionId);
        notifications.process(candidates, overrides, settings.global(), tick);
        espMarkers = buildMarkers(candidates);
        guidanceTarget = guidance.compute(candidates, playerPos, playerYaw, settings.global().confidenceFloor());
    }

    private List<EspMarker> buildMarkers(List<BaseCandidate> candidates) {
        int floor = settings.global().confidenceFloor();
        List<EspMarker> out = new ArrayList<>();
        for (BaseCandidate c : candidates) {
            if (c.kind() == BaseCandidate.CandidateKind.LEFTOVER) {
                continue; // surface explorer-leftovers are never drawn
            }
            if (c.forcedHidden() || !c.visible() || c.fusedConfidence() < floor) {
                continue;
            }
            int color = c.colorOverride() != null ? c.colorOverride() : UiColor.confidence(c.fusedConfidence());
            Vec3d pos = new Vec3d(c.center().x() + 0.5, c.center().y() + 0.5, c.center().z() + 0.5);
            out.add(new EspMarker(c.id(), pos, color, c.fusedConfidence(), c.displayLabel(), c.pinned()));
        }
        return out;
    }

    // ---- host render / input hooks --------------------------------------

    public void renderOverlay(Renderer renderer) {
        overlay.render(renderer);
    }

    public boolean handleInput(InputEvent event) {
        return overlay.handle(event);
    }

    public void toggleInteractionMode() {
        overlay.toggleInteractionMode();
    }

    public boolean interactionMode() {
        return overlay.interactionMode();
    }

    public void toggleOverlay() {
        settings.global().setOverlayEnabled(!settings.global().overlayEnabled());
        save();
    }

    public void toggleRadar() {
        settings.global().setRadarEnabled(!settings.global().radarEnabled());
        save();
    }

    public void addNotificationListener(NotificationListener listener) {
        notifications.addListener(listener);
    }

    public DiagnosticsLog diagnostics() {
        return log;
    }

    public AnalysisEngine engine() {
        return engine;
    }

    public ScannerRegistry registry() {
        return registry;
    }

    public RuleEngine ruleEngine() {
        return ruleEngine;
    }

    public Settings settings() {
        return settings;
    }

    // ---- settings load/save ---------------------------------------------

    private void loadSettings() {
        settings.load(bridge);
        for (Scanner s : registry.all()) {
            SubsystemSettings sub = settings.subsystem(s.id());
            ScannerStatus st = registry.status(s.id());
            if (st != null) {
                st.setEnabled(sub.enabled());
            }
        }
        themeManager.selectPreset(settings.themeName());
        themeManager.setStyleMode("FLAT".equalsIgnoreCase(settings.styleMode()) ? StyleMode.FLAT : StyleMode.POLISHED);
        // GUI skin (overrides the theme-implied default) + saved panel layout / UI scale.
        themeManager.setGuiStyle("IMGUI".equalsIgnoreCase(settings.guiStyle())
                ? com.basefinder.ui.GuiStyle.IMGUI : com.basefinder.ui.GuiStyle.WINDUI);
        overlay.state().importFrom(settings.layout());
        ruleEngine.importFrom(settings.ruleData());
    }

    @Override
    public void save() {
        settings.setThemeName(themeManager.active().name());
        settings.setStyleMode(themeManager.styleMode().name());
        settings.setGuiStyle(themeManager.guiStyle().name());
        settings.layout().clear();
        overlay.state().exportTo(settings.layout());
        settings.ruleData().clear();
        settings.ruleData().addAll(ruleEngine.export());
        settings.save(bridge);
    }

    // ---- UiModel ---------------------------------------------------------

    @Override
    public boolean engineRunning() {
        return engine.running();
    }

    @Override
    public boolean overlayEnabled() {
        return settings.global().overlayEnabled();
    }

    @Override
    public GlobalSettings globals() {
        return settings.global();
    }

    @Override
    public ThemeManager theme() {
        return themeManager;
    }

    @Override
    public List<BaseCandidate> visibleCandidates() {
        int floor = settings.global().confidenceFloor();
        List<BaseCandidate> out = new ArrayList<>();
        for (BaseCandidate c : engine.results().candidates()) {
            if (c.kind() == BaseCandidate.CandidateKind.LEFTOVER) {
                continue; // surface explorer-leftovers are never shown
            }
            // Dungeons / spawner chambers are shown as a distinct non-base marker
            // regardless of the base confidence floor.
            boolean dungeon = c.kind() == BaseCandidate.CandidateKind.DUNGEON;
            if (!c.forcedHidden() && c.visible() && (dungeon || c.fusedConfidence() >= floor)) {
                out.add(c);
            }
        }
        out.sort((a, b) -> Integer.compare(b.fusedConfidence(), a.fusedConfidence()));
        return out;
    }

    @Override
    public List<Finding> allFindings() {
        return engine.results().rawFindings();
    }

    @Override
    public List<EspMarker> espMarkers() {
        return espMarkers;
    }

    @Override
    public List<ScannerInfo> scanners() {
        Map<String, Severity> worst = new HashMap<>();
        for (Finding f : engine.results().rawFindings()) {
            worst.merge(f.scannerId(), f.severity(),
                    (a, b) -> a.weight() >= b.weight() ? a : b);
        }
        List<ScannerInfo> out = new ArrayList<>();
        for (Scanner s : registry.all()) {
            ScannerStatus st = registry.status(s.id());
            out.add(new ScannerInfo(s.id(), s.displayName(), s.description(),
                    st != null && st.enabled(), st == null || st.healthy(),
                    st == null ? 0 : st.lastScanMillis(), st == null ? 0 : st.lastFindingCount(),
                    s.category().label(), worst.getOrDefault(s.id(), Severity.INFO)));
        }
        return out;
    }

    @Override
    public EngineMetrics metrics() {
        return engine.metrics();
    }

    @Override
    public String dataSourceNote() {
        return engine.health().note();
    }

    @Override
    public boolean dataSourceAvailable() {
        return engine.health().available();
    }

    @Override
    public Optional<GuidanceTarget> guidance() {
        return guidanceTarget;
    }

    @Override
    public List<Notification> notifications() {
        return notifications.recent();
    }

    @Override
    public List<LogEntry> logs() {
        return log.recent(80);
    }

    @Override
    public List<Rule> rules() {
        return ruleEngine.rules();
    }

    @Override
    public Vec3d playerPos() {
        return playerPos;
    }

    @Override
    public float playerYaw() {
        return playerYaw;
    }

    // ---- UiCommands ------------------------------------------------------

    @Override
    public void setScannerEnabled(String scannerId, boolean enabled) {
        ScannerStatus st = registry.status(scannerId);
        if (st != null) {
            st.setEnabled(enabled);
        }
        settings.subsystem(scannerId).setEnabled(enabled);
        save();
    }

    @Override
    public void setConfidenceFloor(int percent) {
        settings.global().setConfidenceFloor(percent);
        save();
    }

    @Override
    public void setDetectionRadius(int chunks) {
        settings.global().setDetectionRadiusChunks(chunks);
        save();
    }

    @Override
    public void setRadarRange(int blocks) {
        settings.global().setRadarRange(blocks);
        save();
    }

    @Override
    public void setAggressiveness(ScanAggressiveness aggressiveness) {
        settings.global().setAggressiveness(aggressiveness);
        save();
    }

    @Override
    public void selectTheme(String themeName) {
        themeManager.selectPreset(themeName);
        save();
    }

    @Override
    public void setFlatStyle(boolean flat) {
        themeManager.setStyleMode(flat ? StyleMode.FLAT : StyleMode.POLISHED);
        save();
    }

    @Override
    public void setEsp(boolean outlines, boolean boxes, boolean labels, boolean icons) {
        GlobalSettings g = settings.global();
        g.setEspOutlines(outlines);
        g.setEspBoxes(boxes);
        g.setEspLabels(labels);
        g.setEspIcons(icons);
        save();
    }

    @Override
    public void setStatusPanelItem(String item, boolean enabled) {
        GlobalSettings g = settings.global();
        switch (item) {
            case "engine" -> g.setSpEngine(enabled);
            case "subsystems" -> g.setSpSubsystems(enabled);
            case "dataHealth" -> g.setSpDataHealth(enabled);
            case "performance" -> g.setSpPerformance(enabled);
            case "activity" -> g.setSpActivity(enabled);
            case "resultCounts" -> g.setSpResultCounts(enabled);
            case "errors" -> g.setSpErrors(enabled);
            default -> {
            }
        }
        save();
    }

    @Override
    public void setOverlayEnabled(boolean enabled) {
        settings.global().setOverlayEnabled(enabled);
        save();
    }

    @Override
    public void addRule(Rule rule) {
        ruleEngine.add(rule);
        save();
    }

    @Override
    public void removeRule(Rule rule) {
        ruleEngine.remove(rule);
        save();
    }

    @Override
    public void addPresetRules() {
        for (Rule r : RulePresets.all()) {
            ruleEngine.add(r);
        }
        save();
    }
}
