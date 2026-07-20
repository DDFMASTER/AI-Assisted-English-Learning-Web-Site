package Service;

import DAO.UserDAO;
import DAO.UserDAOImpl;
import Entities.GameRoom;
import Entities.User;
import Listener.AppContextListener;
import jakarta.servlet.ServletContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * PK 房间管理服务 —— 管理游戏房间的创建、加入、离开和状态查询。
 *
 * 使用 ServletContext 中的 ROOMS_MAP 存储所有活跃房间，
 * 键为 roomCode (4位数字字符串)，值为 GameRoom 对象。
 *
 * 过期清理：超过 30 分钟未活动的 WAITING 房间视为过期，
 * 每次 createRoom 或 joinRoom 前触发清理。
 */
public class GameRoomManager {

    /** WAITING 房间超时时间（毫秒）默认 30 分钟 */
    private static final long ROOM_TIMEOUT_MS = 30 * 60 * 1000;

    /** FINISHED 房间保留时间（毫秒）默认 5 分钟，供双方查看结果后自动清理 */
    private static final long FINISHED_ROOM_TTL_MS = 5 * 60 * 1000;

    /** 每页房间数量 */
    private static final int ROOMS_PER_PAGE = 6;

    private final UserDAO userDAO = new UserDAOImpl();

