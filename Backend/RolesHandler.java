package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.*;

public class RolesHandler extends OsBaseHandler {
    
    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        trace("[ROLES] Request from: " + user.getName());
        InMemoryStore.UserState state = getStore().getOrCreateUser(user);
        
        SFSObject res = new SFSObject();
        res.putUtfString("roles", state.getRoles());
        reply(user, "roles", res);
        
        trace("[ROLES] ✅ Response sent");
    }
}
