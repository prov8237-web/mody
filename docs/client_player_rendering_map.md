# Client Player Rendering Map (ActionScript)

**Top 10 files to read first (high signal):**
1. `Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as`
2. `Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as`
3. `Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as`
4. `Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneCharacterComponent.as`
5. `Client/snl.779.swf/scripts/com/oyunstudyosu/engine/IsoScene.as`
6. `Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomModel.as`
7. `Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarModel.as`
8. `Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarController.as`
9. `Client/snl.779.swf/scripts/com/oyunstudyosu/service/ServiceModel.as`
10. `Client/Rooms/st01.1/scripts/com/oyunstudyosu/room/RoomLayer.as`

## 1) Player Rendering Pipeline (High-level)

### Scene load ➜ asset loading ➜ map processing
* `SceneProcessDataComponent.load()` builds room/furniture SWF asset requests, loads room SWF, then calls `processSceneData()` once assets are ready. It pulls the map from `RoomModel`, runs grid processing, and initializes map properties (e.g., sound, YouTube screen).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L37-L220】

### Local player avatar spawn (the **gate condition**)
* **Gate:** In `SceneProcessDataComponent.processSceneData()`, the local player Character is created only if the `SceneCharacterComponent` exists **and** `RoomModel.disableAddChar` is `false`:
  * `if (_loc1_ != null && !Sanalika.instance.roomModel.disableAddChar) { ... }`【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L306-L351】
  * If the gate fails, it logs `Character creation disabled for this room`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L350-L352】
* The actual character instance is created here (`new Character()`), flagged `isMe = true`, and initialized with avatar model values + user variables (roles, speed, smiley, hand, avatarSize). It uses `avatarModel.position` + `direction` to place the player via `reLocate`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L312-L349】

### Remote avatar spawn (other users)
* `RoomController.onUserEnterRoom()` queues new users (unless local player, non-interactive room, or scene missing).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L486-L509】
* `RoomController.addChar()` pulls **user variables** from the entering `SFSUser` to build a `Character`:
  * Required: `position`, `gender`, `clothes` (JSON), `speed`, `avatarName`, `direction` (optional).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L558-L638】
  * Optional: `status`, `smiley`, `avatarSize`, `hand`, `target` (auto-walk), `platform` (mobile), `roles`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L610-L666】

### Character render/update behavior
* `Character.init(id, scene, type, sex, clothes)` is the primary setup method; it creates the display tree, fills the container, adds to scene, and triggers an initial update. Clothes are set via `setAccesory` and are used to construct visual body parts.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as†L295-L314】
* Positioning and update pipeline:
  * `reLocate(x, y, direction)` sets the character’s scene position and grid depth ordering, and updates status/masks based on cell properties.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as†L1817-L1866】
  * `walk(x, y, ...)` drives movement and optionally updates user vars (direction) when local player moves.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as†L1215-L1262】
  * `setAction`, `setSmiley`, `useHandItem` apply animation/status changes and item overlays.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as†L1990-L2034】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as†L2299-L2304】

### Scene character registry
* `SceneCharacterComponent` stores all characters by id, exposes `addChar`, `removeWithAvatarId`, and tracks `myChar` for local player identity.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneCharacterComponent.as†L10-L104】

---

## 2) Player Rendering Map (Classes, Methods, Events, Data)

### Important classes (core path)
| Area | Class | Role |
| --- | --- | --- |
| Engine / scene | `SceneProcessDataComponent` | Loads assets, processes room data, and spawns local avatar if allowed.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L37-L355】 |
| Engine / scene | `SceneCharacterComponent` | Registry of characters in the scene, includes `myChar` reference for local player.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneCharacterComponent.as†L10-L104】 |
| Engine / character | `Character` | Core avatar entity and renderer (create/init, walk, reLocate, status changes).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as†L295-L314】 |
| Room | `RoomController` | Spawns/removes remote avatars, listens to SmartFox events and room extensions.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L36-L116】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L486-L690】 |
| Room | `RoomModel` | Holds room state (incl. `disableAddChar` gate).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomModel.as†L23-L68】 |
| Avatar | `AvatarModel` | Local avatar state (position, direction, clothesOn, size).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarModel.as†L45-L120】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarModel.as†L663-L870】 |
| Avatar | `AvatarController` | Responds to user variable updates for avatars (position, direction, clothes, status, etc.).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarController.as†L33-L83】 |
| Service | `ServiceModel` | Hooks SmartFox events for user/room variable updates and extension responses.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/service/ServiceModel.as†L1790-L1836】 |
| Room SWF | `RoomLayer` (st01.1) | Base MovieClip layer for room SWF assets (stub).【F:Client/Rooms/st01.1/scripts/com/oyunstudyosu/room/RoomLayer.as†L1-L22】 |

