# Backend-First Fix Plan (Local Avatar Not Visible)

> الهدف: إصلاح التوافق بين متطلبات العميل (client contract) وما يرسله السيرفر، مع الحد الأدنى من التغيير على الباك إند لتثبيت ظهور الـ MAIN player.

---

## ✅ Fix Plan (Prioritized)

### 1) **Fix `clothes` user variable shape** (Highest priority)
**Problem:** Client expects `clothes` user var as a JSON **string array** of clip keys, but backend sets a JSON **object array** from inventory items. This breaks local-avatar hydration and does not affect bots.【F:docs/client_avatar_contract.md†L46-L48】【F:Backend/InitHandler.java†L110-L189】

**Backend fix:**
- Build a `List<String>` from `state.getClothesItems()` using the `clip` fields (e.g., `A_2`, `B_1`), then serialize that string array as the **user variable** `clothes`.
- Keep the object-array payload for `clothlist` and the `init` response (`clothes: {type, items}`) so inventory logic remains intact.

**Verification (logs):**
- `[RENDER_AUDIT] stage=INIT` still shows `clothes:String`, but now a new debug line (or audit enhancement) should confirm JSON parses into **string array** with `len > 0`.

---

### 2) **Ensure room vars exist before join** (High)
**Problem:** Client expects `doors`, `bots`, `grid`, `isInteractiveRoom` at `roomJoin`, but backend only sets them in `ServerEventHandler.onUserJoinRoom()` after the join event has already fired.【F:docs/client_expected_payload_keys.json†L69-L89】【F:Backend/ServerEventHandler.java†L41-L72】

**Backend fix:**
- Populate these room vars **before** calling `joinRoom()` in:
  - `InitHandler.processInit()`
  - `UseDoorHandler`, `TeleportHandler`, `UseHouseDoorHandler`, `UseObjectDoorHandler`
- Alternatively, extend `ensureMandatoryRoomVars()` to set `doors`, `bots`, `grid`, `isInteractiveRoom` and call it before any join call.

**Verification (logs):**
- During `roomJoin`, client should already see `currentRoom.containsVariable("grid") == true`.
- Server should log a trace showing room vars were set **before** the join call.

---

### 3) **Add audit validation for JSON shape (clothes)** (Medium)
**Problem:** `RenderGateAudit` validates only types and uses `state.getClothesItems()` (object list), so it can report success even if the user-variable JSON is malformed or not a string array.【F:Backend/RenderGateAudit.java†L14-L97】

**Backend fix:**
- Parse the `clothes` user-variable JSON string.
- Log: `clothesKeysLen`, `firstClip`, and emit `[RENDER_AUDIT_FAIL]` when the JSON is **not** an array of strings.

**Verification (logs):**
- `[RENDER_AUDIT_FAIL]` should appear if `clothes` is an object array or invalid JSON.

---

## Quick Verification Checklist
1. Connect a client and watch server logs for:
   - `[RENDER_AUDIT] stage=INIT` with **string-array clothes** and `len > 0`.
   - `[RENDER_AUDIT] stage=ROOM_JOIN` with no missing keys.
2. Confirm that `grid`, `doors`, `bots`, `isInteractiveRoom` are available during the **initial** `roomJoin` handler (not only after).
3. Verify the MAIN avatar appears immediately on join while bots continue to render.
