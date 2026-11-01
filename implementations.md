# BaseFinder Intelligence Suite — Development Specification

## Project overview

Develop a standalone Java module called **Advanced BaseFinder** for Minecraft Java Edition 26.2.

The module is intended to provide advanced client-side world analysis, information visualisation, diagnostics, configurable analysis subsystems, and a polished overlay experience.

This is a module for an existing custom Minecraft client. It is NOT a standalone Minecraft client, launcher, injector, or replacement GUI framework.

The existing custom client already has its own:
- Module management system
- GUI and settings framework
- Keybind handling
- Minecraft integration and lifecycle
- Rendering infrastructure

The module will eventually be integrated into that client by a separate integration process. Therefore, keep the module independent, reusable, and easy to adapt.

Use only information legitimately available to the normal Minecraft client. Do not depend on server-side files, privileged server commands, server APIs.

---

## 1. Development workflow — extremely important

Do not immediately start coding.

Begin by reviewing this specification and identifying:
- Missing requirements
- Ambiguous behaviour
- Technical limitations
- Compatibility risks
- Potential performance problems
- Features that need further clarification

Ask me clarifying questions **one at a time**.

You should actively suggest useful improvements and additional features where appropriate. Do not simply accept every assumption without evaluating whether it is technically realistic.

Maintain a living specification throughout the discussion. Update it whenever a requirement changes or a new decision is made.

Before writing implementation code, provide:
1. A complete requirements specification.
2. Proposed architecture.
3. Component and subsystem breakdown.
4. Rendering and overlay approach.
5. Performance strategy.
6. Testing strategy.
7. Known limitations and unresolved decisions.

Wait for my explicit approval before implementation begins.

Do not silently make major architectural decisions when clarification is needed.

## 2. Technical research

The target is exclusively **Minecraft Java 26.2**.

Before selecting an architecture, research the actual Minecraft 26.2 implementation.

Investigate:
- Exact Minecraft version and mappings.
- Client lifecycle and initialization.
- World and chunk access.
- Client-side block and entity information.
- Available rendering APIs.
- Threading constraints.
- Relevant event and tick systems.
- Available libraries and dependencies.
- Windows compatibility.
- Existing limitations of client-accessible world information.

Do not assume that older Minecraft APIs still work.

Do not invent classes, methods, events, mappings, or APIs. Verify important technical details against the actual version.

Clearly distinguish between confirmed API behaviour and assumptions.

## 3. Architecture

Use a hybrid architecture:

- One central analysis engine.
- Multiple independently toggleable subsystems.
- Individual settings for every subsystem.
- Shared result and information structures.
- A central confidence and result-management system.
- Independent overlay and visualisation components.

Keep components loosely coupled.

Subsystems should not directly depend on one another unless there is a clear architectural reason.

One unresolved requirement is how subsystems should share information. During requirements gathering, propose and compare:
- Fully independent subsystems.
- A central shared data engine.
- A hybrid approach with controlled information sharing.

Recommend the most maintainable option.

## 4. Overlay and rendering

The module should have a separate transparent overlay window.

It should:
- Remain above the Minecraft window.
- Support click-through by default.
- Become interactive when interaction mode is enabled.
- Support configurable keybinds.
- Allow individual overlay elements to be moved.
- Support a polished, responsive visual experience.
- Support Windows flag of WDA_EXCULDEFROMCAPTURE

Research the best implementation approach for Windows and Minecraft 26.2 before selecting a rendering technology.

The overlay must not interfere with Minecraft's normal rendering or input handling.

Avoid unnecessary windows and background processes.

## 5. Analysis engine

The analysis engine should operate continuously in the background.

Requirements:
- Automatic scanning around the player.
- Configurable scanning scope.
- Balanced performance while maintaining smooth gameplay.
- Independent subsystem controls.
- Automatic initialisation and shutdown with the Minecraft client.
- Safe handling of world changes, disconnects, and shutdown.
- No blocking of Minecraft's main or rendering thread.

Benchmark possible background-processing approaches before choosing one.

Do not assume a fixed worker count is optimal. Measure performance and explain the trade-offs.

The engine must gracefully handle unavailable, incomplete, or outdated information.

When a data source becomes unavailable:
1. Retry automatically.
2. Continue with other available sources if recovery fails.
3. Report the limitation clearly.
4. Avoid presenting incomplete information as verified.

## 6. Information and results

Use normal client-accessible information only.

Track the source of every result.

Each result should support:
- A numerical confidence score from 0–100%.
- A configurable minimum confidence threshold.
- A simple source label.
- Expandable source details.
- Verification status.
- Severity.
- Current status.

Show confidence as a percentage only. Do not display a lengthy explanation beside every result.

Unverified information should be displayed separately as an **unverified observation**.

Never present assumptions as confirmed facts.

Results should disappear when the underlying observation is no longer present, according to the relevant subsystem's verification rules.

## 7. Subsystems and custom rules

Each subsystem must have:
- An ON/OFF toggle.
- Detailed individual settings.
- A clear description.
- Its current operating status.
- Performance information where useful.
- Source and verification information.

Provide a visual custom-rule editor.

The editor should support:
- Preset rules.
- User-defined conditions.
- Multiple conditions.
- AND/OR logic.
- Nested conditions where technically reasonable.
- Configurable thresholds.
- Rule enable/disable controls.
- Rule validation.

Custom rules should be capable of modifying:
- Finding visibility.
- Finding colours.
- Icons and labels.
- Notification severity.
- Notification behaviour.

Use a clear visual interface. Do not require scripting for ordinary rule creation.

## 8. 2D visualisation

The primary 2D visualisation should be a circular radar centred on the player.

