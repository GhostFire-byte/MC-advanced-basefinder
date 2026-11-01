package com.basefinder.ui.view;

import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.ScannerInfo;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.UiDraw;
import com.basefinder.ui.UiModel;
import com.basefinder.ui.UiState;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact, configurable status panel (top-left). Only shows the blocks the user
 * enabled, so the screen is never forced full of information.
 */
public final class StatusPanelView {

    private final UiState state;
    private Rect lastBox = new Rect(10, 10, 0, 0);

    public StatusPanelView(UiState state) {
        this.state = state;
    }

    /** Last drawn bounds, for drag hit-testing. */
    public Rect bounds() {
        return lastBox;
    }

    public void render(Renderer r, UiModel model) {
        List<String> lines = new ArrayList<>();
        var g = model.globals();
        if (g.spEngine()) {
            lines.add((model.engineRunning() ? "\u25CF Engine: running" : "\u25CB Engine: stopped"));
            lines.add("Data: " + (model.dataSourceAvailable() ? "available" : model.dataSourceNote()));
        }
        if (g.spSubsystems()) {
            int on = 0;
            for (ScannerInfo s : model.scanners()) {
                if (s.enabled()) {
                    on++;
                }
            }
            lines.add("Scanners: " + on + "/" + model.scanners().size() + " active");
        }
        if (g.spPerformance()) {
            lines.add(String.format("Scan: %.1f ms  (%d workers)",
                    model.metrics().lastCycleMillis(), model.metrics().workerCount()));
        }
        if (g.spResultCounts()) {
            lines.add("Candidates: " + model.visibleCandidates().size()
                    + "   Findings: " + model.metrics().lastFindingCount());
        }
        if (g.spActivity()) {
            lines.add("Cycle #" + model.metrics().totalCycles());
        }
        if (g.spErrors()) {
            int unhealthy = 0;
            for (ScannerInfo s : model.scanners()) {
                if (!s.healthy()) {
                    unhealthy++;
                }
            }
            lines.add(unhealthy == 0 ? "No errors" : (unhealthy + " scanner error(s)"));
        }
        if (lines.isEmpty()) {
            lastBox = new Rect(posX(), posY(), 0, 0);
            return;
        }

        float sc = state.textScale();
        int pad = Math.round(7 * sc);
        int lh = r.lineHeightScaled(sc);
        int w = Math.round(150 * sc);
        for (String l : lines) {
            w = Math.max(w, r.textWidthScaled(l, sc) + pad * 2);
        }
        int h = pad * 2 + lh * (lines.size() + 1) + Math.round(4 * sc);
        Rect box = new Rect(posX(), posY(), w, h);
        lastBox = box;
        drawPanel(r, model, box);
        int y = box.y() + pad;
        r.textScaled("BaseFinder", box.x() + pad, y, model.theme().active().primary(), true, sc * 1.1f);
        y += lh + Math.round(4 * sc);
        for (String l : lines) {
            int color = l.contains("error") && !l.startsWith("No") ? UiColor.rgb(230, 110, 90)
                    : model.theme().active().text();
            r.textScaled(l, box.x() + pad, y, color, true, sc);
            y += lh;
        }
    }

    private int posX() {
        return state.statusX != null ? state.statusX : 12;
    }

    private int posY() {
        return state.statusY != null ? state.statusY : 12;
    }

    static void drawPanel(Renderer r, UiModel model, Rect box) {
        drawPanel(r, model, box, 1.0);
    }

    static void drawPanel(Renderer r, UiModel model, Rect box, double alphaScale) {
        UiDraw.glassPanel(r, box, model.theme().active(), model.theme().polished(), 10, alphaScale);
    }
}
