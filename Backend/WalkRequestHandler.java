package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import java.util.ArrayList;
import java.util.List;

public class WalkRequestHandler extends OsBaseHandler {
    private static final int MIN_DELAY_MS = 250;
    private static final int MAX_DELAY_MS = 2000;
    private static final int PER_TILE_MS = 120;

    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        ISFSObject data = data(params);
        InMemoryStore.UserState state = getStore().getOrCreateUser(user);

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

        List<UserVariable> vars = new ArrayList<>();
        vars.add(new SFSUserVariable("target", target));
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
        trace("[MOVE_VARS_SET] stage=REQUEST from=" + currentPosition + " target=" + target + " dist=" + distance);
        trace("[MOVE_VARS_META] stage=REQUEST speedSet=" + speedSet + " delay=" + delayMs);
        user.setProperty("lastWalkTarget", target);
        state.setTarget(target);

        SFSObject res = new SFSObject();
        res.putInt("delay", delayMs);
        res.putUtfString("target", target);
        reply(user, "walkrequest", res);

        trace("[MOVE_ACK] stage=REQUEST delay=" + delayMs);
        trace("[WALKREQUEST] ✅ Target set to " + target);
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
}
