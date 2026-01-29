# Backend Movement Gap Analysis (Client vs Server)

## Client trigger summary
- The client animates movement when the **user variable** `target` updates (`"x,y"`).
- The `walkrequest` response only needs a `delay` to trigger `walkfinalrequest` on the client.
- `position` updates do **not** trigger animation; they only dispatch a local event.

## Backend current behavior
### `WalkRequestHandler`
- Reads `x`, `y` from request data.
- Sets user variable `target` to `"x,y"`.
- Responds to `walkrequest` with `delay` (now also includes `target`).

### `WalkFinalRequestHandler`
- Reads target from stored state/user property.
- Sets user variables:
  - `position` = `"x,y"`
  - `status` = `"idle"`
  - `target` = `""` (clears target)
- Responds to `walkfinalrequest` with empty payload.

## Most likely gap causing “no movement”
1) **`target` user variable update not received by client**
   - Movement animation is keyed off `target` update, not `position`.
   - If `target` is never set or not broadcast, the avatar remains static.

2) **`target` format mismatch**
   - Client expects exactly `"x,y"` string. If malformed, `onTargetPositionUpdate()` returns without moving.

3) **Scene component gate**
   - If `SceneCharacterComponent` is missing, the handler returns early and movement never starts.

## Backend-first fix (implemented)
- Added explicit logs for:
  - `[MOVE_VARS_SET]` when `target`/`position` are written.
  - `[MOVE_BROADCAST]` to confirm variable broadcast intent.
  - `[MOVE_ACK]` for `walkrequest` and `walkfinalrequest` responses.

## Verification strategy
- Confirm server logs show `MOVE_VARS_SET` for `target` before the client is expected to move.
- If client still does not move, inspect `USER_VARIABLES_UPDATE` events and confirm `target` is present and formatted as `"x,y"`.
