package com.basefinder.ui.view;

import com.basefinder.config.ScanAggressiveness;
import com.basefinder.model.Finding;
import com.basefinder.rules.Condition;
import com.basefinder.rules.ConditionField;
import com.basefinder.rules.ConditionGroup;
import com.basefinder.rules.LogicOp;
import com.basefinder.rules.Operator;
import com.basefinder.rules.Rule;
import com.basefinder.rules.RuleAction;
import com.basefinder.rules.RuleActionType;
import com.basefinder.ui.InputEvent;
import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.ScannerInfo;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.UiCommands;
import com.basefinder.ui.UiDraw;
import com.basefinder.ui.UiModel;
import com.basefinder.ui.UiState;
import com.basefinder.ui.component.Button;
import com.basefinder.ui.component.Component;
import com.basefinder.ui.component.ScrollPanel;
import com.basefinder.ui.component.Slider;
import com.basefinder.ui.component.TabBar;
import com.basefinder.ui.component.ToggleSwitch;
import com.basefinder.ui.theme.StyleMode;
import com.basefinder.ui.theme.Theme;

import java.util.ArrayList;
import java.util.List;

/**
 * The interactive BaseFinder window: a centred, tabbed panel for settings,
 * scanners, findings review, rules and diagnostics. Only consumes input while
 * interaction mode is on, so it never interferes with normal gameplay.
 */
public final class DashboardView {

    private static final List<String> TABS = List.of(
            "Overview", "Scanners", "Findings", "Rules", "Appearance", "Diagnostics");

    private final UiModel model;
    private final UiCommands commands;
    private final UiState state;
    private final TabBar tabBar;
    private final ScrollPanel scroll = new ScrollPanel();
    private final List<Component> active = new ArrayList<>();
    private Rect window = new Rect(0, 0, 0, 0);
    private Rect titleBar = new Rect(0, 0, 0, 0);
    private Rect resizeHandle = new Rect(0, 0, 0, 0);
    private Rect body = new Rect(0, 0, 0, 0);

    // Simple custom-rule builder state.
    private ConditionField builderField = ConditionField.FUSED_SCORE;
    private Operator builderOp = Operator.GTE;
    private int builderValue = 80;
    private RuleActionType builderAction = RuleActionType.PIN_RADAR;

    public DashboardView(UiModel model, UiCommands commands, UiState state) {
        this.model = model;
        this.commands = commands;
        this.state = state;
        this.tabBar = new TabBar(TABS);
    }

    public Rect window() {
        return window;
    }

    public Rect titleBar() {
        return titleBar;
    }

    public Rect resizeHandle() {
        return resizeHandle;
    }

    private void layout(Renderer r) {
        active.clear();
        double sc = state.uiScale;
        int defW = (int) Math.round(640 * sc);
        int defH = (int) Math.round(404 * sc);
        int w = Math.min(state.dashW != null ? state.dashW : defW, r.screenWidth() - 24);
        int h = Math.min(state.dashH != null ? state.dashH : defH, r.screenHeight() - 24);
        w = Math.max(w, 360);
        h = Math.max(h, 240);
        int x = state.dashX != null ? clamp(state.dashX, 0, r.screenWidth() - w) : (r.screenWidth() - w) / 2;
        int y = state.dashY != null ? clamp(state.dashY, 0, r.screenHeight() - h) : (r.screenHeight() - h) / 2;
        window = new Rect(x, y, w, h);
        int titleH = (int) Math.round(30 * sc);
        titleBar = new Rect(x, y, w, titleH);
        int handle = (int) Math.round(16 * sc);
        resizeHandle = new Rect(x + w - handle, y + h - handle, handle, handle);

        int tabY = y + titleH + (int) Math.round(6 * sc);
        int tabH = (int) Math.round(22 * sc);
        tabBar.setBounds(new Rect(x + 10, tabY, w - 20, tabH));
        active.add(tabBar);

        int bodyTop = tabY + tabH + (int) Math.round(8 * sc);
        this.body = new Rect(x + 12, bodyTop, w - 24, window.bottom() - bodyTop - 12);
        scroll.setBounds(body);
        switch (tabBar.selected()) {
            case 0 -> layoutOverview(body);
            case 1 -> layoutScanners(body);
            case 3 -> layoutRules(body);
            case 4 -> layoutAppearance(body);
            default -> {
            }
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(Math.max(lo, hi), v));
    }

