package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class ProfileHandler extends OsBaseHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        ISFSObject data = data(params);
        String avatarId = resolveAvatarId(data);

        trace("[PROFILE] Request avatarID=" + avatarId + " user=" + user.getName());

        SFSObject res = new SFSObject();
        res.putUtfString("avatarName", avatarId);
        res.putUtfString("avarageRating", "0");
        res.putInt("totalBuddies", 0);
        res.putBool("isBuddy", false);
        res.putBool("isRequest", false);
        res.putInt("banCount", 0);
        res.putSFSArray("cards", new SFSArray());
        res.putSFSArray("stickers", new SFSArray());
        res.putSFSArray("badges", new SFSArray());
        res.putSFSArray("flats", new SFSArray());
        res.putInt("mood", 0);
        res.putInt("likeCount", 0);
        res.putInt("dislikeCount", 0);
        res.putUtfString("status", "");
        res.putUtfString("avatarCity", "");
        res.putUtfString("avatarAge", "");
        res.putInt("emailRegistered", 0);
        res.putSFSObject("skin", new SFSObject());
        res.putUtfString("runWinTeam", "");

        trace("[PROFILE] Response keys=" + res.getKeys().length + " avatarID=" + avatarId);
        reply(user, "profile", res);
    }

    private String resolveAvatarId(ISFSObject data) {
        if (data != null && data.containsKey("avatarID")) {
            try {
                String value = data.getUtfString("avatarID");
                if (value != null && !value.trim().isEmpty()) {
                    return value;
                }
            } catch (Exception ignored) {
            }
        }
        return "Unknown";
    }
}
