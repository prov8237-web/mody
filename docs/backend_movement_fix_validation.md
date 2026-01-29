# Backend Movement Fix Validation

## Expected server logs (walk flow)
1) **Walk request**
   - `[MOVE_VARS_SET] stage=REQUEST from=<x,y> target=<x,y> dist=<d>`
   - `[MOVE_VARS_META] stage=REQUEST speedSet=<true|false> delay=<ms>`
   - `[MOVE_ACK] stage=REQUEST delay=<ms>`
2) **Walk final**
   - `[MOVE_VARS_SET] stage=FINAL position=<x,y> status=idle source=<client|target|property>`
   - `[MOVE_ACK] stage=FINAL user=<name> position=<x,y>`

## Expected client USER_VARIABLES_UPDATE samples
- **target update (movement trigger):**
  - key: `target`
  - value: `"<x>,<y>"`
- **position update (state only):**
  - key: `position`
  - value: `"<x>,<y>"`
- **status update (animation state):**
  - key: `status`
  - value: `"idle"`

## Notes
- Client movement animates only on `target` user variable updates.
- `walkrequest` response must include `delay` for `WalkModel` timer.
- `position` alone does not animate; it only updates state.
- Delay should scale with distance (e.g., 1 tile ≥ minDelay, 10 tiles noticeably higher).
