package com.basefinder.ui.theme;

/**
 * A colour theme. Individual colours are fully configurable; presets just seed
 * sensible defaults. Opacity and gradient are applied by the views depending on
 * the active {@link StyleMode}.
 */
public final class Theme {

    private String name;
    private int primary;
    private int secondary;
    private int accent;
    private int background;
    private int panelTop;
    private int panelBottom;
    private int text;
    private int textDim;
    private int border;
    private int opacity = 230; // panel alpha 0..255

    public Theme(String name, int primary, int secondary, int accent, int background,
                 int panelTop, int panelBottom, int text, int textDim, int border) {
        this.name = name;
        this.primary = primary;
        this.secondary = secondary;
        this.accent = accent;
        this.background = background;
        this.panelTop = panelTop;
        this.panelBottom = panelBottom;
        this.text = text;
        this.textDim = textDim;
        this.border = border;
    }

    public Theme copy(String newName) {
        Theme t = new Theme(newName, primary, secondary, accent, background, panelTop, panelBottom, text, textDim, border);
        t.opacity = opacity;
        return t;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int primary() {
        return primary;
    }

    public void setPrimary(int v) {
        this.primary = v;
    }

    public int secondary() {
        return secondary;
    }

    public void setSecondary(int v) {
        this.secondary = v;
    }

    public int accent() {
        return accent;
    }

    public void setAccent(int v) {
        this.accent = v;
    }

    public int background() {
        return background;
    }

    public void setBackground(int v) {
        this.background = v;
    }

    public int panelTop() {
        return panelTop;
    }

    public void setPanelTop(int v) {
        this.panelTop = v;
    }

    public int panelBottom() {
        return panelBottom;
    }

    public void setPanelBottom(int v) {
        this.panelBottom = v;
    }

    public int text() {
        return text;
    }

    public void setText(int v) {
        this.text = v;
    }

    public int textDim() {
        return textDim;
    }

    public void setTextDim(int v) {
        this.textDim = v;
    }

    public int border() {
        return border;
    }

    public void setBorder(int v) {
        this.border = v;
    }

    public int opacity() {
        return opacity;
    }

    public void setOpacity(int v) {
        this.opacity = Math.max(0, Math.min(255, v));
    }
}
