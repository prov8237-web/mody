package src5;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class ClothListHandler extends OsBaseHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        String ip = user.getSession().getAddress();

        trace("[CLOTHLIST] Request from: " + user.getName() + " IP: " + ip);

        InMemoryStore.UserState state = getStore().getOrCreateUser(user);
        ISFSArray items = state.getClothesItems();
        if (items == null || items.size() == 0) {
            Object savedClothes = getParentExtension().getParentZone().getProperty(ip + "_clothes");
            if (savedClothes instanceof ISFSObject) {
                ISFSObject clothesObj = (ISFSObject) savedClothes;
                if (clothesObj.containsKey("items")) {
                    items = clothesObj.getSFSArray("items");
                    state.setClothesItems(items);
                }
            }
        }
        if (items == null || items.size() == 0) {
            items = DefaultClothesBuilder.buildDefaultItems(state.getGender());
            state.setClothesItems(items);
            trace("[CLOTHLIST] Rebuilt clothes payload with defaults. Count=" + items.size());
        }

        SFSObject res = new SFSObject();
        res.putUtfString("type", "CLOTH");  // ✅ مهم جداً! الكلاينت يحتاج هذا الحقل
        res.putSFSArray("items", items);
        res.putInt("nextRequest", 1000);
        reply(user, "clothlist", res);

        trace("[CLOTHLIST] ✅ Sent " + items.size() + " items with type=CLOTH");
    }
}
