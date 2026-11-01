// Example: ignore the bundled overlay and drive your own GUI from the same data.
// Not part of the build; illustrative only.
package examples;

import com.basefinder.api.BaseFinderModule;
import com.basefinder.config.InMemorySettingsBridge;
import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Finding;
import com.basefinder.ui.EspMarker;
import com.basefinder.ui.ScannerInfo;
import com.basefinder.ui.UiCommands;
import com.basefinder.ui.UiModel;

public final class CustomGuiExample {

    private final BaseFinderModule module;

    public CustomGuiExample(BaseFinderModule module) {
        this.module = module;
    }

    // Read everything your own GUI needs from UiModel:
    public void drawMyGui() {
        UiModel m = module;
        for (BaseCandidate c : m.visibleCandidates()) {
            // c.center(), c.fusedConfidence(), c.pinned(), c.colorOverride()
            draw(c.center().toString(), c.fusedConfidence());
        }
        for (Finding f : m.allFindings()) {
            // your own "findings list" panel: f.label(), f.pos(), f.confidence()
            draw(f.label() + " " + f.pos(), f.confidence());
        }
        for (ScannerInfo s : m.scanners()) {
            // your own scanner toggle list: s.name(), s.enabled(), s.lastScanMs()
        }
        for (EspMarker marker : m.espMarkers()) {
            // draw your own 3D marker at marker.pos() with marker.color()
        }
    }

    // Push changes back via UiCommands:
    public void onUserChangedSettings() {
        UiCommands c = module;
        c.setConfidenceFloor(80);
        c.setScannerEnabled("storage", true);
        c.setEsp(true, true, false, true);
        c.save();
    }

    private void draw(String text, int value) {
        // your renderer here
    }

    public static void main(String[] args) {
        BaseFinderModule module = new BaseFinderModule(() -> null, new InMemorySettingsBridge());
        new CustomGuiExample(module);
    }
}
