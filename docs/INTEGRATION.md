# Integrating Advanced BaseFinder

The module exposes one facade, `com.basefinder.api.BaseFinderModule`, plus two seams
(`UiModel` read / `UiCommands` write) and a Minecraft adapter (`src/mc`). You can
either use the **bundled overlay** or **drive your own GUI** from the same data.

## 1. Construct the module

```java
WorldSource worldSource = () -> /* build a WorldSnapshot from your client */;
SettingsBridge bridge    = /* your client's persistence, or InMemorySettingsBridge */;
BaseFinderModule module  = new BaseFinderModule(worldSource, bridge);
module.start();
```

- `WorldSource.current()` is called on the client thread each cycle and must return a
  **thread-safe/immutable** snapshot (the adapter `McWorldSnapshot` captures one on-thread).
- `SettingsBridge` is how the host persists interface settings. The module **never writes
  files itself**. Analysis findings are **never** persisted.

## 2. Drive the lifecycle (host hooks)

```java
module.onClientTick(gameTime, playerViewSnapshot); // every tick (view snapshot may be lightweight)
module.onBlockChange(pos, oldId, newId, gameTime);  // from the block-update packet handler
module.onBlockEvent(type, pos, blockId, gameTime);  // from the block-action packet handler (chest lids, doors, note blocks)
module.onWorldChanged("disconnect");                // on join/leave/dimension change (clears findings)
module.stop();                                       // on shutdown
```

`onBlockEvent` powers the live stash-activity scanner (a container opening with no player
nearby). It is optional — the module works without it; that one scanner simply stays idle.

## 3. Render

### Option A — bundled overlay (fastest path)
```java
// HUD render event:
module.renderOverlay(new BlazeRenderer(guiGraphics));   // radar, status panel, toasts, guidance, dashboard
// World render event (3D ESP):
EspRenderer.render(pose, buffers, camera, module.espMarkers(), boxes, outlines, labels, icons);
// Input (only while interaction mode is on):
boolean consumed = module.handleInput(inputEvent);      // cancel the event if true
```

### Option B — bring your own GUI
Ignore the bundled overlay and read the same data:
```java
UiModel m = module;                     // BaseFinderModule implements UiModel
m.visibleCandidates();                  // thresholded base candidates
m.allFindings();                        // every raw detection (for a findings list)
m.espMarkers();                         // world markers to draw yourself
m.scanners(); m.metrics(); m.logs();    // status panel data
UiCommands c = module;                  // and push changes back
c.setConfidenceFloor(80); c.setScannerEnabled("storage", false); c.save();
```

## 4. Keybinds (host-owned)
Bind keys in your client's keybind system to:
```java
module.toggleOverlay();           // show/hide the passive HUD
module.toggleInteractionMode();   // enter/exit the interactive dashboard (default key: Insert)
module.toggleRadar();
```

## 5. Screen-capture exclusion (Windows)
`CaptureExclusion.apply(true)` applies `WDA_EXCLUDEFROMCAPTURE` to the Minecraft window
via the Java 25 FFM API. It is a safe no-op off Windows.

## 6. Minecraft adapter notes
`src/mc` uses the deobfuscated 26.2 names. A few hooks are intentionally left explicit
(world box/label drawing, the exact render/tick events) so the module never invents an
unverified client call — wire these to your client's renderer/event bus. Compile with:
```
./gradlew compileMc -PmcJar=C:/path/to/minecraft-26.2.jar
```

See [examples/UsageExample.java](../examples/UsageExample.java) and
[examples/CustomGuiExample.java](../examples/CustomGuiExample.java).
