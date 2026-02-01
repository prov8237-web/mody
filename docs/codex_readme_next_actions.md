# Codex Readme: Local Player Spawn/Render Gates + Next Actions

## 1) Client expected avatar contract (local player)
### Required user variables (must exist before `engine.changeScene()` on `roomJoin`)
- `clothes`: JSON **string array** of clip keys (e.g., `["A_2","B_1"]`).
- `gender`: string ("m"/"f").
- `position`: string in `"x,y"` format (integers expected).
- `direction`: int.

### Optional user variables (applied if present)
- `roles`, `speed`, `smiley`, `hand`, `avatarSize`, `status`, `avatarName`, `platform`.

### Room variables required for scene build
- `isInteractiveRoom` (boolean; must be true for the join handler to proceed).
- `doors`, `bots`, `grid` (scene bootstrap inputs).

### Local player spawn/render gate (decisive condition)
Local avatar creation occurs only if:
1) SFS connection is active, **and**
2) `SceneCharacterComponent` exists, **and**
3) `RoomModel.disableAddChar == false`.

If any of these gates fail, the local avatar is never instantiated, even if bots appear.

### Timing/order constraints (client expectations)
- `sfs.mySelf` must already contain `clothes`, `gender`, `position`, `direction` **before** `RoomController.onJoinRoom()` triggers `engine.changeScene()`.
- `processSceneData()` uses `AvatarModel.position` and `AvatarModel.direction` without validation; malformed values can block proper placement.
- Room variables (`doors`, `bots`, `grid`, `isInteractiveRoom`) must be present **at join time** to avoid missing scene inputs.

## 2) Top 15 decisive evidence points (from prior doc scan)
1) `RoomController.onJoinRoom()` returns early when `isInteractiveRoom` is false, preventing any scene load.
2) `RoomController` hydrates `AvatarModel` from `sfs.mySelf` (`clothes`, `gender`, `position`, `direction`) before `engine.changeScene()`.
3) `SceneProcessDataComponent.processSceneData()` is responsible for local avatar creation.
4) Local avatar spawn is gated by `SceneCharacterComponent` presence and `RoomModel.disableAddChar`.
5) `SceneProcessDataComponent` checks `sfs.isConnected` before proceeding.
6) `Character.init(...)` consumes `AvatarModel.clothesOn` and `gender`.
7) `Character.reLocate(...)` uses `AvatarModel.position` + `direction` without guard rails.
8) `AvatarController.onClothesUpdate()` updates clothes via user variable after spawn.
9) `RoomController.addChar(...)` spawns remote avatars using user variables (`clothes`, `gender`, `position`).
10) `RoomModel.disableAddChar` defaults to false but can be flipped to block local spawn.
11) `RoomController` and `ServiceModel` register `roomJoin`, `userEnterRoom`, `userVariablesUpdate` events that drive render.
12) `SceneCharacterComponent` registers the local avatar in `myChar` and scene registry.
13) `ClothModel.load(...)` consumes item-object payloads (inventory) but does not gate local spawn.
14) `InventoryProcessor` expects full item metadata, but this is separate from initial render.
15) `Room variables` (`doors`, `bots`, `grid`) are used to build the scene and bots; they do not depend on local user variables.

## 3) Next actions (backend-first)
1) Confirm backend user vars for local player are present **before** join and match the string-array clothes contract.
2) Confirm room variables (`doors`, `bots`, `grid`, `isInteractiveRoom`) are set **before** join for init + door/teleport flows.
3) Use RenderGateAudit logs to verify both user-var shape (string array) and room-var timing.
4) If local avatar still does not render, capture server logs for `[RENDER_AUDIT]`, `[ROOM_VARS_GATE]`, and join traces to identify which gate fails.

## 4) Likely root causes (ranked, with evidence anchors)
1) **`clothes` user var shape mismatch or invalid JSON**
   - Evidence: client reads `clothes` as JSON string array and uses it for `Character.init()`.
   - Backend must provide string-array; if an object-array or invalid JSON is sent, local spawn breaks while bots still render.
2) **Room vars timing gap at join (`doors`, `bots`, `grid`, `isInteractiveRoom`)**
   - Evidence: client `roomJoin` handler uses these to build the scene; if they arrive after join, scene can be missing grid/door data.
3) **Local spawn gate disabled (`disableAddChar` true or missing SceneCharacterComponent)**
   - Evidence: `processSceneData()` only spawns local character if component exists and `disableAddChar` is false.
4) **Invalid/missing `position` or `direction` user vars**
   - Evidence: client splits `position` and uses it without validation for `reLocate()`.
5) **SFS connection not ready during `processSceneData()`**
   - Evidence: `processSceneData()` returns early if `sfs.isConnected` is false.
