package Servlet;

import Entities.GameRoom;
import Service.GameRoomManager;
import Utils.JsonUtil;
import Utils.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * PK 房间管理接口。
 *
 * POST /api/pk/room  body: { "action": "create|join|status|leave|list", "roomCode":..., "isPublic":... }
 * GET  /api/pk/room?action=list&page=1
 * GET  /api/pk/room?roomCode=1234
 *
 * 认证要求：需要用户已登录（session 中有 userId）
 */
@WebServlet("/api/pk/room")
public class PkRoomServlet extends HttpServlet {

    private final GameRoomManager roomManager = new GameRoomManager();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Long userId = ServletUtil.getSessionUserId(request);
        if (userId == null) {
            response.getWriter().write(JsonUtil.error("请先登录"));
            return;
        }

        String body = readBody(request);
        String action = extractField(body, "action");
        String roomCode = extractField(body, "roomCode");
        String isPublicStr = extractField(body, "isPublic");

        if (action == null) action = "status";

        String result;
        switch (action) {
            case "create"  -> result = handleCreate(request, userId, body);
            case "join"    -> result = handleJoin(request, userId, roomCode);
            case "status"  -> result = handleStatus(request, userId, roomCode);
            case "leave"   -> result = handleLeave(request, userId, roomCode);
            case "list"    -> result = handleList(request, body);
            case "myroom"  -> result = handleMyRoom(request, userId);
            case "disband" -> result = handleDisband(request, userId);
            case "update"  -> result = handleUpdate(request, userId, body);
            default        -> result = JsonUtil.error("未知操作: " + action);
        }

        response.getWriter().write(result);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Long userId = ServletUtil.getSessionUserId(request);
        if (userId == null) {
            response.getWriter().write(JsonUtil.error("请先登录"));
            return;
        }

        String action = request.getParameter("action");
        String roomCode = request.getParameter("roomCode");

        if ("list".equals(action)) {
            int page = ServletUtil.parseInt(request.getParameter("page"), 1);
            response.getWriter().write(handleListPage(page));
            return;
        }

        if (roomCode == null || roomCode.isBlank()) {
            response.getWriter().write(JsonUtil.error("缺少 roomCode 参数"));
            return;
        }

