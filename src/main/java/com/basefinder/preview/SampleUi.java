package com.basefinder.preview;

import com.basefinder.config.GlobalSettings;
import com.basefinder.config.ScanAggressiveness;
import com.basefinder.diag.LogEntry;
import com.basefinder.diag.LogLevel;
import com.basefinder.engine.EngineMetrics;
import com.basefinder.guidance.DirectionalGuidance.GuidanceTarget;
import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.notify.Notification;
import com.basefinder.rules.Rule;
import com.basefinder.ui.EspMarker;
import com.basefinder.ui.ScannerInfo;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.UiCommands;
import com.basefinder.ui.UiModel;
import com.basefinder.ui.theme.StyleMode;
import com.basefinder.ui.theme.ThemeManager;
import com.basefinder.world.Vec3d;
import com.basefinder.world.Vec3i;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sample {@link UiModel}/{@link UiCommands} with realistic data so the preview
 * window shows a fully-populated overlay. Commands mutate this in-memory state so
 * the dashboard controls visibly work.
 */
final class SampleUi implements UiModel, UiCommands {

    private final GlobalSettings globals = new GlobalSettings();
    private final ThemeManager theme = new ThemeManager();
    private final EngineMetrics metrics = new EngineMetrics();
    private final List<BaseCandidate> candidates = new ArrayList<>();
    private final List<Finding> findings = new ArrayList<>();
    private final List<LogEntry> logs = new ArrayList<>();
    private final List<Rule> rules = new ArrayList<>();
    private final Map<String, ScannerDef> scannerDefs = new LinkedHashMap<>();

    SampleUi() {
        globals.setConfidenceFloor(45);
        buildCandidates();
        buildFindings();
        buildScanners();
        buildLogs();
        rules.add(enabledRule("High-confidence \u2192 pin"));
        rules.add(enabledRule("Nether roof \u2192 red"));
        metrics.recordCycle(4.3, 6, candidates.size(), findings.size(), 180_000);
    }

    private static Rule enabledRule(String name) {
        Rule r = new Rule(name);
        r.setEnabled(true);
        return r;
    }

    private BaseCandidate candidate(String id, int x, int y, int z, int conf,
                                    BaseCandidate.CandidateKind kind, boolean pinned) {
        BaseCandidate c = new BaseCandidate(id, new Vec3i(x, y, z), 0);
        c.setFusedConfidence(conf);
        c.setBaseConfidence(conf);
        c.setKind(kind);
        c.setPinned(pinned);
        return c;
    }

    private void buildCandidates() {
        candidates.add(candidate("a", 140, 72, 60, 95, BaseCandidate.CandidateKind.BASE, true));
        candidates.add(candidate("b", -90, 66, 110, 81, BaseCandidate.CandidateKind.BASE, false));
        candidates.add(candidate("c", 40, 40, -130, 62, BaseCandidate.CandidateKind.DUNGEON, false));
        candidates.add(candidate("d", 70, 73, 175, 88, BaseCandidate.CandidateKind.BASE, true));
        candidates.add(candidate("e", -160, 68, -40, 54, BaseCandidate.CandidateKind.ACTIVITY, false));
        candidates.add(candidate("f", 20, 71, 95, 73, BaseCandidate.CandidateKind.BASE, false));
    }

    private void buildFindings() {
        findings.add(finding(FindingCategory.BEACON, 141, 72, 61, 100, "Beacon"));
        findings.add(finding(FindingCategory.STORAGE, 139, 72, 60, 100, "Chest"));
        findings.add(finding(FindingCategory.BED, 142, 72, 62, 100, "Red bed"));
        findings.add(finding(FindingCategory.SPAWNER, 41, 40, -130, 100, "Mob spawner"));
        findings.add(finding(FindingCategory.PORTAL, 71, 73, 176, 92, "Nether portal"));
        findings.add(finding(FindingCategory.REDSTONE, 22, 71, 96, 74, "Redstone"));
    }

    private Finding finding(FindingCategory cat, int x, int y, int z, int conf, String label) {
        return Finding.builder("preview", cat, new Vec3i(x, y, z))
                .confidence(conf).label(label).build();
    }

    private void buildScanners() {
        scannerDef("terrain", "Terrain Anomaly", true, true, 1.2, 3, Severity.LOW);
        scannerDef("storage", "Storage Scanner", true, true, 0.8, 7, Severity.MEDIUM);
        scannerDef("spawner", "Spawner Scanner", true, true, 0.3, 1, Severity.LOW);
        scannerDef("beacon", "Beacon Scanner", true, true, 0.2, 1, Severity.HIGH);
        scannerDef("portal", "Portal Scanner", true, false, 0.5, 2, Severity.MEDIUM);
        scannerDef("redstone", "Redstone Scanner", true, true, 1.1, 4, Severity.LOW);
        scannerDef("lidar", "Lidar Light", false, true, 2.4, 0, Severity.LOW);
        scannerDef("breadcrumb", "Breadcrumb", true, true, 0.6, 5, Severity.MEDIUM);
    }