### Key methods (signatures)
* `SceneProcessDataComponent.load()` → room SWF + furniture asset requests, map processing.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L60-L174】
* `SceneProcessDataComponent.processSceneData()` → map entries, doors, bots, local avatar creation (gate).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L244-L355】
* `RoomController.addChar(user:SFSUser, isTarget:Boolean = false)` → remote avatar construction from user variables.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L536-L692】
* `Character.init(id:String, scene:IScene, type:int, sex:String, clothes:Array)`【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as†L295-L314】
* `Character.reLocate(x:int, y:int, dir:int, applyStatus:Boolean = true)`【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as†L1817-L1875】
* `Character.walk(x:int, y:int, door:MovieClip = null, ..., ...)`【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as†L1215-L1262】
* `Character.setAction(action:String)` / `setSmiley(key:String)` / `useHandItem(key:String, apply:Boolean = true)`【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as†L1990-L2034】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as†L2299-L2304】

### Events listened to (SFS + custom)
**SmartFox (room/engine):**
* `roomJoin`, `roomJoinError`, `userEnterRoom`, `userExitRoom` in `RoomController`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L49-L58】
* `userVariablesUpdate`, `roomVariablesUpdate`, `extensionResponse` in `ServiceModel.activate()`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/service/ServiceModel.as†L1806-L1836】

**User-variable-driven avatar updates (AvatarController):**
* `smiley`, `clothes`, `avatarSize`, `avatarName`, `gender`, `hand`, `direction`, `position`, `speed`, `roles`, `status`, `poolVars`, `target`, `universeKey`, `fbPermissions`, `tp` (typing).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarController.as†L33-L60】

**Room variable listeners (RoomController):**
* `doors`, `bots` room variables, plus extension handlers for room updates and interactive object state. These influence room scene layout and entry processing.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L60-L75】

### Expected data keys (best guess)
This is a **best-effort** list based on `RoomController.addChar()` and local spawn in `SceneProcessDataComponent`:

**User variables (per avatar):**
* `position` ("x,y" string, required).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L584-L638】
* `direction` (int, optional; default 1).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L617-L623】
* `gender` (string, required).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L590-L603】
* `clothes` (JSON string → array, required).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L600-L606】
* `avatarName` (string).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L615-L616】
* `speed` (number).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L614-L616】
* `roles` (string).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L608-L612】
* `platform` (string; e.g., "mobile").【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L607-L631】
* `status` (string, e.g., sit/action).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L640-L642】
* `smiley` (string).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L643-L646】
* `avatarSize` (number).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L647-L650】
* `hand` (string).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L651-L654】
* `target` ("x,y" string; auto-walk).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L663-L670】
* `poolVars` (string; used to remove avatars from pools on exit).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L517-L533】

**Room variables (scene-level):**
* `isInteractiveRoom` (boolean).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L494-L500】
* `doors` (JSON string).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L70-L75】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L140-L156】
* `bots` (JSON string).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L70-L75】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomController.as†L130-L138】
* `extensions` (JSON array of room extensions).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L206-L220】

**Local avatar model keys (client-side):**
* `position` and `direction` are stored in `AvatarModel` and read when spawning local character.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarModel.as†L845-L870】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L338-L347】
* `clothesOn` and `avatarSize` are used for initialization and avatar size overrides.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/avatar/AvatarModel.as†L663-L674】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L327-L336】

---

## 3) Notes on rendering/display list
* `Character.init()` calls `fill(container)` and `add2Scene()`; these create display children and insert into the scene’s display list. The scene also adds speech balloon containers to its `speechContainer`.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/character/Character.as†L295-L314】
* `SceneCharacterComponent` is the central registry and is used by other systems (chat, pool, mouse interactions) to find characters by id.【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneCharacterComponent.as†L10-L104】

---

## 4) Gate/condition for LOCAL avatar spawn (exact condition)
```
if (_loc1_ != null && !Sanalika.instance.roomModel.disableAddChar)
```
* `_loc1_` is `SceneCharacterComponent` and the flag is stored on `RoomModel` (`disableAddChar:Boolean = false`).【F:Client/snl.779.swf/scripts/com/oyunstudyosu/engine/scene/components/SceneProcessDataComponent.as†L306-L351】【F:Client/snl.779.swf/scripts/com/oyunstudyosu/room/RoomModel.as†L23-L28】

