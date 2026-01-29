package src5;

import com.smartfoxserver.v2.extensions.SFSExtension;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MainExtension extends SFSExtension {
    
    private Set<String> registeredHandlers = new HashSet<>();
    private Map<String, Integer> commandStats = new ConcurrentHashMap<>();
    private final InMemoryStore store = new InMemoryStore();
    
    public void markResponseSent(String command, User user) {
        trace("✅ [RESPONSE-TRACKED] " + command + " for " + user.getName());
    }

    @Override
    public void init() {
        trace("════════════════════════════════════════════════════════");
        trace("🎮 MARHAB EXTENSION - OFFICIAL SERVER REPLICA v1.0");
        trace("════════════════════════════════════════════════════════");
        trace("🎨 Local Avatar Rendering: ENABLED");
        
        // Core handlers - matching official server
        registerHandler("config", ConfigHandler.class);
        registerHandler("init", InitHandler.class);
        registerHandler("baseclothes", BaseClothesHandler.class);
        registerHandler("clothlist", ClothListHandler.class);
        registerHandler("savebaseclothes", SaveBaseClothesHandler.class);
        registerHandler("buddylist", BuddyListHandler.class);
        registerHandler("roomjoincomplete", RoomJoinCompleteHandler.class);
        registerHandler("recommendeduniverse", GenericRequestHandler.class);
        registerHandler("questlist", QuestListHandler.class);
        registerHandler("questaction", QuestActionHandler.class);
        registerHandler("walkrequest", WalkRequestHandler.class);
        registerHandler("walkfinalrequest", WalkFinalRequestHandler.class);
        registerHandler("usechatballoon", UseChatBalloonHandler.class);
        registerHandler("sceneitems", SceneItemsHandler.class);
        registerHandler("savesceneitems", SaveSceneItemsHandler.class);
        
        // Ping/Pong - official server responds with "pong"
        registerHandler("ping", PingHandler.class);
        
        // Door handlers
        registerHandler("teleport", TeleportHandler.class);
        registerHandler("usedoor", UseDoorHandler.class);
        registerHandler("usehousedoor", UseHouseDoorHandler.class);
        registerHandler("useobjectdoor", UseObjectDoorHandler.class);
        
        // Empty handlers for requests that don't need response data
        registerHandler("roles", RolesHandler.class);
        registerHandler("trace", TraceHandler.class);

        // Additional client-used commands handled by GenericRequestHandler
        registerHandler("randomwheel", GenericRequestHandler.class);
        registerHandler("purchase", GenericRequestHandler.class);
        registerHandler("flatpurchase", GenericRequestHandler.class);
        registerHandler("usehanditem", GenericRequestHandler.class);
        registerHandler("transferresponse", GenericRequestHandler.class);
        registerHandler("giftcheckexchange", GenericRequestHandler.class);
        registerHandler("transferrequest", GenericRequestHandler.class);
        registerHandler("startroomvideo", GenericRequestHandler.class);
        registerHandler("partyIsland.rollDice", GenericRequestHandler.class);
        registerHandler("partyIsland.leave", GenericRequestHandler.class);
        registerHandler("dropthrowaction", GenericRequestHandler.class);
        registerHandler("buddyrespondinvitelocation", GenericRequestHandler.class);
        registerHandler("buddyacceptinvitegame", GenericRequestHandler.class);
        registerHandler("buddyinvitelocation", GenericRequestHandler.class);
        registerHandler("buddylocate", GenericRequestHandler.class);
        registerHandler("diamondtransferresponse", GenericRequestHandler.class);
        registerHandler("diamondtransferrequest", GenericRequestHandler.class);
        registerHandler("addbuddy", GenericRequestHandler.class);
        registerHandler("changemood", GenericRequestHandler.class);
        registerHandler("changestatusmessage", GenericRequestHandler.class);
        registerHandler("changebuddyrating", GenericRequestHandler.class);
        registerHandler("addbuddyresponse", GenericRequestHandler.class);
        registerHandler("removebuddy", GenericRequestHandler.class);
        registerHandler("barterresponse", GenericRequestHandler.class);
        registerHandler("barterrequest", GenericRequestHandler.class);
        registerHandler("bartercancel", GenericRequestHandler.class);
        registerHandler("farmimplantation", GenericRequestHandler.class);
        registerHandler("drop", GenericRequestHandler.class);
        registerHandler("changeobjectlock", GenericRequestHandler.class);
        registerHandler("changeobjectframe", GenericRequestHandler.class);
        registerHandler("gatheritemsearch", GenericRequestHandler.class);
        registerHandler("gatheritemcollect", GenericRequestHandler.class);
        registerHandler("avatarsalescollect", GenericRequestHandler.class);
        registerHandler("campaignquest", GenericRequestHandler.class);
        registerHandler("farmclean", GenericRequestHandler.class);
        registerHandler("farmgather", GenericRequestHandler.class);
        registerHandler("matchmakingCancel", GenericRequestHandler.class);
        registerHandler("removeavatarrestriction", GenericRequestHandler.class);
        registerHandler("exchangediamond", GenericRequestHandler.class);
        registerHandler("whisper", GenericRequestHandler.class);
        registerHandler("privatechatlist", GenericRequestHandler.class);
        registerHandler("messagedetails", GenericRequestHandler.class);
        registerHandler("privatechatdeletegroup", GenericRequestHandler.class);
        registerHandler("flatsettings", GenericRequestHandler.class);
        registerHandler("flatpassword", GenericRequestHandler.class);
        registerHandler("debugcommand", GenericRequestHandler.class);
        registerHandler("roommessage", GenericRequestHandler.class);
        registerHandler("orderlist", GenericRequestHandler.class);

        trace("════════════════════════════════════════════════════════");
        trace("✅ Handlers Registered: " + registeredHandlers.size());
        trace("📋 Commands: " + registeredHandlers);
        trace("⚙ STRICT_PROTOCOL=" + ProtocolConfig.strictProtocol() + " DEV_FALLBACK=" + ProtocolConfig.devFallback());
        trace("════════════════════════════════════════════════════════");

        addEventHandler(SFSEventType.USER_LOGIN, ServerEventHandler.class);
        addEventHandler(SFSEventType.USER_JOIN_ROOM, ServerEventHandler.class);
        addEventHandler(SFSEventType.USER_LEAVE_ROOM, ServerEventHandler.class);
        addEventHandler(SFSEventType.USER_DISCONNECT, ServerEventHandler.class);
        
    }
    
    private void registerHandler(String command, Class<?> handlerClass) {
        addRequestHandler(command, handlerClass);
        addRequestHandler("walkfinalrequest", WalkFinalRequestHandler.class);
        registeredHandlers.add(command);
    }

    @Override
    public void handleClientRequest(String requestId, User user, ISFSObject params) {
        commandStats.merge(requestId, 1, Integer::sum);
        user.setProperty("lastRequestId", requestId);
        
        trace("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        trace("📥 CLIENT REQUEST: " + requestId);
        trace("User: " + user.getName() + " | IP: " + user.getSession().getAddress());
        
        if (!registeredHandlers.contains(requestId)) {
            trace("⚠️ UNREGISTERED COMMAND: " + requestId);
        }
        
        if (params != null && params.size() > 0) {
            trace("Params: " + params.getDump());
        }
        trace("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        if (!registeredHandlers.contains(requestId)) {
            if (ProtocolConfig.devFallback() && !ProtocolConfig.strictProtocol()) {
                trace("🧪 DEV_FALLBACK enabled. Using default handler for: " + requestId);
                ISFSObject res = DefaultResponseFactory.buildResponse(requestId, user, params, this);
                send(requestId, res, user);
                markResponseSent(requestId, user);
                return;
            }

            trace("❌ Unhandled command: " + requestId);
            if (ProtocolConfig.strictProtocol()) {
                throw new IllegalStateException("Strict protocol: unhandled command " + requestId);
            }
            ISFSObject error = new com.smartfoxserver.v2.entities.data.SFSObject();
            error.putUtfString("errorCode", "UNHANDLED_COMMAND");
            error.putUtfString("message", "No handler for command: " + requestId);
            send(requestId, error, user);
            markResponseSent(requestId, user);
            return;
        }

        RequestValidator.validateRequest(requestId, params);
        super.handleClientRequest(requestId, user, params);
    }

    public InMemoryStore getStore() {
        return store;
    }
}
