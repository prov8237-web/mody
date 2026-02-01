package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class ProfileSkinListHandler extends OsBaseHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        ISFSObject data = data(params);
        int page = readInt(data, "page", 1);
        String search = readField(data, "search", "");
        String sort = readField(data, "sort", "created_desc");

        trace("[PROFILESKINLIST] page=" + page + " search=" + search + " sort=" + sort + " user=" + user.getName());

        SFSObject res = new SFSObject();
        SFSObject items = new SFSObject();
        items.putSFSArray("list", HandlerUtils.safeArray(null));
        res.putSFSObject("items", items);
        res.putInt("pageSelected", page);
        reply(user, "profileskinlist", res);
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
