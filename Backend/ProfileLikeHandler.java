package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class ProfileLikeHandler extends OsBaseHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        ISFSObject data = data(params);
        String avatarId = readField(data, "avatarID", "Unknown");
        int avatarLike = readInt(data, "avatarLike", 0);
        int rid = getClientRid(params);

        trace("[PROFILELIKE] avatarID=" + avatarId + " like=" + avatarLike + " user=" + user.getName());

        SFSObject res = new SFSObject();
        res.putInt("likeCount", 0);
        res.putInt("dislikeCount", 0);
        if (rid > 0) {
            res.putInt("rid", rid);
        }
        res.putUtfString("__cmd", "profilelike");
        reply(user, "profilelike", res);
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

    private int readInt(ISFSObject data, String key, int fallback) {
        if (data == null || key == null || !data.containsKey(key)) {
            return fallback;
        }
        try {
            return data.getInt(key);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
