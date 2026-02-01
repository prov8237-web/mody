package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import java.util.Set;

public class ProfileHandler extends OsBaseHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        ISFSObject data = data(params);
        String avatarId = resolveAvatarId(data);
        String avatarName = resolveAvatarName(user, avatarId);

        int rid = extractRid(params);
        trace("### PROFILE_HANDLER_V2 HIT ### handler=ProfileHandler rid=" + rid + " user=" + user.getName());
        trace("[PROFILE] Request avatarID=" + avatarId + " user=" + user.getName());

        SFSObject res = new SFSObject();
        res.putUtfString("avatarName", avatarName);
        res.putDouble("avarageRating", 0);
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
        res.putInt("nextRequest", 0);
        res.putInt("duration", 0);
        SFSObject skin = new SFSObject();
        skin.putUtfString("clip", "");
        SFSObject skinProperty = new SFSObject();
        skinProperty.putUtfString("bgColor", "FEFFF2");
        skinProperty.putUtfString("alpha", "1");
        skinProperty.putUtfString("cn", "ProfileSkinProperty");
        skinProperty.putUtfString("textColor", "000000");
        skin.putSFSObject("property", skinProperty);
        skin.putUtfString("roles", "");
        res.putSFSObject("skin", skin);
        res.putUtfString("runWinTeam", "");

        trace("[PROFILE] Response keys=" + keyCount(res) + " avatarID=" + avatarId);
        trace("[PROFILE] PayloadTypes=" + payloadTypes(res));
        trace("RID_CHECK cmd=profile reqRid=" + rid + " resRid=" + rid + " avatarID=" + avatarId);
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

    private String resolveAvatarName(User user, String avatarId) {
        if (user != null) {
            UserVariable avatarName = user.getVariable("avatarName");
            if (avatarName != null && avatarName.getStringValue() != null && !avatarName.getStringValue().trim().isEmpty()) {
                return avatarName.getStringValue();
            }
            if (user.getName() != null && !user.getName().trim().isEmpty()) {
                if (avatarId != null && user.getName().equals(avatarId)) {
                    return "Guest";
                }
                return user.getName();
            }
        }
        return "Guest";
    }

    private String payloadTypes(SFSObject res) {
        if (res == null) {
            return "{}";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        Set<String> keys = res.getKeys();
        int index = 0;
        for (String key : keys) {
            Object value = res.get(key);
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(key).append(":");
            builder.append(value == null ? "null" : value.getClass().getSimpleName());
            index++;
        }
        builder.append("}");
        return builder.toString();
    }
}
