package src5;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import java.util.ArrayList;
import java.util.List;

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

    protected void ensureMandatoryRoomVars(Room room, InMemoryStore.RoomState roomState, String stage) {
        if (room == null) {
            return;
        }
        if (roomState == null) {
            roomState = getStore().getOrCreateRoom(room);
        }
        List<RoomVariable> vars = new ArrayList<>();

        if (!room.containsVariable("roomKey")) {
            vars.add(new SFSRoomVariable("roomKey", room.getName()));
        }
        if (!room.containsVariable("width")) {
            vars.add(new SFSRoomVariable("width", MapBuilder.ROOM_WIDTH));
        }
        if (!room.containsVariable("height")) {
            vars.add(new SFSRoomVariable("height", MapBuilder.ROOM_HEIGHT));
        }
        if (!room.containsVariable("type")) {
            vars.add(new SFSRoomVariable("type", "OUTDOOR"));
        }
        if (!room.containsVariable("roomTitle")) {
            vars.add(new SFSRoomVariable("roomTitle", room.getName()));
        }
        if (!room.containsVariable("isInteractiveRoom")) {
            vars.add(new SFSRoomVariable("isInteractiveRoom", true));
        }
        if (!room.containsVariable("doors")) {
            String doorsJson = roomState != null ? roomState.getDoorsJson() : "[]";
            vars.add(new SFSRoomVariable("doors", doorsJson));
        }
        if (!room.containsVariable("bots")) {
            String botsJson = roomState != null ? roomState.getBotsJson() : "[]";
            vars.add(new SFSRoomVariable("bots", botsJson));
        }
        if (!room.containsVariable("grid")) {
            String gridBase64 = roomState != null ? roomState.getGridBase64() : "";
            vars.add(new SFSRoomVariable("grid", gridBase64));
        }

        if (!vars.isEmpty()) {
            getApi().setRoomVariables(null, room, vars);
            trace("[ROOM_VARS_BROADCAST] stage=" + stage + " room=" + room.getName()
                + " vars=" + collectRoomVarNames(vars));
        }

        boolean hasInteractive = room.containsVariable("isInteractiveRoom") || containsRoomVar(vars, "isInteractiveRoom");
        boolean hasGrid = room.containsVariable("grid") || containsRoomVar(vars, "grid");
        boolean hasDoors = room.containsVariable("doors") || containsRoomVar(vars, "doors");
        boolean hasBots = room.containsVariable("bots") || containsRoomVar(vars, "bots");
        trace("[ROOM_VARS_GATE] stage=" + stage + " room=" + room.getName()
            + " has={isInteractiveRoom:" + hasInteractive
            + ",grid:" + hasGrid
            + ",doors:" + hasDoors
            + ",bots:" + hasBots + "}");
    }

    private boolean containsRoomVar(List<RoomVariable> vars, String key) {
        for (RoomVariable var : vars) {
            if (var != null && key.equals(var.getName())) {
                return true;
            }
        }
        return false;
    }

    private String collectRoomVarNames(List<RoomVariable> vars) {
        StringBuilder builder = new StringBuilder();
        for (RoomVariable var : vars) {
            if (var == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(var.getName());
        }
        return builder.toString();
    }
}
