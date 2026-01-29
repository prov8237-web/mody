package src5;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStore {
    private final Map<Integer, UserState> usersById = new ConcurrentHashMap<>();
    private final Map<String, UserState> usersByName = new ConcurrentHashMap<>();
    private final Map<String, RoomState> roomsByName = new ConcurrentHashMap<>();
    private final Set<String> reservedNames = ConcurrentHashMap.newKeySet();

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public UserState getOrCreateUser(User user) {
        return usersById.computeIfAbsent(user.getId(), id -> {
            UserState state = new UserState(id, user.getName());
            usersByName.put(state.getUserName(), state);
            return state;
        });
    }

    public UserState findUserByName(String name) {
        return usersByName.get(name);
    }

    public RoomState getOrCreateRoom(Room room) {
        return roomsByName.computeIfAbsent(room.getName(), RoomState::new);
    }

    public String ensureUniqueDisplayName(String baseName, int userId) {
        String normalized = isBlank(baseName) ? "Guest" : baseName.trim();
        String candidate = normalized;
        int counter = 1;
        while (reservedNames.contains(candidate)) {
            candidate = normalized + "#" + userId + "_" + counter;
            counter++;
        }
        reservedNames.add(candidate);
        return candidate;
    }

    public void releaseDisplayName(String name) {
        if (name != null) {
            reservedNames.remove(name);
        }
    }

    public static final class UserState {
        private final int userId;
        private final String userName;
        private String avatarName;
        private boolean guest = true;
        private String gender = "m";
        private String clothesJson = "[]";
        private ISFSArray clothesItems = new SFSArray();
        private String position = "275,614";
        private int direction = 0;
        private String roles = "PiA=";
        private double avatarSize = 1.0;
        private String smiley = "";
        private String hand = "";
        private double speed = 1.0;
        private String target = "";
        private String status = "idle";
        private String universeKey = "w8";
        private String fbPermissions = "";
        private String currentRoom = "";
        private int mood = 0;
        private String statusMessage = "";
        private String chatBalloon = "1";
        private String optimizedAssetKey = "";
        private int typing = 0;
        private ISFSArray wallet = new SFSArray();
        private ISFSArray inventory = new SFSArray();
        private ISFSArray buddies = new SFSArray();
        private ISFSArray buddyRequests = new SFSArray();
        private ISFSArray quests = new SFSArray();
        private ISFSArray orders = new SFSArray();

        public UserState(int userId, String userName) {
            this.userId = userId;
            this.userName = userName == null ? "" : userName;
            this.avatarName = isBlank(this.userName) ? "Guest" + userId : this.userName;
            this.quests = buildDefaultQuests();
        }

        public int getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public String getAvatarName() {
            return avatarName;
        }

        public void setAvatarName(String avatarName) {
            this.avatarName = avatarName;
        }

        public boolean isGuest() {
            return guest;
        }

        public void setGuest(boolean guest) {
            this.guest = guest;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getClothesJson() {
            return clothesJson;
        }

        public void setClothesJson(String clothesJson) {
            this.clothesJson = clothesJson;
        }

        public ISFSArray getClothesItems() {
            return clothesItems;
        }

        public void setClothesItems(ISFSArray items) {
            if (items == null) {
                clothesItems = new SFSArray();
                clothesJson = "[]";
                return;
            }
            clothesItems = new SFSArray();
            for (int i = 0; i < items.size(); i++) {
                clothesItems.addSFSObject(items.getSFSObject(i));
            }
            clothesJson = items.toJson();
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public int getDirection() {
            return direction;
        }

        public void setDirection(int direction) {
            this.direction = direction;
        }

        public String getRoles() {
            return roles;
        }

        public void setRoles(String roles) {
            this.roles = roles;
        }

        public String getOptimizedAssetKey() {
            return optimizedAssetKey;
        }

        public void setOptimizedAssetKey(String optimizedAssetKey) {
            this.optimizedAssetKey = optimizedAssetKey;
        }

        public double getAvatarSize() {
            return avatarSize;
        }

        public void setAvatarSize(double avatarSize) {
            this.avatarSize = avatarSize;
        }

        public String getSmiley() {
            return smiley;
        }

        public void setSmiley(String smiley) {
            this.smiley = smiley;
        }

        public String getHand() {
            return hand;
        }

        public void setHand(String hand) {
            this.hand = hand;
        }

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getUniverseKey() {
            return universeKey;
        }

        public void setUniverseKey(String universeKey) {
            this.universeKey = universeKey;
        }

        public String getFbPermissions() {
            return fbPermissions;
        }

        public void setFbPermissions(String fbPermissions) {
            this.fbPermissions = fbPermissions;
        }

        public String getCurrentRoom() {
            return currentRoom;
        }

        public void setCurrentRoom(String currentRoom) {
            this.currentRoom = currentRoom == null ? "" : currentRoom;
        }

        public int getMood() {
            return mood;
        }

        public void setMood(int mood) {
            this.mood = mood;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage == null ? "" : statusMessage;
        }

        public String getChatBalloon() {
            return chatBalloon;
        }

        public void setChatBalloon(String chatBalloon) {
            this.chatBalloon = chatBalloon == null ? "1" : chatBalloon;
        }

        public int getTyping() {
            return typing;
        }

        public void setTyping(int typing) {
            this.typing = typing;
        }

        public ISFSArray getWallet() {
            return wallet;
        }

        public void setWallet(ISFSArray walletArray) {
            wallet = new SFSArray();
            if (walletArray != null) {
                for (int i = 0; i < walletArray.size(); i++) {
                    wallet.addSFSObject(walletArray.getSFSObject(i));
                }
            }
        }

        public ISFSArray getInventory() {
            return inventory;
        }

        public ISFSArray getBuddies() {
            return buddies;
        }

        public ISFSArray getBuddyRequests() {
            return buddyRequests;
        }

        public ISFSArray getQuests() {
            return quests;
        }

        public ISFSArray getOrders() {
            return orders;
        }

        private static ISFSArray buildDefaultQuests() {
            SFSArray quests = new SFSArray();
            quests.addSFSObject(buildPeriodicQuest(-154128, "cosmicStone", 4));
            quests.addSFSObject(buildPeriodicQuest(-154129, "sapling_task", 10));
            quests.addSFSObject(buildPeriodicQuest(-154130, "snowlyFlower", 5));
            quests.addSFSObject(buildPeriodicQuest(-154131, "snowman", 7));
            quests.addSFSObject(buildPeriodicQuest(-154132, "paperDelivery", 7));
            quests.addSFSObject(buildPeriodicQuest(-154133, "GuitarMakingTask", 7));
            quests.addSFSObject(buildPeriodicQuest(-154134, "trashQuest", 6));
            return quests;
        }

        private static SFSObject buildPeriodicQuest(int id, String metaKey, int totalStep) {
            SFSObject quest = new SFSObject();
            quest.putInt("totalStep", totalStep);
            quest.putUtfString("metaKey", metaKey);
            quest.putUtfString("type", "PERIODIC");
            quest.putInt("rewardMultiplier", 1);
            quest.putInt("currentStep", 1);
            quest.putInt("id", id);
            quest.putUtfString("status", "NEW");
            return quest;
        }

        public void setBuddies(ISFSArray buddies) {
            this.buddies = buddies == null ? new SFSArray() : buddies;
        }
    }

    public static final class RoomState {
        private final String roomName;
        private String doorsJson = "[]";
        private String botsJson = "[]";
        private String mapBase64 = "";
        private String gridBase64 = "";
        private ISFSArray sceneItems = new SFSArray();
        private final SFSObject roomInfo = new SFSObject();

        public RoomState(String roomName) {
            this.roomName = roomName;
            roomInfo.putUtfString("key", roomName);
            roomInfo.putUtfString("doorKey", MapBuilder.DEFAULT_DOOR_KEY);
            roomInfo.putInt("pv", 0);
            roomInfo.putInt("dv", 0);
            mapBase64 = MapBuilder.buildMapBase64();
            doorsJson = MapBuilder.buildDoorsJson();
            gridBase64 = MapBuilder.buildGridBase64();
            botsJson = MapBuilder.buildBotsJson();
            sceneItems = MapBuilder.buildSceneItems();
        }

        public String getRoomName() {
            return roomName;
        }

        public String getDoorsJson() {
            return doorsJson;
        }

        public void setDoorsJson(String doorsJson) {
            this.doorsJson = doorsJson;
        }

        public String getBotsJson() {
            return botsJson;
        }

        public void setBotsJson(String botsJson) {
            this.botsJson = botsJson;
        }

        public SFSObject getRoomInfo() {
            return roomInfo;
        }

        public String getMapBase64() {
            return mapBase64;
        }

        public void setMapBase64(String mapBase64) {
            this.mapBase64 = mapBase64 == null ? "" : mapBase64;
        }

        public String getGridBase64() {
            return gridBase64;
        }

        public void setGridBase64(String gridBase64) {
            this.gridBase64 = gridBase64 == null ? "" : gridBase64;
        }

        public synchronized ISFSArray getSceneItems() {
            ISFSArray copy = new SFSArray();
            if (sceneItems != null) {
                for (int i = 0; i < sceneItems.size(); i++) {
                    copy.addSFSObject(sceneItems.getSFSObject(i));
                }
            }
            return copy;
        }

        public synchronized void setSceneItems(ISFSArray items) {
            sceneItems = new SFSArray();
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    sceneItems.addSFSObject(items.getSFSObject(i));
                }
            }
        }

        public SFSObject buildRoomPayload(String doorKey) {
            SFSObject room = new SFSObject();
            room.putUtfString("key", roomName);
            room.putUtfString("doorKey", doorKey == null ? MapBuilder.DEFAULT_DOOR_KEY : doorKey);
            room.putInt("pv", roomInfo.getInt("pv"));
            room.putInt("dv", roomInfo.getInt("dv"));
            room.putUtfString("map", mapBase64);
            return room;
        }
    }
}
