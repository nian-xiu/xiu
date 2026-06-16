package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.RewardMail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RewardMailMapper {
    int insert(RewardMail mail);

    List<RewardMail> findByUserId(@Param("userId") Long userId);

    List<RewardMail> findAll();

    RewardMail findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<RewardMail> findClaimableByUserId(@Param("userId") Long userId);

    int countUnreadByUser(@Param("userId") Long userId);

    int markViewedByUser(@Param("userId") Long userId);

    int markClaimed(@Param("id") Long id);
}
