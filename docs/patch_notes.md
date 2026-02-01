# Patch Notes: Local Avatar Render Reliability

## Summary of Changes
- Local user variable `clothes` is now sent as a JSON **string array** of clip keys, while inventory payloads (`init` response and `clothlist`) keep the object-array format.
- Room variables `isInteractiveRoom`, `grid`, `doors`, and `bots` are ensured **before** `joinRoom()` for init and door/teleport flows.
- Render gate auditing now parses the `clothes` user-variable JSON to verify string-array shape and logs key count + first key.

## Why These Changes
- The client’s local avatar render path expects `clothes` to be a JSON string array; object arrays prevent correct initialization.
- `roomJoin` can occur before room variables are set, causing the local scene to process without required grid/door/bot data.
- Audits needed to validate the actual `clothes` user variable shape, not just the server-side inventory list.

## Verification Checklist
1. **Init flow (local player):**
   - Expected log: `[INIT] clothes userVar keys len=<n> firstKey=<clip>`.
   - Expected audit: `[RENDER_AUDIT]` includes `clothesKeysLen=<n>` and `clothesFirstKey=<clip>`.
2. **Room variable timing (before join):**
   - Expected log before join: `[ROOM_VARS_GATE] stage=INIT room=<name> has={isInteractiveRoom:true,grid:true,doors:true,bots:true}`.
3. **Door/teleport flows:**
   - Expected logs before join: `[ROOM_VARS_GATE] stage=USEDOOR|TELEPORT|USEHOUSEDOOR|USEOBJECTDOOR ...`.
4. **Failure detection:**
   - If the `clothes` user var is not a string-array, expect `[RENDER_AUDIT_FAIL] ... clothesUserVarIssues=[...]`.