    private void layoutOverview(Rect body) {
        int x = body.x();
        int y = body.y();
        var g = model.globals();
        add(new Slider("Confidence floor %", 0, 100, g::confidenceFloor, commands::setConfidenceFloor),
                new Rect(x, y, body.w() / 2 - 8, 26));
        add(new Slider("Detection radius (chunks)", 1, 16, g::detectionRadiusChunks, commands::setDetectionRadius),
                new Rect(x + body.w() / 2 + 2, y, body.w() / 2 - 8, 26));
        y += 32;
        add(new Slider("Radar range", 16, 256, g::radarRange, commands::setRadarRange),
                new Rect(x, y, body.w() / 2 - 8, 26));
        add(new ToggleSwitch(g::overlayEnabled, commands::setOverlayEnabled),
                new Rect(x + body.w() / 2 + 2, y + 8, 36, 16));
        y += 36;
        add(new Button("Light").secondary().onClick(() -> commands.setAggressiveness(ScanAggressiveness.LIGHT)),
                new Rect(x, y, 70, 20));
        add(new Button("Balanced").onClick(() -> commands.setAggressiveness(ScanAggressiveness.BALANCED)),
                new Rect(x + 76, y, 80, 20));
        add(new Button("Aggressive").secondary().onClick(() -> commands.setAggressiveness(ScanAggressiveness.AGGRESSIVE)),
                new Rect(x + 162, y, 90, 20));
        y += 28;
        add(new ToggleSwitch(g::espOutlines, v -> commands.setEsp(v, g.espBoxes(), g.espLabels(), g.espIcons())),
                new Rect(x, y, 36, 16));
        add(new ToggleSwitch(g::espBoxes, v -> commands.setEsp(g.espOutlines(), v, g.espLabels(), g.espIcons())),
                new Rect(x + 120, y, 36, 16));
        add(new ToggleSwitch(g::espLabels, v -> commands.setEsp(g.espOutlines(), g.espBoxes(), v, g.espIcons())),
                new Rect(x + 240, y, 36, 16));
        add(new ToggleSwitch(g::espIcons, v -> commands.setEsp(g.espOutlines(), g.espBoxes(), g.espLabels(), v)),
                new Rect(x + 360, y, 36, 16));
    }

    private void layoutScanners(Rect body) {
        int rowH = 24;
        scroll.setContentHeight(model.scanners().size() * rowH);
        int y = scroll.originY();
        for (ScannerInfo s : model.scanners()) {
            if (y + rowH >= body.y() && y <= body.bottom()) {
                add(new ToggleSwitch(s::enabled, v -> commands.setScannerEnabled(s.id(), v)),
                        new Rect(body.right() - 40, y + 4, 34, 16));
            }
            y += rowH;
        }
    }

    private void layoutRules(Rect body) {
        int x = body.x();
        int y = body.y();
        add(new Button("Add preset rules").onClick(commands::addPresetRules), new Rect(x, y, 120, 20));
        add(new Button("Field: " + builderField).secondary().onClick(this::cycleField), new Rect(x + 126, y, 120, 20));
        add(new Button(builderOp.symbol()).secondary().onClick(this::cycleOp), new Rect(x + 250, y, 44, 20));
        y += 26;
        add(new Slider("Value", 0, 256, () -> builderValue, v -> builderValue = v), new Rect(x, y, 160, 24));
        add(new Button("Action: " + builderAction).secondary().onClick(this::cycleAction), new Rect(x + 170, y, 150, 20));
        add(new Button("Create rule").onClick(this::createRule), new Rect(x + 326, y, 90, 20));
        y += 30;
        int rowH = 22;
        List<Rule> rules = model.rules();
        for (Rule rule : rules) {
            add(new ToggleSwitch(rule::enabled, rule::setEnabled), new Rect(body.right() - 80, y + 2, 34, 16));
            add(new Button("X").secondary().onClick(() -> commands.removeRule(rule)), new Rect(body.right() - 40, y, 18, 18));
            y += rowH;
        }
    }

