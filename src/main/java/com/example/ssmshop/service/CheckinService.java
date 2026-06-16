package com.example.ssmshop.service;

import com.example.ssmshop.domain.User;
import com.example.ssmshop.domain.UserCoupon;
import com.example.ssmshop.dto.CheckinStatus;
import com.example.ssmshop.mapper.UserCouponMapper;
import com.example.ssmshop.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CheckinService {
    private static final int[] REWARDS = {188, 288, 388, 488, 588, 888, 0};

    private final UserMapper userMapper;
    private final UserCouponMapper userCouponMapper;

    public CheckinService(UserMapper userMapper, UserCouponMapper userCouponMapper) {
        this.userMapper = userMapper;
        this.userCouponMapper = userCouponMapper;
    }

    public CheckinStatus status(Long userId) {
        User user = userMapper.findById(userId);
        return buildStatus(user);
    }

    @Transactional
    public CheckinStatus checkin(Long userId) {
        User user = userMapper.findById(userId);
        LocalDate today = LocalDate.now();
        if (today.equals(user.getLastCheckinDate())) {
            return buildStatus(user);
        }
        int currentStreak = user.getCheckinStreak() == null ? 0 : user.getCheckinStreak();
        int nextStreak = user.getLastCheckinDate() != null && user.getLastCheckinDate().plusDays(1).equals(today)
                ? currentStreak + 1
                : 1;
        if (nextStreak > 7) {
            nextStreak = 1;
        }
        int reward = REWARDS[nextStreak - 1];
        userMapper.updateCheckin(userId, reward, nextStreak);
        if (nextStreak == 7) {
            UserCoupon coupon = new UserCoupon();
            coupon.setUserId(userId);
            coupon.setCouponType("DISCOUNT_RATE");
            coupon.setCouponName("连续签到第7天奖励");
            coupon.setDiscountRate(0.90);
            coupon.setExpiresAt(LocalDateTime.now().plusDays(7));
            coupon.setSourceType("CHECKIN");
            coupon.setStatus("UNUSED");
            coupon.setQuantity(1);
            userCouponMapper.insert(coupon);
        }
        return status(userId);
    }

    private CheckinStatus buildStatus(User user) {
        CheckinStatus status = new CheckinStatus();
        LocalDate today = LocalDate.now();
        int streak = user.getCheckinStreak() == null ? 0 : user.getCheckinStreak();
        boolean checkedToday = today.equals(user.getLastCheckinDate());
        int nextDay = checkedToday ? Math.min(7, streak + 1) : nextStreakDay(user, today);
        status.setCoins(user.getCoins() == null ? 0 : user.getCoins());
        status.setStreak(streak);
        status.setCheckedToday(checkedToday);
        status.setNextDay(nextDay);
        status.setNextReward(nextDay == 7 ? 0 : REWARDS[nextDay - 1]);
        status.setCouponCount(userCouponMapper.findUnusedByUserId(user.getId()).size());
        return status;
    }

    private int nextStreakDay(User user, LocalDate today) {
        int streak = user.getCheckinStreak() == null ? 0 : user.getCheckinStreak();
        if (user.getLastCheckinDate() != null && user.getLastCheckinDate().plusDays(1).equals(today)) {
            return Math.min(7, streak + 1);
        }
        return 1;
    }
}
