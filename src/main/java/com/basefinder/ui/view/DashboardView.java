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
import com.basefinder.ui.component.Dropdown;
import com.basefinder.ui.component.ScrollPanel;
import com.basefinder.ui.component.Slider;
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
    private final ScrollPanel scroll = new ScrollPanel();
    private final List<Component> active = new ArrayList<>();
    private final List<Rect> tabRects = new ArrayList<>();
    private int selectedTab = 0;
    private double mouseX;
    private double mouseY;
    private final Dropdown guiStyleDropdown;
    private Rect window = new Rect(0, 0, 0, 0);
    private Rect titleBar = new Rect(0, 0, 0, 0);
    private Rect sidebar = new Rect(0, 0, 0, 0);
    private Rect content = new Rect(0, 0, 0, 0);
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
        this.guiStyleDropdown = new Dropdown("GUI style",
                java.util.List.of("WindUI", "ImGui"),
                () -> model.theme().imgui() ? "ImGui" : "WindUI",
                v -> model.theme().setGuiStyle(
                        "ImGui".equals(v) ? com.basefinder.ui.GuiStyle.IMGUI : com.basefinder.ui.GuiStyle.WINDUI));
    }

    public Rect window() {
        return window;
    }

    /** Selects a tab by index (for the preview/screenshot tool and host integration). */
    public void selectTab(int index) {
        if (index >= 0 && index < TABS.size()) {
            selectedTab = index;
        }
    }

    public int tabCount() {
        return TABS.size();
    }

    public Rect titleBar() {
        return titleBar;
    }

    public Rect resizeHandle() {
        return resizeHandle;
    }

    private void layout(Renderer r) {
        active.clear();
        tabRects.clear();
        double sc = state.uiScale;
        int defW = (int) Math.round(760 * sc);
        int defH = (int) Math.round(470 * sc);
        int w = Math.min(state.dashW != null ? state.dashW : defW, r.screenWidth() - 24);
        int h = Math.min(state.dashH != null ? state.dashH : defH, r.screenHeight() - 24);
        w = Math.max(w, 460);
        h = Math.max(h, 300);
        int x = state.dashX != null ? clamp(state.dashX, 0, r.screenWidth() - w) : (r.screenWidth() - w) / 2;
        int y = state.dashY != null ? clamp(state.dashY, 0, r.screenHeight() - h) : (r.screenHeight() - h) / 2;
        window = new Rect(x, y, w, h);
        boolean imgui = model.theme().imgui();
        int handle = (int) Math.round(16 * sc);
        resizeHandle = new Rect(x + w - handle, y + h - handle, handle, handle);

        if (imgui) {
            int titleH = (int) Math.round(26 * sc);
            titleBar = new Rect(x, y, w, titleH);
            sidebar = new Rect(0, 0, 0, 0);
            // Horizontal tab bar below the title bar.
            int tabRowY = y + titleH;
            int tabH = (int) Math.round(22 * sc);
            int tx = x + (int) Math.round(6 * sc);
            for (int i = 0; i < TABS.size(); i++) {
                int tw = r.textWidthScaled(TABS.get(i), (float) sc) + (int) Math.round(18 * sc);
                tabRects.add(new Rect(tx, tabRowY + 2, tw, tabH));
                tx += tw + (int) Math.round(2 * sc);
            }
            content = new Rect(x, tabRowY + tabH + 3, w, h - titleH - tabH - 3)
                    .inset((int) Math.round(8 * sc));
            int bodyTop = content.y() + (int) Math.round(6 * sc);
            this.body = new Rect(content.x() + (int) Math.round(8 * sc), bodyTop,
                    content.w() - (int) Math.round(16 * sc), content.bottom() - bodyTop - (int) Math.round(8 * sc));
        } else {
            int titleH = (int) Math.round(46 * sc);
            titleBar = new Rect(x, y, w, titleH);
            int side = Math.min((int) Math.round(170 * sc), w / 3);
            sidebar = new Rect(x, y + titleH, side, h - titleH);
            content = new Rect(x + side, y + titleH, w - side, h - titleH).inset((int) Math.round(10 * sc));
            int tabH = (int) Math.round(36 * sc);
            int gap = (int) Math.round(6 * sc);
            int ty = sidebar.y() + (int) Math.round(10 * sc);
            for (int i = 0; i < TABS.size(); i++) {
                tabRects.add(new Rect(sidebar.x() + (int) Math.round(10 * sc), ty,
                        sidebar.w() - (int) Math.round(20 * sc), tabH));
                ty += tabH + gap;
            }
            int header = (int) Math.round(42 * sc);
            int bodyTop = content.y() + header;
            this.body = new Rect(content.x() + (int) Math.round(14 * sc), bodyTop,
                    content.w() - (int) Math.round(28 * sc), content.bottom() - bodyTop - (int) Math.round(12 * sc));
        }
        scroll.setBounds(body);
        switch (selectedTab) {
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
        int colW = body.w() / 2 - 10;
        int colX = x + body.w() / 2 + 4;
        int sh = (int) Math.round(30 * state.uiScale);
        int rowGap = (int) Math.round(44 * state.uiScale);
        var g = model.globals();
        add(new Slider("Confidence floor %", 0, 100, g::confidenceFloor, commands::setConfidenceFloor),
                new Rect(x, y, colW, sh));
        add(new Slider("Detection radius (chunks)", 1, 16, g::detectionRadiusChunks, commands::setDetectionRadius),
                new Rect(colX, y, colW, sh));
        y += rowGap;
        add(new Slider("Radar range", 16, 256, g::radarRange, commands::setRadarRange),
                new Rect(x, y, colW, sh));
        add(new ToggleSwitch(g::overlayEnabled, commands::setOverlayEnabled),
                new Rect(colX, y + (int) Math.round(6 * state.uiScale), (int) Math.round(40 * state.uiScale),
                        (int) Math.round(20 * state.uiScale)));
        y += rowGap;
        int bw = (int) Math.round(84 * state.uiScale);
        int bh = (int) Math.round(22 * state.uiScale);
        add(new Button("Light").secondary().onClick(() -> commands.setAggressiveness(ScanAggressiveness.LIGHT)),
                new Rect(x, y, bw, bh));
        add(new Button("Balanced").onClick(() -> commands.setAggressiveness(ScanAggressiveness.BALANCED)),
                new Rect(x + bw + 6, y, bw, bh));
        add(new Button("Aggressive").secondary().onClick(() -> commands.setAggressiveness(ScanAggressiveness.AGGRESSIVE)),
                new Rect(x + 2 * (bw + 6), y, bw, bh));
        y += (int) Math.round(34 * state.uiScale);
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
        int rowH = (int) Math.round(24 * state.uiScale);
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
        double s = state.uiScale;
        int x = body.x();
        int y = body.y();
        int bw = (int) Math.round(150 * s);
        int bh = (int) Math.round(24 * s);
        int gapX = (int) Math.round(10 * s);
        int gapY = (int) Math.round(6 * s);
        int cols = Math.max(1, Math.min(3, body.w() / (bw + gapX)));
        int i = 0;
        for (Theme t : model.theme().presets()) {
            int cx = x + (i % cols) * (bw + gapX);
            int cy = y + (i / cols) * (bh + gapY);
            add(new Button(t.name()).secondary().onClick(() -> commands.selectTheme(t.name())),
                    new Rect(cx, cy, bw, bh));
            i++;
        }
        y += ((i + cols - 1) / cols) * (bh + gapY) + (int) Math.round(12 * s);
        int rowH = (int) Math.round(40 * s);
        add(new Button(model.theme().styleMode() == StyleMode.POLISHED ? "Style: Polished" : "Style: Flat")
                .onClick(() -> commands.setFlatStyle(model.theme().styleMode() == StyleMode.POLISHED)),
                new Rect(x, y, (int) Math.round(150 * s), bh));
        add(new Slider("Opacity", 80, 255, () -> model.theme().active().opacity(),
                v -> model.theme().active().setOpacity(v)),
                new Rect(x + (int) Math.round(170 * s), y, (int) Math.round(180 * s), (int) Math.round(30 * s)));
        y += rowH;
        add(new Slider("UI scale %", 80, 200, () -> (int) Math.round(state.uiScale * 100),
                v -> state.uiScale = v / 100.0),
                new Rect(x, y, (int) Math.round(220 * s), (int) Math.round(30 * s)));
        add(new Button("Reset layout").secondary().onClick(state::resetLayout),
                new Rect(x + (int) Math.round(240 * s), y, (int) Math.round(130 * s), bh));
        y += rowH;
        add(guiStyleDropdown, new Rect(x, y + r_lineGap(), (int) Math.round(180 * s), (int) Math.round(24 * s)));
    }

    private int r_lineGap() {
        return (int) Math.round(12 * state.uiScale);
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
        int ink = alpha(th.text(), fade);
        int dim = alpha(th.textDim(), fade);
        int acc = alpha(th.accent(), fade);
        int borderFade = UiColor.withAlpha(th.border(), (int) Math.round(UiColor.alpha(th.border()) * fade));

        if (model.theme().imgui()) {
            renderImGuiChrome(r, th, fade, sc, ink, dim, acc, borderFade);
        } else {
            renderWindUiChrome(r, th, fade, sc, ink, dim, acc, borderFade);
        }

        r.pushClip(body);
        switch (selectedTab) {
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
        if (selectedTab == 0) {
            overlayOverviewLabels(r, body);
        }
        // Resize grip (three diagonal ticks).
        int grip = UiColor.withAlpha(th.textDim(), (int) Math.round(200 * fade));
        for (int i = 1; i <= 3; i++) {
            r.line(resizeHandle.right() - i * 4, resizeHandle.bottom() - 2,
                    resizeHandle.right() - 2, resizeHandle.bottom() - i * 4, grip, 1f);
        }
    }

    private void renderWindUiChrome(Renderer r, com.basefinder.ui.theme.Theme th, double fade,
                                    float sc, int ink, int dim, int acc, int borderFade) {
        int rad = (int) Math.round(16 * state.uiScale);
        UiDraw.windowPanel(r, window, th, rad, fade);
        r.textScaled("BaseFinder", titleBar.x() + (int) Math.round(18 * sc),
                titleBar.y() + (int) Math.round(10 * sc), ink, false, sc * 1.3f);
        r.textScaled("Advanced world analysis", titleBar.x() + (int) Math.round(18 * sc),
                titleBar.y() + (int) Math.round(10 * sc) + r.lineHeightScaled(sc * 1.3f), dim, false, sc * 0.9f);
        String hint = "I to close";
        r.textScaled(hint, titleBar.right() - r.textWidthScaled(hint, sc) - (int) Math.round(16 * sc),
                titleBar.centerY() - r.lineHeightScaled(sc) / 2, dim, false, sc);
        r.line(titleBar.x() + 12, titleBar.bottom(), titleBar.right() - 12, titleBar.bottom(), borderFade, 1f);

        for (int i = 0; i < tabRects.size(); i++) {
            Rect tr = tabRects.get(i);
            boolean sel = i == selectedTab;
            boolean hov = tr.contains(mouseX, mouseY);
            int fillA = (int) Math.round(((sel ? 26 : 0) + (hov ? 12 : 0)) * fade);
            if (fillA > 0) {
                UiDraw.roundedRect(r, tr, (int) Math.round(8 * sc), UiColor.withAlpha(th.text(), fillA));
            }
            if (sel) {
                // Accent bar on the left edge marks the active tab.
                UiDraw.roundedRect(r, new Rect(tr.x() + 2, tr.y() + 6, (int) Math.round(3 * sc), tr.h() - 12),
                        2, acc);
            }
            r.textScaled(TABS.get(i), tr.x() + (int) Math.round(16 * sc),
                    tr.centerY() - r.lineHeightScaled(sc) / 2, sel ? ink : dim, false, sc);
        }

        UiDraw.card(r, content, th, (int) Math.round(12 * state.uiScale), 0, fade);
        r.textScaled(TABS.get(selectedTab), content.x() + (int) Math.round(16 * sc),
                content.y() + (int) Math.round(12 * sc), ink, false, sc * 1.1f);
        r.line(content.x() + 12, content.y() + (int) Math.round(40 * sc), content.right() - 12,
                content.y() + (int) Math.round(40 * sc), borderFade, 1f);
    }

    private void renderImGuiChrome(Renderer r, com.basefinder.ui.theme.Theme th, double fade,
                                   float sc, int ink, int dim, int acc, int borderFade) {
        // Flat Dear-ImGui window: thin border, darker title bar, underlined active tab.
        int winA = (int) Math.round(th.opacity() * fade);
        UiDraw.shadow(r, window, 3, 4, fade);
        UiDraw.roundedRect(r, window, 3, UiColor.withAlpha(th.background(), winA));
        UiDraw.roundedOutline(r, window, 3, borderFade, 1f);
        // Title bar strip (frame colour) + collapse dot + title.
        UiDraw.roundedRect(r, titleBar, 3, UiColor.withAlpha(th.panelTop(), winA));
        r.triangle(titleBar.x() + 10, titleBar.centerY() - 3, titleBar.x() + 16, titleBar.centerY() - 3,
                titleBar.x() + 13, titleBar.centerY() + 3, acc);
        r.textScaled("BaseFinder", titleBar.x() + (int) Math.round(24 * sc),
                titleBar.centerY() - r.lineHeightScaled(sc) / 2, ink, false, sc);
        r.line(titleBar.x(), titleBar.bottom(), titleBar.right(), titleBar.bottom(), borderFade, 1f);

        for (int i = 0; i < tabRects.size(); i++) {
            Rect tr = tabRects.get(i);
            boolean sel = i == selectedTab;
            boolean hov = tr.contains(mouseX, mouseY);
            int fillA = (int) Math.round(((sel ? 255 : hov ? 90 : 40) * fade));
            int base = sel ? th.accent() : th.panelTop();
            UiDraw.roundedRect(r, tr, 3, UiColor.withAlpha(base, sel ? winA : fillA));
            r.textScaled(TABS.get(i), tr.centerX() - r.textWidthScaled(TABS.get(i), sc) / 2,
                    tr.centerY() - r.lineHeightScaled(sc) / 2, sel ? UiColor.rgb(16, 17, 24) : dim, false, sc);
        }
        // Content frame.
        UiDraw.roundedRect(r, content, 3, UiColor.withAlpha(th.panelTop(), (int) Math.round(120 * fade)));
        UiDraw.roundedOutline(r, content, 3, borderFade, 1f);
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
        float sc = state.textScale();
        int rowH = (int) Math.round(24 * state.uiScale);
        int y = scroll.originY();
        for (ScannerInfo s : model.scanners()) {
            if (y + rowH >= body.y() && y <= body.bottom()) {
                int dot = s.healthy() ? UiColor.rgb(80, 200, 120) : UiColor.rgb(230, 110, 90);
                r.filledCircle(body.x() + 6, y + rowH / 2.0, 3 * sc, s.enabled() ? dot : UiColor.rgb(100, 100, 110));
                r.textScaled(s.name(), body.x() + 16, y + 2, model.theme().active().text(), true, sc);
                r.textScaled(String.format("%.1fms  %d found", s.lastScanMs(), s.lastFindingCount()),
                        body.x() + 16, y + 2 + r.lineHeightScaled(sc) / 2, model.theme().active().textDim(), true, sc * 0.85f);
            }
            y += rowH;
        }
    }

    private void renderFindings(Renderer r, Rect body) {
        float sc = state.textScale();
        int rowH = (int) Math.round(15 * state.uiScale);
        List<Finding> findings = model.allFindings();
        scroll.setContentHeight(findings.size() * rowH);
        int y = scroll.originY();
        for (Finding f : findings) {
            if (y + rowH >= body.y() && y <= body.bottom()) {
                int color = UiColor.confidence(f.confidence());
                r.textScaled(f.confidence() + "%", body.x(), y, color, true, sc);
                r.textScaled(f.label(), body.x() + (int) Math.round(38 * sc), y, model.theme().active().text(), true, sc);
                String coords = f.pos().toString();
                r.textScaled(coords, body.right() - r.textWidthScaled(coords, sc) - 2, y,
                        model.theme().active().textDim(), true, sc);
            }
            y += rowH;
        }
        if (findings.isEmpty()) {
            r.textScaled("No findings yet.", body.x(), body.y(), model.theme().active().textDim(), true, sc);
        }
    }

    private void renderRules(Renderer r, Rect body) {
        float sc = state.textScale();
        int y = body.y() + (int) Math.round(56 * state.uiScale);
        for (Rule rule : model.rules()) {
            r.textScaled((rule.enabled() ? "\u25CF " : "\u25CB ") + rule.name(), body.x(), y,
                    model.theme().active().text(), true, sc);
            y += (int) Math.round(22 * state.uiScale);
        }
        if (model.rules().isEmpty()) {
            r.textScaled("No rules. Add presets or build one above.", body.x(), y,
                    model.theme().active().textDim(), true, sc);
        }
    }

    private void renderDiagnostics(Renderer r, Rect body) {
        float sc = state.textScale();
        int rowH = (int) Math.round(13 * state.uiScale);
        var logs = model.logs();
        scroll.setContentHeight(logs.size() * rowH + rowH);
        int y = scroll.originY();
        r.textScaled("Data source: " + model.dataSourceNote(), body.x(), y,
                model.theme().active().textDim(), true, sc);
        y += rowH;
        for (var e : logs) {
            if (y + rowH >= body.y() && y <= body.bottom()) {
                int color = switch (e.level()) {
                    case ERROR -> UiColor.rgb(230, 110, 90);
                    case WARN -> UiColor.rgb(235, 190, 80);
                    default -> model.theme().active().textDim();
                };
                r.textScaled(e.toString(), body.x(), y, color, true, sc * 0.9f);
            }
            y += rowH;
        }
    }

    public boolean handle(InputEvent e) {
        if (e.type() == InputEvent.Type.MOUSE_MOVE || e.type() == InputEvent.Type.MOUSE_DRAG) {
            mouseX = e.mouseX();
            mouseY = e.mouseY();
        }
        // Sidebar tab selection.
        if (e.type() == InputEvent.Type.MOUSE_DOWN) {
            for (int i = 0; i < tabRects.size(); i++) {
                if (tabRects.get(i).contains(e.mouseX(), e.mouseY())) {
                    selectedTab = i;
                    return true;
                }
            }
        }
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

    private int alpha(int color, double fade) {
        return UiColor.withAlpha(color, (int) Math.round(255 * Math.max(0, Math.min(1, fade))));
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