    private void layoutAppearance(Rect body) {
        int x = body.x();
        int y = body.y();
        int i = 0;
        for (Theme t : model.theme().presets()) {
            add(new Button(t.name()).secondary().onClick(() -> commands.selectTheme(t.name())),
                    new Rect(x + (i % 3) * 120, y + (i / 3) * 24, 114, 20));
            i++;
        }
        y += ((i + 2) / 3) * 24 + 6;
        add(new Button(model.theme().styleMode() == StyleMode.POLISHED ? "Style: Polished" : "Style: Flat")
                .onClick(() -> commands.setFlatStyle(model.theme().styleMode() == StyleMode.POLISHED)),
                new Rect(x, y, 130, 20));
        add(new Slider("Opacity", 80, 255, () -> model.theme().active().opacity(),
                v -> model.theme().active().setOpacity(v)), new Rect(x + 140, y, 160, 24));
        y += 30;
        add(new Slider("UI scale %", 80, 200, () -> (int) Math.round(state.uiScale * 100),
                v -> state.uiScale = v / 100.0), new Rect(x, y, 200, 24));
        add(new Button("Reset layout").secondary().onClick(state::resetLayout),
                new Rect(x + 210, y, 120, 20));
    }

    private void add(Component c, Rect bounds) {
        c.setBounds(bounds);
        active.add(c);
    }

    public void render(Renderer r) {
        layout(r);
        var th = model.theme().active();
        double fade = Math.max(0.0, Math.min(1.0, state.dashFade));
        float sc = state.textScale();

        // Frosted window with open/close fade.
        UiDraw.glassPanel(r, window, th, model.theme().polished(), 12, fade);
        // Title bar accent strip.
        UiDraw.roundedGradient(r, new Rect(titleBar.x(), titleBar.y(), titleBar.w(), titleBar.h()),
                12, UiColor.withAlpha(th.panelTop(), (int) Math.round(th.opacity() * fade)),
                UiColor.withAlpha(th.panelBottom(), (int) Math.round(th.opacity() * 0.6 * fade)));
        r.line(titleBar.x() + 10, titleBar.bottom(), titleBar.right() - 10, titleBar.bottom(),
                UiColor.withAlpha(th.border(), (int) Math.round(200 * fade)), 1f);

        int ink = UiColor.withAlpha(th.primary(), (int) Math.round(255 * fade));
        int dim = UiColor.withAlpha(th.textDim(), (int) Math.round(255 * fade));
        r.textScaled("BaseFinder", titleBar.x() + 12,
                titleBar.centerY() - r.lineHeightScaled(sc * 1.25f) / 2, ink, true, sc * 1.25f);
        r.textScaled("INSERT to close", titleBar.right() - r.textWidthScaled("INSERT to close", sc) - 12,
                titleBar.centerY() - r.lineHeightScaled(sc) / 2, dim, true, sc);

        r.pushClip(body);
        switch (tabBar.selected()) {
            case 1 -> renderScanners(r, body);
            case 2 -> renderFindings(r, body);
            case 3 -> renderRules(r, body);
            case 5 -> renderDiagnostics(r, body);
            default -> {
            }
        }
        r.popClip();
        for (Component c : active) {
            c.render(r, model.theme());
        }
        if (tabBar.selected() == 0) {
            overlayOverviewLabels(r, body);
        }
        // Resize grip (three diagonal ticks).
        int grip = UiColor.withAlpha(th.border(), (int) Math.round(220 * fade));
        for (int i = 1; i <= 3; i++) {
            r.line(resizeHandle.right() - i * 4, resizeHandle.bottom() - 2,
                    resizeHandle.right() - 2, resizeHandle.bottom() - i * 4, grip, 1f);
        }
    }

    private void overlayOverviewLabels(Renderer r, Rect body) {
        int c = model.theme().active().textDim();
        r.text("Overlay", body.x() + body.w() / 2 + 44, body.y() + 76, c, true);
        r.text("Out", body.x() + 42, body.y() + 112, c, true);
        r.text("Box", body.x() + 162, body.y() + 112, c, true);
        r.text("Lbl", body.x() + 282, body.y() + 112, c, true);
        r.text("Ico", body.x() + 402, body.y() + 112, c, true);
    }

