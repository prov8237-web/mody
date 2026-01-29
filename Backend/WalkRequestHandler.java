package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WalkRequestHandler extends OsBaseHandler {
    private static final int MIN_DELAY_MS = 300;
    private static final int MAX_DELAY_MS = 2000;
    private static final int PER_TILE_MS = 110;
    private static final ConcurrentHashMap<Integer, MoveTrace> MOVE_TRACES = new ConcurrentHashMap<>();

    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        ISFSObject data = data(params);
        InMemoryStore.UserState state = getStore().getOrCreateUser(user);
        logMoveRequest(user, params, data, state);

        if (data == null || !data.containsKey("x") || !data.containsKey("y")) {
            trace("[WALKREQUEST] Missing coordinates");
            if (ProtocolConfig.strictProtocol()) {
                throw new IllegalStateException("Strict protocol: walkrequest missing x/y");
            }
            SFSObject error = new SFSObject();
            error.putUtfString("errorCode", "MISSING_COORDS");
            error.putUtfString("message", "Missing x/y");
            send("walkrequest", error, user);
            return;
        }

        int x = data.getInt("x");
        int y = data.getInt("y");
        String target = x + "," + y;
        String currentPosition = resolveCurrentPosition(user, state);
        int[] currentCoords = parsePosition(currentPosition);
        int[] targetCoords = parsePosition(target);
        int distance = Math.abs(targetCoords[0] - currentCoords[0]) + Math.abs(targetCoords[1] - currentCoords[1]);
        int delayMs = clampDelay(distance * PER_TILE_MS);
        int direction = computeDirection(currentCoords, targetCoords, state != null ? state.getDirection() : 1);

        List<UserVariable> vars = new ArrayList<>();
        vars.add(new SFSUserVariable("target", target));
        vars.add(new SFSUserVariable("direction", direction));
        boolean speedSet = false;
        if (data.containsKey("speed")) {
            try {
                vars.add(new SFSUserVariable("speed", data.getDouble("speed")));
                speedSet = true;
            } catch (Exception e) {
                trace("[WALKREQUEST] Invalid speed value");
            }
        }
        getApi().setUserVariables(user, vars);
        int clientRid = getClientRid(params);
        trace("[MOVE_REQ] clientRid=" + clientRid + " user=" + user.getName() + " from=" + currentPosition + " to=" + target
            + " dist=" + distance + " delay=" + delayMs + " dir=" + direction);
        trace("[MOVE_VARS_META] stage=REQUEST speedSet=" + speedSet + " delay=" + delayMs);
        user.setProperty("lastWalkTarget", target);
        state.setTarget(target);
        state.setDirection(direction);

        SFSObject res = new SFSObject();
        res.putInt("delay", delayMs);
        res.putInt("rid", clientRid);
        replyToRequest(user, "walkrequest", res, params);

        trace("[MOVE_ACK] stage=REQUEST delay=" + delayMs);
        trace("[WALKREQUEST] ✅ Target set to " + target);
    }

    private void logMoveRequest(User user, ISFSObject params, ISFSObject data, InMemoryStore.UserState state) {
        try {
            long ts = System.currentTimeMillis();
            int userId = user != null ? user.getId() : -1;
            String userName = user != null ? user.getName() : "null";
            String roomName = user != null && user.getLastJoinedRoom() != null ? user.getLastJoinedRoom().getName() : "null";
            int roomId = user != null && user.getLastJoinedRoom() != null ? user.getLastJoinedRoom().getId() : -1;
            int clientRid = getClientRid(params);
            String avatarId = readUserVarAsString(user, "avatarID", "avatarId");
            String playerId = readUserVarAsString(user, "playerID", "playerId");
            String target = readString(data, "target");
            if (target == null || "missing".equals(target)) {
                String x = readString(data, "x");
                String y = readString(data, "y");
                if (!"missing".equals(x) && !"missing".equals(y)) {
                    target = x + "," + y;
                }
            }
            String targetType = valueType(data, "target", target);
            String direction = readUserVarAsString(user, "direction");
            String status = readUserVarAsString(user, "status");
            String snapshot = buildVarSnapshot(user, data);
            trace("[MOVE_REQ] ts=" + ts + " room=" + roomId + "/" + roomName + " uid=" + userId + " uname=" + userName
                + " rid=" + clientRid + " avatarId=" + avatarId + " playerId=" + playerId
                + " target=" + target + " targetType=" + targetType + " direction=" + direction + " status=" + status
                + " vars=" + snapshot);
            if (clientRid == -1) {
                trace("[WARN] rid=-1 may indicate client not correlating responses; check protocol.");
            }
            MoveTrace traceEntry = new MoveTrace(clientRid, target, ts);
            MOVE_TRACES.put(userId, traceEntry);
        } catch (Exception e) {
            trace("[MOVE_REQ] log failed: " + e.getMessage());
        }
    }

    static MoveTrace getMoveTrace(int userId) {
        return MOVE_TRACES.get(userId);
    }

    static void clearMoveTrace(int userId) {
        MOVE_TRACES.remove(userId);
    }

    static final class MoveTrace {
        private final int rid;
        private final String target;
        private final long ts;

        MoveTrace(int rid, String target, long ts) {
            this.rid = rid;
            this.target = target;
            this.ts = ts;
        }
    }

    private String resolveCurrentPosition(User user, InMemoryStore.UserState state) {
        if (user != null && user.getVariable("position") != null) {
            Object value = user.getVariable("position").getValue();
            if (value instanceof String && !((String) value).trim().isEmpty()) {
                return (String) value;
            }
        }
        String fallback = state != null ? state.getPosition() : null;
        return fallback == null || fallback.trim().isEmpty() ? "0,0" : fallback;
    }

    private int[] parsePosition(String value) {
        int[] coords = new int[] {0, 0};
        if (value == null) {
            return coords;
        }
        String[] parts = value.split(",");
        if (parts.length != 2) {
            return coords;
        }
        try {
            coords[0] = Integer.parseInt(parts[0].trim());
            coords[1] = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return coords;
        }
        return coords;
    }

    private int clampDelay(int delay) {
        if (delay < MIN_DELAY_MS) {
            return MIN_DELAY_MS;
        }
        if (delay > MAX_DELAY_MS) {
            return MAX_DELAY_MS;
        }
        return delay;
    }

    private int computeDirection(int[] from, int[] to, int fallback) {
        int dx = to[0] - from[0];
        int dy = to[1] - from[1];
        if (dx == 0 && dy == 0) {
            return normalizeDirection(fallback);
        }
        if (dx == 0 && dy < 0) {
            return 1;
        }
        if (dx > 0 && dy < 0) {
            return 2;
        }
        if (dx > 0 && dy == 0) {
            return 3;
        }
        if (dx > 0 && dy > 0) {
            return 4;
        }
        if (dx == 0 && dy > 0) {
            return 5;
        }
        if (dx < 0 && dy > 0) {
            return 6;
        }
        if (dx < 0 && dy == 0) {
            return 7;
        }
        return 8;
    }

    private int normalizeDirection(int direction) {
        if (direction < 1 || direction > 8) {
            return 1;
        }
        return direction;
    }
}