        response.getWriter().write(handleStatus(request, userId, roomCode));
    }

    // ============================================
    // 创建房间
    // ============================================
    private String handleCreate(HttpServletRequest request, Long userId, String body) {
        boolean isPublic = extractBoolField(body, "isPublic", false);
        String difficulty = extractField(body, "difficulty");
        if (difficulty == null || difficulty.isBlank()) difficulty = "cet4";

        GameRoom room = roomManager.createRoom(getServletContext(), userId, isPublic);
        if (room == null) {
            return JsonUtil.error("你已创建过一个房间了，请先解散旧房间再创建");
        }

        room.setDifficulty(difficulty);

        return "{\"success\":true,\"roomCode\":\"" + room.getRoomCode() + "\","
                + "\"status\":\"" + room.getStatus() + "\","
                + "\"isHost\":true,"
                + "\"isPublic\":" + room.isPublic() + ","
                + "\"difficulty\":\"" + room.getDifficulty() + "\""
                + "}";
    }

    // ============================================
    // 加入房间
    // ============================================
    private String handleJoin(HttpServletRequest request, Long userId, String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            return JsonUtil.error("请输入房间码");
        }

        String error = roomManager.joinRoom(getServletContext(), roomCode.trim(), userId);
        if (error != null) {
            return JsonUtil.error(error);
        }

        GameRoom room = roomManager.getRoom(getServletContext(), roomCode.trim());
        boolean isHost = room != null && room.isHost(userId);

        return "{\"success\":true,\"roomCode\":\"" + roomCode.trim() + "\","
                + "\"status\":\"" + (room != null ? room.getStatus() : "WAITING") + "\","
                + "\"isHost\":" + isHost + "}";
    }

    // ============================================
    // 查询房间状态
    // ============================================
    private String handleStatus(HttpServletRequest request, Long userId, String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            return JsonUtil.error("缺少 roomCode 参数");
        }

        GameRoom room = roomManager.getRoom(getServletContext(), roomCode.trim());
        if (room == null) {
            return "{\"success\":true,\"exists\":false,\"message\":\"房间不存在\"}";
        }

        return buildRoomStatusJson(room, userId);
    }

    /** 构建房间状态 JSON */
    private String buildRoomStatusJson(GameRoom room, Long userId) {
        boolean isHost  = room.isHost(userId);
        boolean isGuest = room.isGuest(userId);
        boolean isInRoom = isHost || isGuest;

        Long hostId  = room.getHostUserId();
        Long guestId = room.getGuestUserId();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":true");
        sb.append(",\"hasRoom\":").append(isHost || isGuest);
        sb.append(",\"exists\":true");
        sb.append(",\"roomCode\":\"").append(room.getRoomCode()).append("\"");
        sb.append(",\"status\":\"").append(room.getStatus()).append("\"");
        sb.append(",\"isHost\":").append(isHost);
        sb.append(",\"isGuest\":").append(isGuest);
        sb.append(",\"isInRoom\":").append(isInRoom);
        sb.append(",\"isReady\":").append(room.isReady());
        sb.append(",\"isPublic\":").append(room.isPublic());
        sb.append(",\"difficulty\":\"").append(JsonUtil.escapeJson(room.getDifficulty() != null ? room.getDifficulty() : "")).append("\"");
        sb.append(",\"totalQuestions\":").append(room.getQuestions().size());
        // 双方答题进度
        sb.append(",\"hostCorrect\":").append(hostId != null ? room.getCorrectCount(hostId) : 0);
        sb.append(",\"guestCorrect\":").append(guestId != null ? room.getCorrectCount(guestId) : 0);
        sb.append(",\"hostAnswered\":").append(hostId != null ? room.getAnsweredCount(hostId) : 0);
        sb.append(",\"guestAnswered\":").append(guestId != null ? room.getAnsweredCount(guestId) : 0);
        // 是否完成
        sb.append(",\"hostFinished\":").append(room.isHostFinished());
        sb.append(",\"guestFinished\":").append(room.isGuestFinished());
        sb.append(",\"bothFinished\":").append(room.bothFinished());
        // 耗时（秒）
        sb.append(",\"hostTotalTime\":").append(hostId != null ? room.getTotalTime(hostId) / 1000 : 0);
        sb.append(",\"guestTotalTime\":").append(guestId != null ? room.getTotalTime(guestId) / 1000 : 0);
        sb.append("}");

        return sb.toString();
    }

    // ============================================
    // 我的房间
    // ============================================
    private String handleMyRoom(HttpServletRequest request, Long userId) {
        GameRoom room = roomManager.getRoomByUser(getServletContext(), userId);
        if (room == null) {
            return "{\"success\":true,\"hasRoom\":false}";
        }
        return buildRoomStatusJson(room, userId);
    }

    // ============================================
    // 解散房间（仅房主）
    // ============================================
    private String handleDisband(HttpServletRequest request, Long userId) {
        String error = roomManager.disbandRoom(getServletContext(), userId);
        if (error != null) {
            return JsonUtil.error(error);
        }
        return "{\"success\":true,\"message\":\"房间已解散\"}";
    }

    // ============================================
    // 更新房间设置（仅房主，WAITING 状态）
    // ============================================
    private String handleUpdate(HttpServletRequest request, Long userId, String body) {
        String roomCode = extractField(body, "roomCode");
        if (roomCode == null || roomCode.isBlank()) {
            return JsonUtil.error("缺少 roomCode");
        }
        GameRoom room = roomManager.getRoom(getServletContext(), roomCode.trim());
        if (room == null) {
            return JsonUtil.error("房间不存在");
        }
        if (!room.isHost(userId)) {
            return JsonUtil.error("只有房主可以修改设置");
        }
        if (!GameRoom.WAITING.equals(room.getStatus())) {
            return JsonUtil.error("只能在等待状态下修改设置");
        }

        // 可修改字段
        String difficulty = extractField(body, "difficulty");
        if (difficulty != null && !difficulty.isBlank()) {
            room.setDifficulty(difficulty);
        }
        if (body.contains("\"isPublic\"")) {
            room.setPublic(extractBoolField(body, "isPublic", room.isPublic()));
        }
        if (body.contains("\"difficulty\"")) {
            String diff = extractField(body, "difficulty");
            if (diff != null && !diff.isBlank()) room.setDifficulty(diff);
        }

        return buildRoomStatusJson(room, userId);
    }

    // ============================================
    // 房间列表（POST body 带 page）
    // ============================================
    private String handleList(HttpServletRequest request, String body) {
        int page = extractIntField(body, "page", 1);
        return handleListPage(page);
    }

    private String handleListPage(int page) {
        GameRoomManager.RoomListResult result = roomManager.listPublicRooms(getServletContext(), page);
        List<GameRoomManager.RoomInfo> items = result.getItems();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":true");
        sb.append(",\"total\":").append(result.getTotal());
        sb.append(",\"page\":").append(result.getPage());
        sb.append(",\"totalPages\":").append(result.getTotalPages());
        sb.append(",\"rooms\":[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            GameRoomManager.RoomInfo r = items.get(i);
            sb.append("{");
            sb.append("\"roomCode\":\"").append(r.getRoomCode()).append("\",");
            sb.append("\"hostName\":\"").append(JsonUtil.escapeJson(r.getHostName())).append("\",");
            sb.append("\"difficulty\":\"").append(JsonUtil.escapeJson(r.getDifficulty())).append("\",");
            sb.append("\"isReady\":").append(r.isReady());
            sb.append("}");
        }
        sb.append("]}");

        return sb.toString();
    }

    // ============================================
    // 离开房间
    // ============================================
    private String handleLeave(HttpServletRequest request, Long userId, String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            return JsonUtil.error("缺少 roomCode 参数");
        }

        String error = roomManager.leaveRoom(getServletContext(), roomCode.trim(), userId);
        if (error != null) {
            return JsonUtil.error(error);
        }

        return "{\"success\":true,\"message\":\"已离开房间\"}";
    }

    // ============================================
    // JSON 字段提取
    // ============================================
    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private String extractField(String body, String fieldName) {
        for (String pattern : new String[]{
                "\"" + fieldName + "\":\"",
                "\"" + fieldName + "\": \"",
                "\"" + fieldName + "\" :\""
        }) {
            int start = body.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                StringBuilder val = new StringBuilder();
                for (int i = start; i < body.length(); i++) {
                    char c = body.charAt(i);
                    if (c == '\\' && i + 1 < body.length()) {
                        char next = body.charAt(i + 1);
                        switch (next) {
                            case '"' -> { val.append('"'); i++; }
                            case '\\' -> { val.append('\\'); i++; }
                            case 'n' -> { val.append('\n'); i++; }
                            case 'r' -> { val.append('\r'); i++; }
                            case 't' -> { val.append('\t'); i++; }
                            default -> val.append(c);
                        }
                    } else if (c == '"') {
                        break;
                    } else {
                        val.append(c);
                    }
                }
                return val.toString();
            }
        }
        return null;
    }

    private int extractIntField(String body, String fieldName, int defaultVal) {
        for (String pattern : new String[]{
                "\"" + fieldName + "\":",
                "\"" + fieldName + "\": ",
                "\"" + fieldName + "\" :"
        }) {
            int start = body.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                while (start < body.length() && body.charAt(start) == ' ') start++;
                StringBuilder num = new StringBuilder();
                while (start < body.length() && Character.isDigit(body.charAt(start))) {
                    num.append(body.charAt(start));
                    start++;
                }
                if (num.length() > 0) {
                    try { return Integer.parseInt(num.toString()); }
                    catch (NumberFormatException e) { return defaultVal; }
                }
            }
        }
        return defaultVal;
    }

    private boolean extractBoolField(String body, String fieldName, boolean defaultVal) {
        for (String pattern : new String[]{
                "\"" + fieldName + "\":",
                "\"" + fieldName + "\": ",
                "\"" + fieldName + "\" :"
        }) {
            int start = body.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                while (start < body.length() && body.charAt(start) == ' ') start++;
                if (body.substring(start).startsWith("true")) return true;
                if (body.substring(start).startsWith("false")) return false;
            }
        }
        return defaultVal;
    }
}
