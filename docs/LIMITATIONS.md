# Known limitations & honest verification status

## Fundamental (client-side) limits
- Only **loaded** chunks can be analyzed. Nothing is detectable beyond render distance
  or through unloaded chunks. This is a hard limit of a legitimate client.
- The **seed-diff** refinement in the terrain scanner only activates when a world seed is
  known (singleplayer, `/seed`, or manual entry). On most multiplayer servers it stays off,
  and the heuristic detectors carry detection on their own.
- Heuristic detectors have inherent false positives. These are mitigated by confidence
  **fusion** and the global **confidence floor** (default 75%): isolated weak hits never
  draw an overlay.
- The **structure** scanner is deliberately conservative (entity-cluster heuristic), since
  reliable structure bounds are not available to a normal client.

## Verification status
- The engine, all scanners, fusion, temporal persistence, rules, notifications, settings,
  guidance, village discrimination and lifecycle are covered by **81 unit/integration tests
  against simulated world data**. Passing tests do **not** prove in-game behaviour.
- `src/mc` adapters use the deobfuscated **26.2** names and are compile-verified only inside
  a real client. A few render hooks (world-space box/label drawing, the exact render/tick
  events) are left as explicit integration points rather than invented API calls.

## Performance
- Scanning runs off the render thread on an adaptive, capped worker pool with a per-cycle
  block budget. The single shared scope sweep visits each block once per cycle for all
  block-based scanners. Budget is spent from the world bottom upward, so deep/underground
  areas are prioritized when the budget can't cover full height at large radius.
- Defaults: 8-chunk radius, full world height, balanced aggressiveness, ~20-tick cadence.
  All are user-adjustable. Expensive analysis is never run every tick.

## Data policy
- One configuration, no profiles. Interface settings persist via the host bridge only.
- Findings are **temporary** (RAM only) and cleared on world change / client close.
- No persistent result database, no JSON/CSV exports, no history.
