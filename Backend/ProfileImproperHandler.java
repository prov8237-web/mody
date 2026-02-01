package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class ProfileImproperHandler extends OsBaseHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        ISFSObject data = data(params);
        String avatarId = readField(data, "avatarID", "Unknown");
        String action = readField(data, "action", "unknown");
        int rid = getClientRid(params);

        trace("[PROFILEIMPROPER] avatarID=" + avatarId + " action=" + action + " user=" + user.getName());
        SFSObject res = new SFSObject();
        if (rid > 0) {
            res.putInt("rid", rid);
        }
        res.putUtfString("__cmd", "profileimproper");
        reply(user, "profileimproper", res);
    }

    private String readField(ISFSObject data, String key, String fallback) {
        if (data == null || key == null || !data.containsKey(key)) {
            return fallback;
        }
        try {
            String value = data.getUtfString(key);
            return value == null || value.trim().isEmpty() ? fallback : value;
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
