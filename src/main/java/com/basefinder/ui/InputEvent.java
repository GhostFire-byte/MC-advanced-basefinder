package com.basefinder.ui;

/** A normalised input event delivered to the interactive overlay. */
public final class InputEvent {

    public enum Type {MOUSE_DOWN, MOUSE_UP, MOUSE_MOVE, MOUSE_DRAG, SCROLL, KEY_DOWN, CHAR}

    private final Type type;
    private final double mouseX;
    private final double mouseY;
    private final int button;
    private final double scroll;
    private final int keyCode;
    private final char character;

    private InputEvent(Type type, double mouseX, double mouseY, int button, double scroll, int keyCode, char character) {
        this.type = type;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.button = button;
        this.scroll = scroll;
        this.keyCode = keyCode;
        this.character = character;
    }

    public static InputEvent mouse(Type type, double x, double y, int button) {
        return new InputEvent(type, x, y, button, 0, 0, '\0');
    }

    public static InputEvent scroll(double x, double y, double amount) {
        return new InputEvent(Type.SCROLL, x, y, 0, amount, 0, '\0');
    }

    public static InputEvent key(int keyCode) {
        return new InputEvent(Type.KEY_DOWN, 0, 0, 0, 0, keyCode, '\0');
    }

    public static InputEvent character(char c) {
        return new InputEvent(Type.CHAR, 0, 0, 0, 0, 0, c);
    }

    public Type type() {
        return type;
    }

    public double mouseX() {
        return mouseX;
    }

    public double mouseY() {
        return mouseY;
    }

    public int button() {
        return button;
    }

    public double scroll() {
        return scroll;
    }

    public int keyCode() {
        return keyCode;
    }

    public char character() {
        return character;
    }
}