    private void scannerDef(String id, String name, boolean enabled, boolean healthy,
                            double ms, int count, Severity sev) {
        scannerDefs.put(id, new ScannerDef(id, name, enabled, healthy, ms, count, sev));
    }

    private void buildLogs() {
        logs.add(new LogEntry(LogLevel.INFO, "engine", "Scan cycle complete", 0));
        logs.add(new LogEntry(LogLevel.INFO, "fusion", "6 candidates fused", 0));
        logs.add(new LogEntry(LogLevel.WARN, "portal", "Portal scanner slow this cycle", 0));
    }

    // --- UiModel ----------------------------------------------------------

    @Override public boolean engineRunning() { return true; }
    @Override public boolean overlayEnabled() { return globals.overlayEnabled(); }
    @Override public GlobalSettings globals() { return globals; }
    @Override public ThemeManager theme() { return theme; }
    @Override public List<BaseCandidate> visibleCandidates() { return candidates; }
    @Override public List<Finding> allFindings() { return findings; }

    @Override
    public List<EspMarker> espMarkers() {
        List<EspMarker> out = new ArrayList<>();
        for (BaseCandidate c : candidates) {
            out.add(new EspMarker(c.id(),
                    new Vec3d(c.center().x() + 0.5, c.center().y() + 0.5, c.center().z() + 0.5),
                    UiColor.confidence(c.fusedConfidence()), c.fusedConfidence(),
                    c.displayLabel(), c.pinned()));
        }
        return out;
    }

    @Override
    public List<ScannerInfo> scanners() {
        List<ScannerInfo> out = new ArrayList<>();
        for (ScannerDef d : scannerDefs.values()) {
            out.add(new ScannerInfo(d.id, d.name, d.name + " description", d.enabled, d.healthy,
                    d.ms, d.count, "category", d.sev));
        }
        return out;
    }

    @Override public EngineMetrics metrics() { return metrics; }
    @Override public String dataSourceNote() { return "preview (sample data)"; }
    @Override public boolean dataSourceAvailable() { return true; }
    @Override public Optional<GuidanceTarget> guidance() { return Optional.empty(); }
    @Override public List<Notification> notifications() { return List.of(); }
    @Override public List<LogEntry> logs() { return logs; }
    @Override public List<Rule> rules() { return rules; }
    @Override public Vec3d playerPos() { return new Vec3d(0, 70, 0); }
    @Override public float playerYaw() { return 35f; }

    // --- UiCommands -------------------------------------------------------

    @Override
    public void setScannerEnabled(String scannerId, boolean enabled) {
        ScannerDef d = scannerDefs.get(scannerId);
        if (d != null) {
            d.enabled = enabled;
        }
    }

    @Override public void setConfidenceFloor(int percent) { globals.setConfidenceFloor(percent); }
    @Override public void setDetectionRadius(int chunks) { globals.setDetectionRadiusChunks(chunks); }
    @Override public void setRadarRange(int blocks) { globals.setRadarRange(blocks); }
    @Override public void setAggressiveness(ScanAggressiveness a) { globals.setAggressiveness(a); }
    @Override public void selectTheme(String themeName) { theme.selectPreset(themeName); }

    @Override
    public void setFlatStyle(boolean flat) {
        theme.setStyleMode(flat ? StyleMode.FLAT : StyleMode.POLISHED);
    }

    @Override
    public void setEsp(boolean outlines, boolean boxes, boolean labels, boolean icons) {
        globals.setEspOutlines(outlines);
        globals.setEspBoxes(boxes);
        globals.setEspLabels(labels);
        globals.setEspIcons(icons);
    }

    @Override
    public void setStatusPanelItem(String item, boolean enabled) {
        switch (item) {
            case "engine" -> globals.setSpEngine(enabled);
            case "subsystems" -> globals.setSpSubsystems(enabled);
            case "performance" -> globals.setSpPerformance(enabled);
            case "counts" -> globals.setSpResultCounts(enabled);
            case "activity" -> globals.setSpActivity(enabled);
            case "errors" -> globals.setSpErrors(enabled);
            default -> {
            }
        }
    }

    @Override public void setOverlayEnabled(boolean enabled) { globals.setOverlayEnabled(enabled); }
    @Override public void addRule(Rule rule) { rules.add(rule); }
    @Override public void removeRule(Rule rule) { rules.remove(rule); }

    @Override
    public void addPresetRules() {
        rules.add(enabledRule("Preset: storage cluster"));
        rules.add(enabledRule("Preset: beacon alert"));
    }

    @Override public void save() { }

    private static final class ScannerDef {
        final String id;
        final String name;
        boolean enabled;
        final boolean healthy;
        final double ms;
        final int count;
        final Severity sev;

        ScannerDef(String id, String name, boolean enabled, boolean healthy, double ms, int count,
                   Severity sev) {
            this.id = id;
            this.name = name;
            this.enabled = enabled;
            this.healthy = healthy;
            this.ms = ms;
            this.count = count;
            this.sev = sev;
        }
    }
}