    // ============================================
    // 获取房间映射
    // ============================================
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, GameRoom> getRoomMap(ServletContext ctx) {
        ConcurrentHashMap<String, GameRoom> map =
                (ConcurrentHashMap<String, GameRoom>) ctx.getAttribute(AppContextListener.ROOMS_MAP);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            ctx.setAttribute(AppContextListener.ROOMS_MAP, map);
        }
        return map;
    }

    // ============================================
    // 创建房间
    // ============================================
    /**
     * @return 创建成功返回 GameRoom；
     *         若用户已有房间则返回 null（调用方应提示先解散旧房间）
     */
    public GameRoom createRoom(ServletContext ctx, Long hostUserId, boolean isPublic) {
        cleanupExpired(ctx);
        // 检查用户是否已有房间
        GameRoom existing = getRoomByUser(ctx, hostUserId);
        if (existing != null) {
            return null; // 已有房间
        }
        String roomCode = generateRoomCode(ctx);
        GameRoom room = new GameRoom(roomCode, hostUserId, isPublic);
        getRoomMap(ctx).put(roomCode, room);
        return room;
    }

    // ============================================
    // 根据用户 ID 查找其所在房间
    // ============================================
    public GameRoom getRoomByUser(ServletContext ctx, Long userId) {
        ConcurrentHashMap<String, GameRoom> map = getRoomMap(ctx);
        for (GameRoom room : map.values()) {
            if (GameRoom.FINISHED.equals(room.getStatus())) continue;
            if (room.isHost(userId) || room.isGuest(userId)) {
                return room;
            }
        }
        return null;
    }

    // ============================================
    // 房主解散房间（彻底删除）
    // ============================================
    public String disbandRoom(ServletContext ctx, Long userId) {
        GameRoom room = getRoomByUser(ctx, userId);
        if (room == null) {
            return "你没有进行中的房间";
        }
        if (!room.isHost(userId)) {
            return "只有房主可以解散房间";
        }
        getRoomMap(ctx).remove(room.getRoomCode());
        return null; // 成功
    }

    // ============================================
    // 加入房间
    // ============================================
    public String joinRoom(ServletContext ctx, String roomCode, Long guestUserId) {
        cleanupExpired(ctx);
        ConcurrentHashMap<String, GameRoom> map = getRoomMap(ctx);
        GameRoom room = map.get(roomCode);

        if (room == null) {
            return "房间码无效或房间不存在，请检查后重试";
        }
        if (!GameRoom.WAITING.equals(room.getStatus())) {
            return "该房间已开始游戏或已结束";
        }
        if (room.getGuestUserId() != null) {
            return "该房间已满，无法加入";
        }
        if (room.getHostUserId() != null && room.getHostUserId().equals(guestUserId)) {
            return "不能加入自己创建的房间";
        }

        room.setGuestUserId(guestUserId);
        return null; // 成功，null 表示无错误
    }

    // ============================================
    // 离开房间 / 取消房间
    // ============================================
    public String leaveRoom(ServletContext ctx, String roomCode, Long userId) {
        ConcurrentHashMap<String, GameRoom> map = getRoomMap(ctx);
        GameRoom room = map.get(roomCode);

        if (room == null) {
            return "房间不存在";
        }

        // 只有房主可以取消整个房间（WAITING 状态下）
        if (room.isHost(userId)) {
            if (GameRoom.WAITING.equals(room.getStatus())) {
                map.remove(roomCode);
                return null;
            }
            // 游戏进行中房主退出 → 对手获胜
            room.finish();
            return null;
        }

        // 加入者退出
        if (room.isGuest(userId)) {
            if (GameRoom.WAITING.equals(room.getStatus())) {
                room.setGuestUserId(null);
                return null;
            }
            // 游戏进行中加入者退出 → 房主获胜
            room.finish();
            return null;
        }

        return "你不是该房间的参与者";
    }

    // ============================================
    // 获取房间信息
    // ============================================
    public GameRoom getRoom(ServletContext ctx, String roomCode) {
        return getRoomMap(ctx).get(roomCode);
    }

    // ============================================
    // 公开房间列表（分页）
    // ============================================
    public RoomListResult listPublicRooms(ServletContext ctx, int page) {
        cleanupExpired(ctx);
        ConcurrentHashMap<String, GameRoom> map = getRoomMap(ctx);

        // 筛选 WAITING 且公开的房间
        List<GameRoom> publicRooms = map.values().stream()
                .filter(r -> GameRoom.WAITING.equals(r.getStatus()) && r.isPublic())
                .sorted(Comparator.comparingLong(GameRoom::getCreatedAt).reversed())
                .collect(Collectors.toList());

        int total = publicRooms.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / ROOMS_PER_PAGE));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int fromIndex = (page - 1) * ROOMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROOMS_PER_PAGE, total);

        List<RoomInfo> items = new ArrayList<>();
        for (int i = fromIndex; i < toIndex; i++) {
            GameRoom r = publicRooms.get(i);
            String hostName = "";
            User host = userDAO.findById(r.getHostUserId());
            if (host != null) hostName = host.getUsername();
            items.add(new RoomInfo(
                    r.getRoomCode(),
                    hostName,
                    r.getDifficulty(),
                    r.isReady(),
                    r.isPublic()
            ));
        }

        return new RoomListResult(total, page, totalPages, items);
    }

    public static class RoomInfo {
        private final String  roomCode;
        private final String  hostName;
        private final String  difficulty;
        private final boolean ready;
        private final boolean isPublic;

        public RoomInfo(String roomCode, String hostName, String difficulty, boolean ready, boolean isPublic) {
            this.roomCode   = roomCode;
            this.hostName   = hostName;
            this.difficulty = difficulty;
            this.ready      = ready;
            this.isPublic   = isPublic;
        }
        public String  getRoomCode()  { return roomCode; }
        public String  getHostName()  { return hostName; }
        public String  getDifficulty(){ return difficulty; }
        public boolean isReady()      { return ready; }
        public boolean isPublic()     { return isPublic; }
    }

    public static class RoomListResult {
        private final int total;
        private final int page;
        private final int totalPages;
        private final List<RoomInfo> items;

        public RoomListResult(int total, int page, int totalPages, List<RoomInfo> items) {
            this.total = total;
            this.page = page;
            this.totalPages = totalPages;
            this.items = items;
        }
        public int getTotal()      { return total; }
        public int getPage()       { return page; }
        public int getTotalPages() { return totalPages; }
        public List<RoomInfo> getItems() { return items; }
    }

    // ============================================
    // 私有辅助方法
    // ============================================

    /** 生成不重复的4位房间码 */
    private String generateRoomCode(ServletContext ctx) {
        ConcurrentHashMap<String, GameRoom> map = getRoomMap(ctx);
        String code;
        int attempts = 0;
        do {
            code = String.format("%04d", (int) (Math.random() * 10000));
            attempts++;
            if (attempts > 100) {
                // 极端情况：遍历所有可能
                for (int i = 0; i < 10000; i++) {
                    String c = String.format("%04d", i);
                    if (!map.containsKey(c)) return c;
                }
            }
        } while (map.containsKey(code));
        return code;
    }

    /** 清理过期房间（WAITING 超时 30 分钟 / FINISHED 超时 5 分钟） */
    private void cleanupExpired(ServletContext ctx) {
        ConcurrentHashMap<String, GameRoom> map = getRoomMap(ctx);
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, GameRoom>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            GameRoom room = it.next().getValue();
            // WAITING 房间：超过 30 分钟未开始 → 清理
            if (GameRoom.WAITING.equals(room.getStatus())
                    && (now - room.getCreatedAt()) > ROOM_TIMEOUT_MS) {
                it.remove();
            }
            // FINISHED 房间：结束后超过 5 分钟 → 清理
            else if (GameRoom.FINISHED.equals(room.getStatus())
                    && room.getFinishedAt() > 0
                    && (now - room.getFinishedAt()) > FINISHED_ROOM_TTL_MS) {
                it.remove();
            }
        }
    }
}
