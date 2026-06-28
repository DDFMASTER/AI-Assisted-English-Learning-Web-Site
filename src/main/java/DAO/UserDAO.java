package DAO;

import Entities.User;

import java.time.LocalDateTime;

public interface UserDAO {
    User findByUsername(String username);

    User findById(Long userId);

    int insert(User user);

    int updateLoginTime(Long userId, LocalDateTime time);

    int updateCheckin(Long userId, LocalDateTime time, int experience);
}
