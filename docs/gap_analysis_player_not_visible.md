# Gap Analysis: MAIN (Local) Player Avatar Not Visible While Bots Render

## Scope & Required Inputs
This analysis **strictly compares** client render requirements to backend behavior to explain why the **local player** can fail to render while bots do. Sources required by the task are used:

- Client contract docs:
  - `docs/client_avatar_contract.md`
  - `docs/client_expected_payload_keys.json`
  - `docs/client_player_rendering_map.md`
- Backend implementation:
  - `Backend/RenderGateAudit.java`
  - `Backend/InitHandler.java`
  - `Backend/ServerEventHandler.java`
  - `Backend/ClothListHandler.java`
  - Room join handlers (`Backend/UseDoorHandler.java`, `Backend/TeleportHandler.java`, `Backend/UseHouseDoorHandler.java`, `Backend/UseObjectDoorHandler.java`)

---

## 1) Exact Local Render Gates (Client-Side)
These are the **hard gates** the local avatar must pass for visibility. They are **not** required for bots, which are spawned from room variables.

### Gate A — Interactive room gate
`RoomController.onJoinRoom()` **returns early** when `isInteractiveRoom` is explicitly `false`; scene load never starts. Expected room variable in the join payload is `isInteractiveRoom:Boolean`.【F:docs/client_avatar_contract.md†L19-L27】【F:docs/client_expected_payload_keys.json†L69-L76】

### Gate B — Scene character component gate
`SceneProcessDataComponent.processSceneData()` only creates the local `Character` when `SceneCharacterComponent` exists **and** `RoomModel.disableAddChar == false`. If the gate fails, the local player is never created.【F:docs/client_player_rendering_map.md†L17-L26】【F:docs/client_avatar_contract.md†L31-L37】

### Gate C — Required user variables gate (local model hydration)
Before `engine.changeScene()`, `RoomController` reads **local** user variables from `sfs.mySelf`:
- `clothes` (JSON string array)
- `gender` (string)
- `position` ("x,y")
- `direction` (int)
Missing or malformed values block correct initialization or placement in `processSceneData()`.【F:docs/client_avatar_contract.md†L29-L54】

**Why bots can render while the local player does not:** bots are built from room variables (`bots`, `doors`, `grid`) and do **not** depend on local user variables. The local player uniquely depends on `sfs.mySelf` values, so a mismatch in **local** user vars can suppress local rendering even when bots appear.【F:docs/client_player_rendering_map.md†L29-L47】【F:docs/client_expected_payload_keys.json†L69-L89】

---

## 2) Expected vs Actual (Backend) — Keys, Types, Timing
This section compares what the client **expects** to what the backend **actually sends/sets**.

### 2.1 User variables required at roomJoin time (local player)
**Expected (client contract):**
- `clothes`: JSON **string array** (e.g., `["A_1","B_1"]`).
- `gender`: string.
- `position`: "x,y" string.
- `direction`: int.
- Timing: **must be present before** `engine.changeScene()` is triggered on `roomJoin`.【F:docs/client_avatar_contract.md†L29-L54】【F:docs/client_expected_payload_keys.json†L5-L27】

**Actual (backend):**
- `InitHandler.processInit()` sets user vars in multiple batches and then joins the room immediately afterwards. The `clothes` user var is set to `clothesJson`, which is serialized from an **array of objects** (`{clip, subType, productID, ...}`), not a string array.【F:Backend/InitHandler.java†L110-L189】【F:Backend/InitHandler.java†L260-L349】

**Mismatch:** `clothes` user var **type/shape** diverges from the client’s expected JSON string array (string keys), which is consumed during local avatar hydration.

---

### 2.2 Room variables required at roomJoin time (scene setup)
**Expected (client contract):**
- `doors`, `bots`, `grid`, `isInteractiveRoom` available during the room-join path so the scene/grid/bot data is ready for processing.【F:docs/client_expected_payload_keys.json†L69-L89】

**Actual (backend):**
- `ServerEventHandler.onUserJoinRoom()` sets `doors`, `bots`, `grid`, `isInteractiveRoom` **after** the room join event fires (server-side), which means the first `roomJoin` event can arrive at the client before these vars are attached to the room state.【F:Backend/ServerEventHandler.java†L41-L72】
- `InitHandler.ensureMandatoryRoomVars()` only ensures `roomKey`, `width`, `height`, `type`, `roomTitle`, **not** `doors`, `bots`, `grid`, or `isInteractiveRoom`.【F:Backend/InitHandler.java†L445-L486】

**Mismatch:** timing gap where the local client can process `roomJoin` before `doors/bots/grid/isInteractiveRoom` are available, which can lead to missing grid/door data during scene setup.

---

