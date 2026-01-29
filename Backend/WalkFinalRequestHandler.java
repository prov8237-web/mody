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
        
        // Get the target position that was set in WalkRequestHandler
        String target = state.getTarget();
        
        if (target == null || target.isEmpty()) {
            // Try to get from user property
            Object lastTarget = user.getProperty("lastWalkTarget");
            if (lastTarget != null) {
                target = (String) lastTarget;
            }
        }
        
        if (target == null || target.isEmpty()) {
            trace("[WALKFINAL] No target found, ignoring");
            SFSObject res = new SFSObject();
            send("walkfinalrequest", res, user);
            return;
        }
        
        // Update position to the target
        List<UserVariable> vars = new ArrayList<>();
        vars.add(new SFSUserVariable("position", target));
        vars.add(new SFSUserVariable("status", "idle"));
        getApi().setUserVariables(user, vars);
        trace("[MOVE_VARS_SET] stage=FINAL position=" + target + " status=idle");
        
        // Update state
        state.setPosition(target);
        state.setTarget(target);
        
        trace("[WALKFINAL] ✅ Position updated to " + target);

        // Send empty response
        SFSObject res = new SFSObject();
        res.putUtfString("position", target);
        res.putUtfString("status", "idle");
        send("walkfinalrequest", res, user);
        trace("[MOVE_ACK] stage=FINAL user=" + user.getName() + " position=" + target);
    }
}
