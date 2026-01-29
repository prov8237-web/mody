package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import java.util.ArrayList;
import java.util.List;

public class WalkFinalRequestHandler extends OsBaseHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        InMemoryStore.UserState state = getStore().getOrCreateUser(user);
        ISFSObject data = data(params);
        
        // Get the target position that was set in WalkRequestHandler
        String target = state.getTarget();
        String source = "target";
        if (data != null && data.containsKey("x") && data.containsKey("y")) {
            try {
                int x = data.getInt("x");
                int y = data.getInt("y");
                target = x + "," + y;
                source = "client";
            } catch (Exception e) {
                trace("[WALKFINAL] Invalid x/y in request");
            }
        }
        
        if (target == null || target.isEmpty()) {
            // Try to get from user property
            Object lastTarget = user.getProperty("lastWalkTarget");
            if (lastTarget != null) {
                target = (String) lastTarget;
                source = "property";
            }
        }
        
        if (target == null || target.isEmpty()) {
            trace("[WALKFINAL] No target found, ignoring");
            SFSObject res = new SFSObject();
            replyToRequest(user, "walkfinalrequest", res, params);
            return;
        }
        String currentPosition = resolveCurrentPosition(user, state);
        int[] currentCoords = parsePosition(currentPosition);
        int[] targetCoords = parsePosition(target);
        int direction = computeDirection(currentCoords, targetCoords, state != null ? state.getDirection() : 1);

        int clientRid = getClientRid(params);
        trace("[MOVE_FINAL] clientRid=" + clientRid + " user=" + user.getName() + " pos=" + target + " dir=" + direction + " source=" + source);

        // Update position to the target
        List<UserVariable> vars = new ArrayList<>();
        vars.add(new SFSUserVariable("position", target));
        vars.add(new SFSUserVariable("direction", direction));
        vars.add(new SFSUserVariable("status", "idle"));
        getApi().setUserVariables(user, vars);
        trace("[MOVE_VARS_SET] stage=FINAL position=" + target + " direction=" + direction + " status=idle source=" + source);
        
        // Update state
        state.setPosition(target);
        state.setTarget(target);
        state.setDirection(direction);
        
        trace("[WALKFINAL] ✅ Position updated to " + target);

        // Send response
        SFSObject res = new SFSObject();
        res.putInt("nextRequest", 100);
        res.putInt("rid", clientRid);
        replyToRequest(user, "walkfinalrequest", res, params);
        trace("[MOVE_ACK] stage=FINAL user=" + user.getName() + " position=" + target);
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
