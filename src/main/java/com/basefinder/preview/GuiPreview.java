package com.basefinder.preview;

import com.basefinder.ui.InputEvent;
import com.basefinder.ui.view.OverlayRoot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Standalone desktop preview of the BaseFinder overlay. Renders the REAL overlay
 * code ({@link OverlayRoot} and its views) through a Java2D renderer, with sample
 * data, so the GUI can be seen without launching Minecraft.
 *
 * <p>Run it with: {@code gradlew previewGui}
 * (or, after {@code gradlew classes}: {@code java -cp build\classes\java\main
 * com.basefinder.preview.GuiPreview}).
 *
 * <p>Controls: <b>I</b> opens/closes the config dashboard (watch the fade);
 * drag the radar, the compact panel and the dashboard; drag the dashboard's
 * bottom-right corner to resize; the Appearance tab has a UI-scale slider.
 * <b>R</b> resets the layout, <b>Esc</b> closes the dashboard.
 */
public final class GuiPreview {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("BaseFinder \u2014 GUI Preview");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new PreviewPanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static final class PreviewPanel extends JPanel {

        private final SampleUi ui = new SampleUi();
        private final OverlayRoot root = new OverlayRoot(ui, ui);
        private final Java2dRenderer renderer = new Java2dRenderer();

        PreviewPanel() {
            setPreferredSize(new Dimension(1180, 680));
            setFocusable(true);
            setBackground(new Color(10, 12, 20));

            MouseAdapter mouse = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    send(InputEvent.mouse(InputEvent.Type.MOUSE_DOWN, e.getX(), e.getY(), e.getButton() - 1));
                }
                @Override public void mouseReleased(MouseEvent e) {
                    send(InputEvent.mouse(InputEvent.Type.MOUSE_UP, e.getX(), e.getY(), e.getButton() - 1));
                }
                @Override public void mouseDragged(MouseEvent e) {
                    send(InputEvent.mouse(InputEvent.Type.MOUSE_DRAG, e.getX(), e.getY(), 0));
                }
                @Override public void mouseMoved(MouseEvent e) {
                    send(InputEvent.mouse(InputEvent.Type.MOUSE_MOVE, e.getX(), e.getY(), 0));
                }
                @Override public void mouseWheelMoved(MouseWheelEvent e) {
                    send(InputEvent.scroll(e.getX(), e.getY(), -e.getPreciseWheelRotation()));
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
            addMouseWheelListener(mouse);

            addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_I -> root.toggleInteractionMode();
                        case KeyEvent.VK_ESCAPE -> root.setInteractionMode(false);
                        case KeyEvent.VK_R -> root.state().resetLayout();
                        default -> {
                        }
                    }
                    repaint();
                }
            });

            new Timer(16, e -> repaint()).start();
            root.setInteractionMode(true); // open the dashboard so the full UI is visible on launch
        }

        private void send(InputEvent e) {
            root.handle(e);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics;
            drawScene(g);
            renderer.begin(g, getWidth(), getHeight());
            root.render(renderer);
            drawHelp(g);
        }

        /** A simple translucent "game" backdrop so the frosted glass reads clearly. */
        private void drawScene(Graphics2D g) {
            g.setPaint(new GradientPaint(0, 0, new Color(26, 32, 54), 0, getHeight(), new Color(8, 10, 18)));
            g.fillRect(0, 0, getWidth(), getHeight());
            int horizon = (int) (getHeight() * 0.62);
            g.setColor(new Color(40, 56, 48));
            g.fillRect(0, horizon, getWidth(), getHeight() - horizon);
            g.setColor(new Color(255, 255, 255, 18));
            for (int i = 0; i < 9; i++) {
                int bx = 60 + i * 130;
                g.fillRect(bx, horizon - 40 - (i % 3) * 25, 90, 120);
            }
        }

        private void drawHelp(Graphics2D g) {
            g.setColor(new Color(255, 255, 255, 200));
            String help = "I: open/close config   \u2022   drag radar / panel / dashboard   \u2022   "
                    + "drag corner to resize   \u2022   R: reset layout   \u2022   Esc: close";
            g.drawString(help, 14, getHeight() - 14);
        }
    }

    private GuiPreview() {
    }
}