    private void renderScanners(Renderer r, Rect body) {
        int rowH = 24;
        int y = scroll.originY();
        for (ScannerInfo s : model.scanners()) {
            if (y + rowH >= body.y() && y <= body.bottom()) {
                int dot = s.healthy() ? UiColor.rgb(80, 200, 120) : UiColor.rgb(230, 110, 90);
                r.filledCircle(body.x() + 6, y + rowH / 2, 3, s.enabled() ? dot : UiColor.rgb(100, 100, 110));
                r.text(s.name(), body.x() + 16, y + 2, model.theme().active().text(), true);
                r.text(String.format("%.1fms  %d found", s.lastScanMs(), s.lastFindingCount()),
                        body.x() + 16, y + 2 + r.lineHeight() / 2 + 2, model.theme().active().textDim(), true);
            }
            y += rowH;
        }
    }

    private void renderFindings(Renderer r, Rect body) {
        List<Finding> findings = model.allFindings();
        scroll.setContentHeight(findings.size() * 14);
        int y = scroll.originY();
        for (Finding f : findings) {
            if (y + 14 >= body.y() && y <= body.bottom()) {
                int color = UiColor.confidence(f.confidence());
                r.text(f.confidence() + "%", body.x(), y, color, true);
                r.text(f.label(), body.x() + 34, y, model.theme().active().text(), true);
                String coords = f.pos().toString();
                r.text(coords, body.right() - r.textWidth(coords) - 2, y, model.theme().active().textDim(), true);
            }
            y += 14;
        }
        if (findings.isEmpty()) {
            r.text("No findings yet.", body.x(), body.y(), model.theme().active().textDim(), true);
        }
    }

    private void renderRules(Renderer r, Rect body) {
        int y = body.y() + 56;
        for (Rule rule : model.rules()) {
            r.text((rule.enabled() ? "\u25CF " : "\u25CB ") + rule.name(), body.x(), y,
                    model.theme().active().text(), true);
            y += 22;
        }
        if (model.rules().isEmpty()) {
            r.text("No rules. Add presets or build one above.", body.x(), y, model.theme().active().textDim(), true);
        }
    }

    private void renderDiagnostics(Renderer r, Rect body) {
        var logs = model.logs();
        scroll.setContentHeight(logs.size() * 12 + 14);
        int y = scroll.originY();
        r.text("Data source: " + model.dataSourceNote(), body.x(), y, model.theme().active().textDim(), true);
        y += 14;
        for (var e : logs) {
            if (y + 12 >= body.y() && y <= body.bottom()) {
                int color = switch (e.level()) {
                    case ERROR -> UiColor.rgb(230, 110, 90);
                    case WARN -> UiColor.rgb(235, 190, 80);
                    default -> model.theme().active().textDim();
                };
                r.text(e.toString(), body.x(), y, color, true);
            }
            y += 12;
        }
    }

    public boolean handle(InputEvent e) {
        if (scroll.handle(e, model.theme())) {
            return true;
        }
        for (Component c : active) {
            if (c.handle(e, model.theme())) {
                return true;
            }
        }
        // Consume clicks inside the window so they don't fall through to the game.
        return e.type() == InputEvent.Type.MOUSE_DOWN && window.contains(e.mouseX(), e.mouseY());
    }

    private void cycleField() {
        ConditionField[] v = ConditionField.values();
        builderField = v[(builderField.ordinal() + 1) % v.length];
    }

    private void cycleOp() {
        Operator[] v = Operator.values();
        builderOp = v[(builderOp.ordinal() + 1) % v.length];
    }

    private void cycleAction() {
        RuleActionType[] v = RuleActionType.values();
        builderAction = v[(builderAction.ordinal() + 1) % v.length];
    }

    private void createRule() {
        Rule rule = new Rule("Custom: " + builderField + " " + builderOp.symbol() + " " + builderValue);
        ConditionGroup group = new ConditionGroup(LogicOp.AND);
        group.add(new Condition(builderField, builderOp, String.valueOf(builderValue)));
        rule.groups().add(group);
        rule.actions().add(new RuleAction(builderAction, defaultActionValue(builderAction)));
        commands.addRule(rule);
    }

    private static String defaultActionValue(RuleActionType type) {
        return switch (type) {
            case SET_COLOR -> String.valueOf(0xFF55FFAA);
            case SET_VISIBLE -> "true";
            case SET_LABEL -> "Marked";
            case SET_ICON -> "star";
            case SET_NOTIFY_SEVERITY -> "HIGH";
            case SET_NOTIFY_BEHAVIOR -> "ALWAYS";
            default -> "true";
        };
    }
}
