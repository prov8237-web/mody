# Backend Movement Fix Validation

## Expected server logs (walk flow)
1) **Walk request**
   - `[MOVE_REQ] rid=<rid> user=<name> from=<x,y> to=<x,y> dist=<d> delay=<ms> dir=<dir>`
   - `[MOVE_VARS_META] stage=REQUEST speedSet=<true|false> delay=<ms>`
   - `[MOVE_RESP] cmd=walkrequest type=RESPONSE rid=<rid> keys=delay`
   - `[MOVE_ACK] stage=REQUEST delay=<ms>`
2) **Walk final**
   - `[MOVE_FINAL] rid=<rid> user=<name> pos=<x,y> dir=<dir> source=<client|target|property>`
   - `[MOVE_VARS_SET] stage=FINAL position=<x,y> direction=<dir> status=idle source=<client|target|property>`
   - `[MOVE_RESP] cmd=walkfinalrequest type=RESPONSE rid=<rid> keys=nextRequest`
   - `[MOVE_ACK] stage=FINAL user=<name> position=<x,y>`

## Expected client USER_VARIABLES_UPDATE samples
- **target update (movement trigger):**
  - key: `target`
  - value: `"<x>,<y>"`
- **position update (state only):**
  - key: `position`
  - value: `"<x>,<y>"`
- **direction update (facing):**
  - key: `direction`
  - value: `<1-8>`
- **status update (animation state):**
  - key: `status`
  - value: `"idle"`

## Notes
- Client movement animates only on `target` user variable updates.
- `walkrequest` response must include `delay` for `WalkModel` timer.
- `position` alone does not animate; it only updates state.
- Delay should scale with distance (e.g., 1 tile ≥ minDelay, 10 tiles noticeably higher).

## Expected client console sequence (per click)
1) `NET_OUT` walkrequest `rid=N` data contains `x,y` (and optional `door`).
2) `NET_IN` walkrequest `rid=N` (NOT PUSH) payload includes only `delay`.
3) `userVariablesUpdate` changed=`target`, then changed=`direction`.
4) `NET_OUT` walkfinalrequest `rid=M` data `{}` or minimal.
5) `NET_IN` walkfinalrequest `rid=M` (NOT PUSH) payload includes only `nextRequest=100`.
6) `userVariablesUpdate` changed=`position`, then changed=`direction` (and optional `status`).

## Manual verification checklist
1) Join as guest, confirm spawn at `20,30` (or current state position).
2) Click a nearby tile (1-2 tiles); check delay >= minDelay and logs include `MOVE_REQ` with distance.
3) Click a far tile (e.g., 40,13); confirm delay is higher and `MOVE_RESP` is RESPONSE (rid not -1).
4) Confirm client console shows non-PUSH responses with matching rid and only the official keys.
