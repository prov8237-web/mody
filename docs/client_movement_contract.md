# Client Movement Contract (ActionScript)

## Movement pipeline (high-level)
1) **Client issues walk request** via `Character.walkRequest(x, y, door)` → `serviceModel.requestData("walkrequest", {x, y, door})`.
2) **Server responds** to `walkrequest` with a payload containing `delay`.
3) **WalkModel listens** for the `walkrequest` response and schedules `walkfinalrequest` after `delay`.
4) **Client animates movement** when the **user variable** `target` is updated (USER_VARIABLES_UPDATE): `AvatarController.onTargetPositionUpdate()` reads `target` (`"x,y"`) and calls `Character.walk(...)`.

## Required keys + types (client expectations)
### Extension response: `walkrequest`
- `delay` (number/string parseable to int): used by `WalkModel` to schedule `walkfinalrequest`.

### User variables update (movement trigger)
- `target` (string, `"x,y"`): **required** to trigger `Character.walk(...)` for both local and remote users.
- `speed` (number): optional; if present, assigned to character before walking.

### User variables update (position state)
- `position` (string, `"x,y"`): tracked for state and event dispatch; does **not** animate movement by itself.
- `status` (string): used for action/animation state (e.g., idle/sit/dance) and may affect direction handling.

## Event sources and handlers (exact classes)
### Walk request sender
- **Class:** `Character`
- **Method:** `walkRequest(param1:int, param2:int, param3:MovieClip = null)`
- **File:** `Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as`

### Walk response listener (delay → walkfinalrequest)
- **Class:** `WalkModel`
- **Method:** `onWalkStart(param1:Object)` → `walkTimerComplete()`
- **File:** `Client/snl.779.swf/scripts/com/oyunstudyosu/model/WalkModel.as`

### Movement trigger (user var update)
- **Class:** `AvatarController`
- **Method:** `onTargetPositionUpdate(param1:Object)`
- **File:** `Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarController.as`

### Position update (state only, not movement)
- **Class:** `AvatarController`
- **Method:** `onPositionUpdate(param1:Object)`
- **File:** `Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarController.as`

## Gates / conditions
- `AvatarController.onTargetPositionUpdate()` returns early if:
  - no user object,
  - scene is missing, or
  - `SceneCharacterComponent` is absent.
- Movement uses `target` **only** if it parses into exactly two values (`x,y`).

## Implication for server
- The **only guaranteed movement trigger** is the `target` user-variable update.
- Updating `position` alone does **not** animate; it only fires `USER_POSITION_UPDATED` event.
