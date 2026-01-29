# Patch Notes: Local Avatar Render Reliability

## Summary of Changes
- Removed `org.json` dependency from `RenderGateAudit` and added a minimal in-house JSON string-array parser for the `clothes` user variable audit.
- Added explicit server logs for **guest creation**, **user var set**, **join room**, and **room variable broadcast** to validate the local spawn sequence.
- Render gate auditing now relies on the local parser to verify `clothes` user-variable shape and logs key count + first key.
- Added movement logs and walk response payload updates to validate target/position updates (`walkrequest`/`walkfinalrequest`).

## Why These Changes
- Backend builds the `clothes` user variable as a JSON string array; the audit should validate that shape without requiring external JSON libraries.
- The server should emit clear trace points for the local player spawn path so failures can be isolated to **user vars** or **room var timing**.

## How to compile
1. Ensure Java 11 is in `PATH` and the SmartFoxServer 2X jars are available on the classpath.
2. Rebuild the backend extension (example):
   - `javac -cp "./lib/*" -d ./out $(find Backend -name "*.java")`

## Expected server log lines (spawn gate confirmation)
Look for these in order during a clean init + join:
1. **Guest created:**
   - `[GUEST_CREATED] user=<name> guest=true playerID=<id>`
2. **User vars set (init):**
   - `[USER_VARS_SET] stage=INIT count=<n> user=<name>`
   - `[USER_VARS_SET] stage=INIT status user=<name>`
   - `[USER_VARS_SET] stage=INIT target user=<name>`
   - `[USER_VARS_SET] stage=INIT speed user=<name>`
3. **Join room request:**
   - `[JOIN_ROOM] stage=INIT user=<name> room=<room>`
4. **Room vars ready + broadcast:**
   - `[ROOM_VARS_GATE] stage=INIT room=<room> has={isInteractiveRoom:true,grid:true,doors:true,bots:true}`
   - `[ROOM_VARS_BROADCAST] stage=INIT room=<room> vars=...`
   - `[ROOM_VARS_BROADCAST] stage=USER_JOIN_ROOM room=<room> vars=doors,bots,grid,isInteractiveRoom,isGameStarted,isGameEnded`
5. **Render gate audit:**
   - `[RENDER_AUDIT] stage=INIT ... clothesKeysLen=<n> clothesFirstKey=<clip>`
   - `[RENDER_AUDIT] stage=ROOM_JOIN ... clothesKeysLen=<n> clothesFirstKey=<clip>`

## Expected server log lines (movement sequence)
1. **Walk request acknowledged:**
   - `[MOVE_VARS_SET] stage=REQUEST from=<x,y> target=<x,y> dist=<d>`
   - `[MOVE_VARS_META] stage=REQUEST speedSet=<true|false> delay=<ms>`
   - `[MOVE_ACK] stage=REQUEST delay=<ms>`
2. **Walk final update:**
   - `[MOVE_VARS_SET] stage=FINAL position=<x,y> status=idle source=<client|target|property>`
   - `[MOVE_ACK] stage=FINAL user=<name> position=<x,y>`

## Quick smoke test checklist
1. Start server, connect a client as a guest.
2. Verify the log sequence above appears in order.
3. Confirm `clothesKeysLen` is non-zero and the first key looks like `<clip>_<color>`.
4. Ensure room variables are set **before** the client’s join handler (no missing `grid/doors/bots` errors).
5. Click a nearby tile (1-2 tiles away) and confirm delay >= minDelay.
6. Click a far tile (e.g., 40,13) and confirm delay is noticeably higher than near clicks.
7. Confirm the local avatar appears immediately on room entry while bots remain visible.
