# Backend Current State (Handlers + Join Flow + Variables + Broadcasts)

## 1) Core request handlers (client entry points)
- **InitHandler**: main entry on `init`. Sets user variables (gender, playerID, avatarName, platform, clothes, etc.), sets position/direction, sends `initQueue`, `roles`, `questlistroom`, and full `init` response, then joins room and sends `restartServer` + `privateChatUpdate`.
- **RoomJoinCompleteHandler**: responds to `roomjoincomplete` and sends `displayAd`.
- **ClothListHandler**: returns `clothlist` with `type=CLOTH` and `items` array.
- **SaveBaseClothesHandler**: accepts initial avatar selection and saves clothes, then updates user variables (`avatarName`, `gender`, `clothes`).
- **WalkRequestHandler / WalkFinalRequestHandler**: update user variables `target` and later `position`, `status`.
- **UseDoorHandler / TeleportHandler / UseHouseDoorHandler / UseObjectDoorHandler**: perform room transition and build room payloads.
- **GenericRequestHandler**: bulk responder for non-critical commands; also updates user variables for mood/status/hand changes.

## 2) Join-room flow (init → join → room vars)
1) `InitHandler.processInit()` builds/validates clothes, sets user vars (including `clothes` as JSON string array), sets `position` and `direction`, and logs RenderGateAudit (`stage=INIT`).
2) `InitHandler` ensures room vars via `ensureMandatoryRoomVars(...)` and calls `joinRoom()` for `street01`.
3) `ServerEventHandler.onUserJoinRoom()` sets room vars (`doors`, `bots`, `grid`, `isInteractiveRoom`, `isGameStarted`, `isGameEnded`) and runs RenderGateAudit (`stage=ROOM_JOIN`).
4) Client follows up with `roomjoincomplete` → `displayAd` from `RoomJoinCompleteHandler`.

## 3) User variables set during init (local player)
- Identity: `playerID`, `avatarName`, `gender`, `universeKey`.
- Appearance: `clothes` (JSON string array of clip keys), `avatarSize`, `hand`, `smiley`.
- Movement: `position`, `direction`, `target`, `speed`, `status`.
- Metadata: `platform`, `imgPath`, `mood`, `statusMessage`, `optimizedAssetKey`, `roles`.

## 4) Room variables and broadcasts
- **ensureMandatoryRoomVars(...)**: sets `roomKey`, `width`, `height`, `type`, `roomTitle`, `isInteractiveRoom`, `doors`, `bots`, `grid` if missing. Emits `[ROOM_VARS_GATE]` and `[ROOM_VARS_BROADCAST]` logs.
- **ServerEventHandler.onUserJoinRoom(...)**: always broadcasts `doors`, `bots`, `grid`, `isInteractiveRoom`, `isGameStarted`, `isGameEnded` and logs `[ROOM_VARS_BROADCAST]`.

## 5) Timing-sensitive points
- Local avatar render depends on user variables being ready **before** the `roomJoin` event, and on room variables (`doors`, `bots`, `grid`, `isInteractiveRoom`) being ready at `roomJoin` time.
- Door/teleport handlers call `ensureMandatoryRoomVars(...)` before `joinRoom()`, which should satisfy the timing for room vars.

## 6) Log anchors to trace join + render gates
- `[USER_VARS_SET] stage=INIT ...` (user variables set)
- `[GUEST_CREATED] user=... guest=true ...` (guest creation)
- `[JOIN_ROOM] stage=INIT ...` (join call)
- `[ROOM_VARS_GATE] stage=...` (room vars presence)
- `[ROOM_VARS_BROADCAST] stage=...` (room vars broadcast)
- `[RENDER_AUDIT] stage=INIT|ROOM_JOIN ...` (render gate audit)