Requirements:
- Player always faces upwards.
- Radar rotates with the camera.
- Small icons only for markers.
- Configurable radar range.
- Configurable zoom.
- Configurable appearance.
- Clear separation between different result categories.
- Markers removed immediately when their corresponding findings are no longer present.

Avoid excessive marker clutter.

## 9. 3D visualisation

Support a configurable combination of:
- Outlines.
- Bounding boxes.
- Floating labels.
- Icons.

Allow the user to control these individually.

Support custom colours, opacity, visibility distance, and other relevant visual settings.

Rendering must be efficient and should not cause unnecessary frame-time spikes.

## 10. HUD and notifications

Provide:
- HUD notifications.
- World-space markers.
- A fully customisable status panel.
- Configurable notification conditions.
- Configurable cooldowns.
- Different colours for different severity levels.

The status panel should allow users to select which information is displayed.

Possible information includes:
- Engine status.
- Active subsystems.
- Data-source health.
- Performance information.
- Current analysis activity.
- Result counts.
- Errors and warnings.

Do not force all information onto the screen at once.

## 11. Appearance and customisation

Provide a selection of attractive preset themes.

Also support individual colour configuration, including:
- Primary colour.
- Secondary colour.
- Accent colour.
- Background colour.
- Text colour.
- Opacity.
- Gradient options.

The appearance should feel polished, modern, clean, and consistent.

Avoid excessive visual effects that reduce readability or performance.

## 12. Keybind manager

Provide a fully configurable keybind manager.

Allow keybinds for:
- Showing and hiding the overlay.
- Enabling and disabling interaction mode.
- Toggling the radar.
- Toggling the status panel.
- Controlling individual subsystems.
- Other useful actions.

Prevent accidental keybind conflicts where possible.

## 13. Configuration and data storage

Use one configuration only.

Do not implement multiple configuration profiles.

All session analysis data must remain temporary.

Do not implement persistent databases, session exports, JSON/CSV result exports, or historical data storage.

Temporary data should be cleared when Minecraft closes.

Configuration persistence may be discussed separately from session-result storage, but do not assume that session findings should be saved.

## 14. Performance and reliability

Performance should be balanced.

Prioritise:
- Smooth Minecraft gameplay.
- Low unnecessary CPU usage.
- Efficient memory management.
- Controlled background workloads.
- Efficient rendering.
- Safe thread communication.
- Proper resource cleanup.

Benchmark alternative background-processing approaches.

Do not run expensive analysis every tick without a clear reason.

Use caching where appropriate, but ensure stale information is not presented as current.

Handle:
- World unloading.
- Dimension changes.
- Server disconnects.
- Reconnection.
- Client shutdown.
- Missing information.
- Rendering failures.
- Unexpected subsystem errors.

One subsystem failing must not crash the entire module.

## 15. Diagnostics

Provide two levels of diagnostics.

**User-facing status panel**
- Simple explanations.
- Current engine status.
- Subsystem status.
- Data-source availability.
- Warnings and errors.

**Detailed diagnostic logs**
- Technical error information.
- Subsystem failures.
- Performance measurements.
- Lifecycle events.
- Relevant debugging information.

Avoid flooding logs with repeated identical messages.

## 16. Testing

The initial testing strategy should include:

**Unit tests**
- Analysis engine.
- Result management.
- Confidence handling.
- Rule evaluation.
- Configuration.
- Notification logic.
- Subsystem lifecycle.

**Simulated Minecraft data**
- Test normal and unusual input conditions.
- Test incomplete information.
- Test unavailable sources.
- Test changing observations.
- Test multiple simultaneous subsystems.
- Test error recovery.

Do not claim that something works in-game merely because a unit test passes.

Clearly document what has and has not been verified.

## 17. Code quality and documentation

Use clean, maintainable Java.

Requirements:
- Clear package structure.
- Small, focused classes.
- Sensible interfaces.
- Strong separation of responsibilities.
- Proper error handling.
- No unnecessary dependencies.
- No giant monolithic classes.
- No unexplained magic numbers.
- No placeholder implementations presented as finished features.

Document:
- Architecture.
- Public interfaces.
- Subsystem registration.
- Settings.
- Lifecycle integration.
- Rendering integration.
- Keybind integration.
- Known limitations.
- Build and testing instructions.

## 18. Integration with the existing custom client

This is a standalone module intended for later integration.

Do not create:
- A replacement Minecraft client.
- A launcher.
- An injector.
- A new module framework.
- A replacement GUI framework.
- An unrelated configuration system.

Expose clean integration interfaces for:
- Client lifecycle.
- Module enable/disable.
- Settings.
- Keybinds.
- Rendering.
- Notifications.

The existing custom client will be inspected separately during the integration stage.

Do not invent integration classes or assume the existing client's internal structure.

Make the module adaptable without requiring its entire architecture to be rewritten.

## 19. Unresolved decisions

Do not silently decide these requirements.

Ask me about them during the requirements-gathering process:

1. **Initial analysis scope:** Should the first release focus on visualisation of information already available to the client, world-state diagnostics using normal client-accessible data, or both?

2. **Subsystem communication:** Should subsystems work independently, share information through a central engine, or use a hybrid approach?

3. **Configuration persistence:** Determine which interface settings should persist between sessions, while keeping session analysis data strictly temporary.

4. **Overlay rendering technology:** Research and recommend the best approach for Windows and Minecraft 26.2.

5. **Background processing:** Benchmark and recommend the best approach.

For every unresolved decision, explain the options and trade-offs before asking me to choose.

---

## Final instruction

Treat this as a serious software engineering project.

Research first. Ask questions one at a time. Maintain the living specification. Challenge unrealistic assumptions. Recommend improvements.

**Do not start implementation until I have reviewed and explicitly approved the complete specification and architecture.**