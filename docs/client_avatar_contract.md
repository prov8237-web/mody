# Client Avatar Rendering Contract (Local Player)

## Scope
This document traces the **local player** avatar render path end-to-end and captures the **runtime data contract** required for the avatar to be created and shown in a room. Citations point to the exact ActionScript sources.

---

## 1) End-to-end flow: event → model update → avatar creation → render

### A. Room join event (SmartFox)
* `RoomController.onJoinRoom()` is wired to the SFS `roomJoin` event and is **the entry point** for room setup. It **returns early** when `isInteractiveRoom` is explicitly false; otherwise it starts room loading. This can prevent any scene setup for non-interactive rooms.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L437-L455】

### B. Local avatar model hydration (from `sfs.mySelf` user variables)
* Prior to `engine.changeScene()`, `RoomController` pulls user variables from `sfs.mySelf` and **hydrates the local avatar model**:
  * `clothes` → JSON array (stored in `AvatarModel.clothesOn`).
  * `gender` → string.
  * `position` → "x,y" string.
  * `direction` → int.
* These assignments happen **before** the scene is changed and are used later for avatar creation.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L423-L429】

### C. Scene build and local avatar creation
* `SceneProcessDataComponent.processSceneData()` is called after room assets load and is responsible for **creating the local `Character`**. It uses `AvatarModel.gender` and `AvatarModel.clothesOn` to initialize the avatar and uses `AvatarModel.position` + `AvatarModel.direction` to place it via `reLocate()`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L257-L346】

### D. Runtime updates after render
* `AvatarController.onClothesUpdate()` listens to the **user variable `clothes`** for live updates and applies it to the character via `setAccesory()`. If it is the local user, it also updates `AvatarModel.clothesOn`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarController.as†L674-L694】

---

## 2) FIRST render gate (local avatar creation)
The local avatar is created in `SceneProcessDataComponent.processSceneData()` only when **all** of the following are true:

1. **SmartFox connection is alive**: if `sfs.isConnected` is false, the function returns early (no rendering).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L266-L270】
2. **Scene has a `SceneCharacterComponent`** and `RoomModel.disableAddChar` is false:
   * `if (_loc1_ != null && !Sanalika.instance.roomModel.disableAddChar) { ... }`【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L309-L346】
   * `RoomModel.disableAddChar` defaults to `false` but can be set elsewhere to block rendering.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomModel.as†L21-L26】

**Interpretation:** If the room is marked non-interactive, the client may not even start loading (`onJoinRoom` returns early). If the room is interactive but `disableAddChar` is true or the SFS connection is not active, the local character is **never created**, even if movement events appear to work in logs.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L437-L455】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L266-L351】

---

## 3) Runtime data contract for local avatar render

### Must-have fields (hard requirements)
These are accessed **without guard rails** for the local player when the scene changes and in `processSceneData()`:

1. `clothes` (UserVariable; JSON string → array)
   * Read on room join and parsed immediately to initialize `AvatarModel.clothesOn` (used in `Character.init`).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L423-L426】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L320-L321】
2. `gender` (UserVariable; string)
   * Read on join; passed to `Character.init` as `sex`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L423-L425】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L320-L321】
3. `position` (UserVariable; string, "x,y")
   * Read on join and later split for `reLocate` without validation; invalid format can break positioning or crash rendering logic.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L427-L428】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L337-L343】
4. `direction` (UserVariable; int)
   * Read on join and used in `reLocate`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L427-L428】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L341-L343】

### Optional fields (applied when present)
* `roles` (UserVariable; string) → `Character.setRoles()` for permission-based behavior.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L316-L321】
* `speed` (UserVariable; number) → default `1` if missing.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L322-L323】
* `smiley` (UserVariable; string) → `setSmiley()` if present.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L325-L328】
* `hand` (UserVariable; string) → `useHandItem()` if present.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L329-L332】
* `avatarSize` (UserVariable; number) → overrides size if present.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L333-L335】

### Defaults / fallback logic
* `speed` defaults to `1` if the user variable is missing.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L322-L323】
* `disableAddChar` defaults to `false` (so the avatar is spawned unless toggled).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomModel.as†L21-L26】

---

## 4) Clothes channels: initial render vs. updates

### A. **UserVariable `clothes`** (JSON string)
* **Used for initial render**: `RoomController` reads `sfs.mySelf.getVariable("clothes")`, parses JSON, and assigns it to `AvatarModel.clothesOn` *before* `engine.changeScene()`. This array is passed directly into `Character.init()` during local spawn.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L423-L429】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L320-L321】
* **Used for later updates**: `AvatarController.onClothesUpdate()` listens for the same user variable and applies it via `setAccesory()` on the existing character; it updates `AvatarModel.clothesOn` for the local player as well.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarController.as†L674-L694】

