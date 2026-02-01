package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class KickAvatarFromRoomHandler extends OsBaseHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        ISFSObject data = data(params);
        String avatarId = readField(data, "avatarID", "Unknown");
        double duration = readDouble(data, "duration", 0.0);
        int rid = getClientRid(params);

        trace("[KICK_AVATAR] avatarID=" + avatarId + " duration=" + duration + " user=" + user.getName());
        SFSObject res = new SFSObject();
        if (rid > 0) {
            res.putInt("rid", rid);
        }
        res.putUtfString("__cmd", "kickavatarfromroom");
        reply(user, "kickavatarfromroom", res);
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

    private double readDouble(ISFSObject data, String key, double fallback) {
        if (data == null || key == null || !data.containsKey(key)) {
            return fallback;
        }
        try {
            return data.getDouble(key);
        } catch (Exception ignored) {
            try {
                return data.getInt(key);
            } catch (Exception ignored2) {
                return fallback;
            }
        }
    }
}
