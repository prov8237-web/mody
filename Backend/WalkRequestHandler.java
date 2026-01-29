package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import java.util.ArrayList;
import java.util.List;

public class WalkRequestHandler extends OsBaseHandler {
    private static final int DEFAULT_DELAY_MS = 200;

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

        List<UserVariable> vars = new ArrayList<>();
        vars.add(new SFSUserVariable("target", target));
        if (data.containsKey("speed")) {
            try {
                vars.add(new SFSUserVariable("speed", data.getDouble("speed")));
            } catch (Exception e) {
                trace("[WALKREQUEST] Invalid speed value");
            }
        }
        getApi().setUserVariables(user, vars);
        trace("[MOVE_VARS_SET] stage=REQUEST target=" + target);
        user.setProperty("lastWalkTarget", target);
        state.setTarget(target);

        SFSObject res = new SFSObject();
        res.putInt("delay", DEFAULT_DELAY_MS);
        res.putUtfString("target", target);
        reply(user, "walkrequest", res);

        trace("[MOVE_ACK] stage=REQUEST delay=" + DEFAULT_DELAY_MS);
        trace("[WALKREQUEST] ✅ Target set to " + target);
    }
}
