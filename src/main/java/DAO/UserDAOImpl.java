package DAO;

import Entities.User;
import Utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT user_id, username, password, salt, profile, role, " +
                     "study_purpose, literacy, last_literacy, experience, " +
                     "last_checkin, created_at, last_login " +
                     "FROM user WHERE username = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("查询用户失败: " + username, e);
        }
        return null;
    }

    @Override
    public User findById(Long userId) {
        String sql = "SELECT user_id, username, password, salt, profile, role, " +
                     "study_purpose, literacy, last_literacy, experience, " +
                     "last_checkin, created_at, last_login " +
                     "FROM user WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("查询用户失败: " + userId, e);
        }
        return null;
    }

    @Override
    public int insert(User user) {
        String sql = "INSERT INTO user (username, password, salt, profile, role, " +
                     "study_purpose, literacy, last_literacy, experience, " +
                     "last_checkin, created_at, last_login) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getProfile());
            ps.setString(5, user.getRole() != null ? user.getRole() : "normal");
            ps.setString(6, user.getStudyPurpose());
            ps.setInt(7, user.getLiteracy() != null ? user.getLiteracy() : 0);
            ps.setTimestamp(8, toTimestamp(user.getLastLiteracy()));
            ps.setInt(9, user.getExperience() != null ? user.getExperience() : 0);
            ps.setTimestamp(10, toTimestamp(user.getLastCheckin()));
            ps.setTimestamp(11, toTimestamp(
                    user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()));
            ps.setTimestamp(12, toTimestamp(user.getLastLogin()));

            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("插入用户失败: " + user.getUsername(), e);
        }
    }

    @Override
    public int updateLoginTime(Long userId, LocalDateTime time) {
        String sql = "UPDATE user SET last_login = ? WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, toTimestamp(time));
            ps.setLong(2, userId);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("更新登录时间失败", e);
        }
    }

    @Override
    public int updateCheckin(Long userId, LocalDateTime time, int experience) {
        String sql = "UPDATE user SET last_checkin = ?, experience = ? WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, toTimestamp(time));
            ps.setInt(2, experience);
            ps.setLong(3, userId);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("更新签到失败", e);
        }
    }

    @Override
    public int updateExperience(Long userId, int experience) {
        String sql = "UPDATE user SET experience = ? WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, experience);
            ps.setLong(2, userId);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("更新用户经验值失败: userId=" + userId, e);
        }
    }

    @Override
    public int updateLiteracy(Long userId, int literacy) {
        String sql = "UPDATE user SET literacy = ? WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, literacy);
            ps.setLong(2, userId);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("更新用户词汇量失败: userId=" + userId, e);
        }
    }

    // ========== profile 字段统一存储（JSON 格式，同时存 CEFR 进度和 VIP 状态） ==========

    /** 从 profile JSON 中提取 CEFR 进度（兼容旧格式 "vip" 字符串） */
    static int readProgressFromProfile(String profile) {
        if (profile == null || profile.isBlank()) return 0;
        if ("vip".equals(profile)) return 0; // 旧格式：纯 VIP 字符串，无进度
        try {
            int idx = profile.indexOf("\"cefrProgress\"");
            if (idx < 0) return 0;
            int colon = profile.indexOf(":", idx);
            int end = profile.indexOf(",", colon);
            if (end < 0) end = profile.indexOf("}", colon);
            if (colon < 0 || end < 0) return 0;
            return Integer.parseInt(profile.substring(colon + 1, end).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /** 判断 profile 是否表示 VIP（兼容旧格式 "vip" 和新 JSON 格式） */
    static boolean isVipInProfile(String profile) {
        if (profile == null) return false;
        if ("vip".equals(profile)) return true;
        return profile.contains("\"vip\":true");
    }

    /** 构建统一的 profile JSON */
    static String buildProfileJson(int progress, boolean isVip) {
        if (isVip) {
            return "{\"cefrProgress\":" + progress + ",\"vip\":true}";
        }
        return "{\"cefrProgress\":" + progress + "}";
    }

    @Override
    public int updateCefrProgress(Long userId, int progress) {
        // 读取当前 profile 以保留 VIP 状态
        boolean isVip = false;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT profile FROM user WHERE user_id = ?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    isVip = isVipInProfile(rs.getString("profile"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("读取用户profile失败: userId=" + userId, e);
        }

        String sql = "UPDATE user SET profile = ? WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, buildProfileJson(progress, isVip));
            ps.setLong(2, userId);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("更新CEFR进度失败: userId=" + userId, e);
        }
    }

    private User mapRow(ResultSet rs) throws Exception {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setSalt(rs.getString("salt"));
        user.setProfile(isVipInProfile(rs.getString("profile")) ? "vip" : "");
        user.setRole(rs.getString("role"));
        user.setStudyPurpose(rs.getString("study_purpose"));
        user.setLiteracy(rs.getInt("literacy"));
        user.setLastLiteracy(toLocalDateTime(rs.getTimestamp("last_literacy")));
        user.setExperience(rs.getInt("experience"));
        user.setCefrProgress(readProgressFromProfile(rs.getString("profile")));
        user.setLastCheckin(toLocalDateTime(rs.getTimestamp("last_checkin")));
        user.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        user.setLastLogin(toLocalDateTime(rs.getTimestamp("last_login")));
        return user;
    }

    // ========== 管理员方法 ==========

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password, salt, profile, role, " +
                     "study_purpose, literacy, last_literacy, experience, " +
                     "last_checkin, created_at, last_login " +
                     "FROM user ORDER BY user_id DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("查询全部用户失败", e);
        }
        return users;
    }

    @Override
    public int deleteById(Long userId) {
        String sql = "DELETE FROM user WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("删除用户失败: " + userId, e);
        }
    }

    @Override
    public int updatePassword(Long userId, String password, String salt) {
        String sql = "UPDATE user SET password = ?, salt = ? WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, password);
            ps.setString(2, salt);
            ps.setLong(3, userId);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("重置密码失败: " + userId, e);
        }
    }

    @Override
    public int updateStudyPurpose(Long userId, String studyPurpose) {
        String sql = "UPDATE user SET study_purpose = ? WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studyPurpose);
            ps.setLong(2, userId);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("更新学习阶段失败: " + userId, e);
        }
    }

    @Override
    public int updateRole(Long userId, String role) {
        String sql = "UPDATE user SET role = ? WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role);
            ps.setLong(2, userId);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("更新角色失败: " + userId, e);
        }
    }

    @Override
    public int updateVip(Long userId, String vipStatus, LocalDateTime vipExpireAt, int newExperience) {
        // 读取当前 profile 以保留 CEFR 进度
        int currentProgress = 0;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT profile FROM user WHERE user_id = ?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    currentProgress = readProgressFromProfile(rs.getString("profile"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("读取用户profile失败: userId=" + userId, e);
        }

        boolean isVip = "vip".equals(vipStatus);
        String profile = buildProfileJson(currentProgress, isVip);

        String sql = "UPDATE user SET profile = ?, last_checkin = ?, experience = ? WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, profile);
            ps.setTimestamp(2, toTimestamp(vipExpireAt));
            ps.setInt(3, newExperience);
            ps.setLong(4, userId);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("VIP兑换失败: " + userId, e);
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private Timestamp toTimestamp(LocalDateTime dt) {
        return dt != null ? Timestamp.valueOf(dt) : null;
    }
}
