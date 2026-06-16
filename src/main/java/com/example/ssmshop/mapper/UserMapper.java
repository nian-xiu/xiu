package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    User findById(Long id);

    User findByUsername(String username);

    List<User> findAll();

    long countCustomers();

    int insert(User user);

    int updateProfile(User user);

    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updateCheckin(@Param("id") Long id, @Param("coins") int coins, @Param("checkinStreak") int checkinStreak);

    int increaseCoins(@Param("id") Long id, @Param("coins") int coins);

    int decreaseCoins(@Param("id") Long id, @Param("coins") int coins);
}