### 2.3 Join-room flow events (backend)
**Observed backend flow:**
1. `InitHandler.processInit()` → `setUserVariables(...)` (multiple calls) → send `init` response → `getApi().joinRoom(...)`.【F:Backend/InitHandler.java†L150-L349】
2. SFS fires `USER_JOIN_ROOM` → `ServerEventHandler.onUserJoinRoom()` sets room variables (`doors`, `bots`, `grid`, `isInteractiveRoom`).【F:Backend/ServerEventHandler.java†L41-L72】
3. `RoomJoinCompleteHandler` responds to `roomjoincomplete` request with empty payload + `displayAd` (no spawn logic).【F:Backend/RoomJoinCompleteHandler.java†L10-L48】
4. Door/teleport joins (`UseDoorHandler`, `TeleportHandler`, `UseHouseDoorHandler`, `UseObjectDoorHandler`) call `getApi().joinRoom` and **do not set room vars before join** (room vars still rely on `USER_JOIN_ROOM`).【F:Backend/UseDoorHandler.java†L8-L45】【F:Backend/TeleportHandler.java†L8-L32】【F:Backend/UseHouseDoorHandler.java†L8-L32】【F:Backend/UseObjectDoorHandler.java†L8-L32】

**Implication:** the local client **can** reach `roomJoin` before the required room variables are attached, while bots render later when room vars update. This doesn’t block bots, but can block local spawn in some edge cases if grid/scene data are missing at the moment local hydration runs.

---

## 3) Exact Render Gate Failures for LOCAL Player
Based on the contract vs backend behavior, the following gates can fail **only** for the local player:

### Failure 1 — `clothes` user var shape mismatch (highest confidence)
- **Gate impacted:** Local user-variable hydration (Gate C).
- **Expected:** JSON **string array** of clip keys.
- **Actual:** JSON **array of objects** (`clip`, `subType`, etc.) serialized into the `clothes` user variable in `InitHandler.processInit()`.【F:Backend/InitHandler.java†L110-L189】
- **Why bots still render:** bots are spawned from `bots` room variable, not `sfs.mySelf.clothes`.

### Failure 2 — Room variables arrive after `roomJoin` (timing gap)
- **Gate impacted:** Interactive room gate and scene setup inputs.
- **Expected:** `isInteractiveRoom`, `doors`, `bots`, `grid` available at join time.
- **Actual:** set only in `ServerEventHandler.onUserJoinRoom()` (after join).【F:Backend/ServerEventHandler.java†L41-L72】
- **Risk to local:** local avatar creation depends on having a valid scene/grid pipeline before `processSceneData()`; missing grid/doors during the initial join can short-circuit placement or map-derived defaults.

### Failure 3 — Render audit does not validate clothes JSON shape (diagnostic gap)
- **Gate impacted:** Diagnosis (not runtime).
- **Expected:** audit validates the JSON array is **string keys**.
- **Actual:** audit validates user-variable types only and uses `state.getClothesItems()` (object-array), so it can report success while the user variable itself is incompatible with the client’s JSON parsing expectations.【F:Backend/RenderGateAudit.java†L14-L97】

---

## 4) Prioritized Gap List (Evidence + Minimal Backend Fix + Verification)

### Gap 1 — `clothes` user var shape (Critical)
- **Expected (client):** JSON **string array** of clothes keys for local render.【F:docs/client_avatar_contract.md†L46-L48】
- **Actual (backend):** JSON **object array** assigned to user variable in `InitHandler.processInit()`.【F:Backend/InitHandler.java†L110-L189】
- **Minimal backend fix:** Serialize and store `clothes` as a **string array of clip keys** (e.g., `["A_2","B_2"]`) in the user variable; keep object-array only for `clothes` payloads and `clothlist` response.
- **Verify:** `[RENDER_AUDIT] stage=INIT` should show `clothes:String` and an additional log (or new audit) should confirm the parsed JSON is an array of strings.【F:Backend/RenderGateAudit.java†L14-L97】

### Gap 2 — Room variables timing (High)
- **Expected (client):** `doors`, `bots`, `grid`, `isInteractiveRoom` available before/at `roomJoin` handling.【F:docs/client_expected_payload_keys.json†L69-L89】
- **Actual (backend):** set in `ServerEventHandler.onUserJoinRoom()` after the join event fires.【F:Backend/ServerEventHandler.java†L41-L72】
- **Minimal backend fix:** set these room variables **before** `joinRoom()` (e.g., in `InitHandler.processInit()` and in door/teleport handlers) or augment `ensureMandatoryRoomVars()` to include them prior to join.
- **Verify:** client `roomJoin` handler should see `currentRoom.containsVariable("grid") == true` immediately on join; server logs should show room vars set before `joinRoom`.

### Gap 3 — Missing audit for JSON shape (Medium)
- **Expected:** audit identifies mismatched JSON payload shape for `clothes`.
- **Actual:** audit only checks variable types and then logs from `state.getClothesItems()` (object list), not the user-variable JSON itself.【F:Backend/RenderGateAudit.java†L14-L97】
- **Minimal backend fix:** parse the `clothes` user variable JSON, log `clothesKeysLen` and first key; emit failure if it is not a string array.
- **Verify:** `[RENDER_AUDIT_FAIL]` should trigger if `clothes` JSON is an object array or malformed.

---

## 5) Conclusion (Strict)
The **most direct, client-visible gap** is the `clothes` user-variable **shape mismatch**: the local avatar render pipeline expects a JSON string array while the backend provides an object array. This uniquely affects the **local** player (bots render from room variables) and is the highest-confidence explanation for “bots render, main player invisible.” Room-variable timing is the next-highest-risk gap because those variables are only attached after join, which can deprive the local player of grid/door data during the initial render pass.【F:Backend/InitHandler.java†L110-L189】【F:Backend/ServerEventHandler.java†L41-L72】【F:docs/client_avatar_contract.md†L29-L54】
