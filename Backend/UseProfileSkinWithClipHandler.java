package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class UseProfileSkinWithClipHandler extends OsBaseHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        ISFSObject data = data(params);
        String clip = readField(data, "clip", "0");
        int rid = extractRid(params);

        trace("[USE_PROFILE_SKIN] clip=" + clip + " user=" + user.getName());
        SFSObject res = new SFSObject();
        trace("RID_CHECK cmd=useprofileskinwithclip reqRid=" + rid + " resRid=" + rid + " avatarID=unknown");
        sendResponseWithRid("useprofileskinwithclip", res, user, rid);
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
