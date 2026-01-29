package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public abstract class OsBaseHandler extends BaseClientRequestHandler {
    
    protected void reply(User user, String cmd, ISFSObject res) {
        if (res == null) {
            res = new SFSObject();
        }

        ProtocolValidator.validateResponse(cmd, res, this);
        
        trace("📤 SENDING: " + cmd + " to " + user.getName());
        send(cmd, res, user);
        
        try {
            MainExtension mainExt = (MainExtension) getParentExtension();
            if (mainExt != null) {
                mainExt.markResponseSent(cmd, user);
            }
        } catch (Exception e) {
            // Silent
        }
    }

    protected void sendValidated(User user, String cmd, ISFSObject res) {
        if (res == null) {
            res = new SFSObject();
        }
        ProtocolValidator.validateResponse(cmd, res, this);
        send(cmd, res, user);
    }

    protected ISFSObject data(ISFSObject params) {
        return HandlerUtils.dataOrSelf(params);
    }

    protected InMemoryStore getStore() {
        MainExtension ext = (MainExtension) getParentExtension();
        return ext != null ? ext.getStore() : new InMemoryStore();
    }
}
