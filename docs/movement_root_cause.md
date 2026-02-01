# Movement Root Cause (Client/Backend Contract)

## Evidence from client code
- **Movement trigger:** `AvatarController.onTargetPositionUpdate()` listens to USER_VARIABLES_UPDATE for `target` and calls `Character.walk(...)` when `target` parses to `"x,y"`. (`Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarController.as`)
- **Walk timing:** `WalkModel.onWalkStart()` uses the `delay` field from the `walkrequest` response to schedule `walkfinalrequest`. (`Client/snl.779.swf/scripts/com/oyunstudyosu/model/WalkModel.as`)
- **Position update behavior:** `AvatarController.onPositionUpdate()` only dispatches a state event; it does **not** initiate walking. (`Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarController.as`)

## Why the backend was failing
1) **Fixed delay too small**: a constant 200ms causes `walkfinalrequest` to fire almost immediately, often before the client finishes pathing/animation.
2) **Responses were treated as PUSH (rid=-1)**: walk handlers replied with payloads not matching official keys, and `walkfinalrequest` used a raw `send(...)` path (no request-bound response helper), which appears as PUSH (rid=-1) in the client console and can bypass WalkModel’s response scheduling.
3) **Movement depends on `target` user var**: if `target` is cleared too early or never broadcast to the mover, the local avatar never animates.
4) **Missing timing metadata**: without distance-aware delays, far clicks animate no longer than near clicks, which collapses movement.

## Backend-first fix
- Compute delay based on grid distance (`abs(dx)+abs(dy)`), clamp to safe bounds.
- Always set `target` user var before responding.
- Return official-parity response payloads (`delay` for walkrequest, `nextRequest` for walkfinalrequest).
- Emit direction updates alongside target/position to match official `userVariablesUpdate` keys.
- On final, set `position` + `status=idle` but **do not clear `target` in the same tick**.

## Expected result
- Client receives USER_VARIABLES_UPDATE for `target` and starts walking.
- `walkfinalrequest` arrives after a reasonable delay, updating `position` and `status` after animation begins.