### B. **`clothes` SFSObject `{ type, items }` (inventory-style payload)**
* There is **no direct usage** of a `clothes` SFSObject for avatar creation in the rendering path.
* The closest **items-style payload** is `ClothModel.load(param1.items)`, which expects an object containing `items` and builds the **wardrobe list** from fields like `id`, `quantity`, `active`, `base`, `clip`, `lifeTime`, `timeLeft` (not for direct render at spawn).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/cloth/ClothModel.as†L25-L57】

**Conclusion:** Local spawn uses **UserVariable `clothes` JSON array**; items-style `clothes` payloads are used by the **inventory/wardrobe** and do **not** gate initial render in this client slice.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L423-L429】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L320-L321】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/cloth/ClothModel.as†L25-L57】

---

## 5) Clothes item fields (best-effort contract)
These fields are **observed** in the wardrobe pipeline and related inventory processing (useful for validating `items` payloads):

* `clip` (string): cloth asset key (e.g., `gender_clip` mapping is created in `ClothModel.load`).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/cloth/ClothModel.as†L48-L57】
* `id` (int/string), `quantity` (int), `active` (bool/int), `base` (bool/int).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/cloth/ClothModel.as†L48-L57】
* `lifeTime` / `timeLeft` (numbers; defaulted to `-1` if not provided).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/cloth/ClothModel.as†L48-L57】

For **inventory items** in general (non-clothes), `InventoryProcessor` expects fields:
* `transferrable`, `productID`, `clip`, `lifeTime`, `timeLeft`, `subType`, `createdAt`, `id`, `quantity`, `roles` (for permissions).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/inventory/InventoryProcessor.as†L31-L49】

---

## 6) Timing requirements

1. **Before `engine.changeScene()`** (room join):
   * `sfs.mySelf` must already expose `clothes`, `gender`, `position`, `direction`. These are pulled immediately after `roomJoin` and used to seed `AvatarModel`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L423-L429】
2. **Before `processSceneData()`** (scene processing):
   * `AvatarModel` must contain a valid `position` string; it is split and used without validation to place the local `Character`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L337-L343】
3. **After render** (runtime updates):
   * Later `clothes` updates are applied via user variable events, not by re-spawning the avatar.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarController.as†L674-L694】

---

## 7) Common failure modes & where rendering stops

### Failure mode A — **room not interactive**
* If `isInteractiveRoom` is false on `roomJoin`, the handler returns early and **scene loading never starts**, so no avatar is created.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L437-L455】

### Failure mode B — **SFS connection not ready**
* `processSceneData()` aborts if `sfs.isConnected` is false, preventing local avatar creation.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L266-L270】

### Failure mode C — **`disableAddChar` set**
* If `RoomModel.disableAddChar == true`, the local `Character` is never created (logs “Character creation disabled for this room”).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L309-L351】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomModel.as†L21-L26】

### Failure mode D — **missing/invalid `clothes` or `position`**
* `clothes` is parsed immediately on room join; missing or invalid JSON can throw or lead to a null/empty array, which then drives `Character.init()`.
* `position` is split and parsed without validation; malformed strings (not “x,y”) can lead to invalid coordinates or runtime errors during `reLocate()`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L423-L429】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L337-L343】

### Failure mode E — **missing `SceneCharacterComponent`**
* Local spawn only happens if the scene has a `SceneCharacterComponent`; if it is missing, the spawn block is skipped entirely.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L309-L351】

---

## 8) Minimal local avatar contract (cheat sheet)

**Required (must be present before `engine.changeScene()`):**
* `clothes` → JSON string of clothing keys (e.g., `["A_1", "B_1", ...]`).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L423-L426】
* `gender` → string (e.g., `"m"`, `"f"`).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L423-L425】
* `position` → string `"x,y"` (integers expected).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L427-L428】
* `direction` → int (used in `reLocate`).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L427-L428】

**Optional (applied if present):**
* `roles`, `speed`, `smiley`, `hand`, `avatarSize` (see section 3).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L316-L335】

**Global gates:**
* `sfs.isConnected` must be true; `disableAddChar` must be false; scene must include `SceneCharacterComponent`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L266-L351】

---

## 9) Notes (what’s **not** used in initial render)
* Items-style `clothes` payloads (`{ items: [...] }`) are consumed by `ClothModel.load` and **do not gate local avatar spawn** in the rendering path shown here.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/cloth/ClothModel.as†L25-L57】

