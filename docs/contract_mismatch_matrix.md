# Contract Mismatch Matrix (Client vs Backend)

| Required key | Client expectation | Backend source (current) | Type (backend) | Timing (backend) | Mismatch / risk |
| --- | --- | --- | --- | --- | --- |
| `clothes` (user var) | JSON string array of clip keys | `InitHandler.processInit()` sets `clothes` from `buildClothesKeysArray()`; `SaveBaseClothesHandler` updates `clothes` on save | String (JSON array) | Set before `joinRoom()` in init; on savebaseclothes during customization | **Match** (now string-array). Risk only if other flows overwrite with object-array.
| `gender` (user var) | String | `InitHandler.processInit()` and `SaveBaseClothesHandler` | String | Set before `joinRoom()` in init | Match.
| `position` (user var) | String `"x,y"` | `InitHandler.processInit()` sets `position` `"20,30"`; `WalkFinalRequestHandler` updates | String | Set before `joinRoom()` in init | Match; must remain valid `x,y` format.
| `direction` (user var) | Int | `InitHandler.processInit()` sets `direction` to `0` | Integer | Set before `joinRoom()` in init | Match.
| `isInteractiveRoom` (room var) | Boolean true | `ensureMandatoryRoomVars(...)` and `ServerEventHandler.onUserJoinRoom()` | Boolean | Before join in init/door flows (via ensure); also set after join in server event | **Timing risk** if a join happens without `ensureMandatoryRoomVars(...)`.
| `doors` (room var) | JSON string | `ensureMandatoryRoomVars(...)` and `ServerEventHandler.onUserJoinRoom()` | String | Before join in init/door flows; after join in server event | **Timing risk** if missing before client join handler.
| `bots` (room var) | JSON string | `ensureMandatoryRoomVars(...)` and `ServerEventHandler.onUserJoinRoom()` | String | Before join in init/door flows; after join in server event | **Timing risk** if missing before client join handler.
| `grid` (room var) | Base64 string | `ensureMandatoryRoomVars(...)` and `ServerEventHandler.onUserJoinRoom()` | String | Before join in init/door flows; after join in server event | **Timing risk** if missing before client join handler.
| `roles` (user var) | String | `InitHandler.processInit()` | String | Set before `joinRoom()` in init | Match (optional).
| `speed` (user var) | Number | `InitHandler.processInit()`; `WalkRequestHandler` does not change | Double | Set before `joinRoom()` in init | Match (optional).
| `avatarSize` (user var) | Number | `InitHandler.processInit()` | Double | Set before `joinRoom()` in init | Match (optional).
