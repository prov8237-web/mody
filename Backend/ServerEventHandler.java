package src5;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import java.util.ArrayList;
import java.util.List;

public class ServerEventHandler extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent event) {
        SFSEventType type = (SFSEventType) event.getType();
        MainExtension ext = (MainExtension) getParentExtension();
        if (ext == null) {
            return;
        }
        switch (type) {
            case USER_LOGIN:
                onUserLogin(event, ext);
                break;
            case USER_JOIN_ROOM:
                onUserJoinRoom(event, ext);
                break;
            case USER_LEAVE_ROOM:
                onUserLeaveRoom(event, ext);
                break;
            case USER_DISCONNECT:
                onUserDisconnect(event, ext);
                break;
            default:
                break;
        }
    }

    private void onUserLogin(ISFSEvent event, MainExtension ext) {
        User user = (User) event.getParameter(SFSEventParam.USER);
        if (user == null) {
            return;
        }
        InMemoryStore store = ext.getStore();
        InMemoryStore.UserState state = store.getOrCreateUser(user);
        String displayName = store.ensureUniqueDisplayName(state.getAvatarName(), user.getId());
        state.setAvatarName(displayName);

        trace("[SERVER_EVENT] USER_LOGIN init vars for " + user.getName());
    }

    private void onUserJoinRoom(ISFSEvent event, MainExtension ext) {
        Room room = (Room) event.getParameter(SFSEventParam.ROOM);
        User user = (User) event.getParameter(SFSEventParam.USER);
        if (room == null || user == null) {
            return;
        }
        InMemoryStore store = ext.getStore();
        InMemoryStore.UserState userState = store.getOrCreateUser(user);
        userState.setCurrentRoom(room.getName());
        InMemoryStore.RoomState roomState = store.getOrCreateRoom(room);

        List<RoomVariable> roomVars = new ArrayList<>();
        roomVars.add(new SFSRoomVariable("doors", roomState.getDoorsJson()));
        roomVars.add(new SFSRoomVariable("bots", roomState.getBotsJson()));
        roomVars.add(new SFSRoomVariable("grid", roomState.getGridBase64()));
        roomVars.add(new SFSRoomVariable("isInteractiveRoom", true));
        roomVars.add(new SFSRoomVariable("isGameStarted", false));
        roomVars.add(new SFSRoomVariable("isGameEnded", false));
        getApi().setRoomVariables(null, room, roomVars, false, false, false);
        trace("[SERVER_EVENT] USER_JOIN_ROOM room vars for " + room.getName());
    }

    private void onUserLeaveRoom(ISFSEvent event, MainExtension ext) {
        User user = (User) event.getParameter(SFSEventParam.USER);
        if (user == null) {
            return;
        }
        InMemoryStore store = ext.getStore();
        InMemoryStore.UserState state = store.getOrCreateUser(user);
        state.setCurrentRoom("");
        trace("[SERVER_EVENT] USER_LEAVE_ROOM cleared room for " + user.getName());
    }

    private void onUserDisconnect(ISFSEvent event, MainExtension ext) {
        User user = (User) event.getParameter(SFSEventParam.USER);
        if (user == null) {
            return;
        }
        InMemoryStore store = ext.getStore();
        InMemoryStore.UserState state = store.getOrCreateUser(user);
        store.releaseDisplayName(state.getAvatarName());
    }
}
