package com.example.ssmshop.service;

import com.example.ssmshop.domain.ActivityCampaign;
import com.example.ssmshop.domain.ActivityClaim;
import com.example.ssmshop.domain.CouponCode;
import com.example.ssmshop.domain.RewardMail;
import com.example.ssmshop.domain.User;
import com.example.ssmshop.domain.UserCoupon;
import com.example.ssmshop.form.CouponCodeForm;
import com.example.ssmshop.form.RewardCampaignForm;
import com.example.ssmshop.form.RewardMailForm;
import com.example.ssmshop.mapper.ActivityCampaignMapper;
import com.example.ssmshop.mapper.ActivityClaimMapper;
import com.example.ssmshop.mapper.CouponCodeMapper;
import com.example.ssmshop.mapper.RewardMailMapper;
import com.example.ssmshop.mapper.UserCouponMapper;
import com.example.ssmshop.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class RewardCenterService {
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ActivityCampaignMapper activityCampaignMapper;
    private final ActivityClaimMapper activityClaimMapper;
    private final RewardMailMapper rewardMailMapper;
    private final UserMapper userMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponCodeMapper couponCodeMapper;

    public RewardCenterService(ActivityCampaignMapper activityCampaignMapper,
                               ActivityClaimMapper activityClaimMapper,
                               RewardMailMapper rewardMailMapper,
                               UserMapper userMapper,
                               UserCouponMapper userCouponMapper,
                               CouponCodeMapper couponCodeMapper) {
        this.activityCampaignMapper = activityCampaignMapper;
        this.activityClaimMapper = activityClaimMapper;
        this.rewardMailMapper = rewardMailMapper;
        this.userMapper = userMapper;
        this.userCouponMapper = userCouponMapper;
        this.couponCodeMapper = couponCodeMapper;
    }

    public List<ActivityCampaign> userCampaigns(Long userId) {
        return activityCampaignMapper.findForUser(userId);
    }

    public List<ActivityCampaign> adminCampaigns() {
        return activityCampaignMapper.findAll();
    }

    public List<RewardMail> adminMails() {
        return rewardMailMapper.findAll();
    }

    public List<CouponCode> adminCouponCodes() {
        return couponCodeMapper.findAll();
    }

    @Transactional
    public void claimCampaign(Long userId, Long campaignId) {
        ActivityCampaign campaign = activityCampaignMapper.findById(campaignId);
        if (campaign == null) {
            throw new IllegalArgumentException("活动不存在");
        }
        if (!"ACTIVE".equals(campaign.getStatus()) || !campaign.isActiveWindow()) {
            throw new IllegalArgumentException("当前活动暂不可领取");
        }
        if (activityClaimMapper.existsByCampaignAndUser(campaignId, userId) > 0) {
            throw new IllegalArgumentException("你已经领取过这个活动了");
        }
        if (activityCampaignMapper.increaseClaimedCount(campaignId) == 0) {
            throw new IllegalArgumentException("活动名额已领完");
        }
        ActivityClaim claim = new ActivityClaim();
        claim.setCampaignId(campaignId);
        claim.setUserId(userId);
        activityClaimMapper.insert(claim);
        grantReward(userId, campaign.getRewardType(), campaign.getCoinAmount(), campaign.getCouponType(),
                campaign.getCouponName(), campaign.getDiscountRate(), campaign.getThresholdAmount(),
                campaign.getReduceAmount(), campaign.getCouponQuantity(),
                resolveExpiresAt(campaign.getCouponExpiryDays(), campaign.getEndAt()), "ACTIVITY", campaignId);
    }

    public List<UserCoupon> backpack(Long userId) {
        return userCouponMapper.findByUserId(userId);
    }

    @Transactional
    public void deleteInactiveCoupon(Long userId, Long couponId) {
        if (userCouponMapper.deleteInactiveByIdAndUserId(couponId, userId) == 0) {
            throw new IllegalArgumentException("只能删除已使用或已过期的优惠券");
        }
    }

    @Transactional
    public int clearInactiveCoupons(Long userId) {
        return userCouponMapper.deleteInactiveByUserId(userId);
    }

    public List<RewardMail> mailbox(Long userId) {
        return rewardMailMapper.findByUserId(userId);
    }

    public int unreadMailCount(Long userId) {
        return rewardMailMapper.countUnreadByUser(userId);
    }

    @Transactional
    public void markMailViewed(Long userId) {
        rewardMailMapper.markViewedByUser(userId);
    }

    @Transactional
    public void claimMail(Long userId, Long mailId) {
        RewardMail mail = rewardMailMapper.findByIdAndUserId(mailId, userId);
        if (mail == null) {
            throw new IllegalArgumentException("邮件不存在");
        }
        if (mail.isClaimed()) {
            throw new IllegalArgumentException("这封邮件已经领取过了");
        }
        if (mail.isExpired()) {
            throw new IllegalArgumentException("邮件奖励已过期");
        }
        grantRewardFromMail(userId, mail);
        rewardMailMapper.markClaimed(mailId);
    }

    @Transactional
    public int claimAllMails(Long userId) {
        List<RewardMail> mails = rewardMailMapper.findClaimableByUserId(userId);
        int claimed = 0;
        for (RewardMail mail : mails) {
            if (mail.isClaimed() || mail.isExpired()) {
                continue;
            }
            grantRewardFromMail(userId, mail);
            if (rewardMailMapper.markClaimed(mail.getId()) > 0) {
                claimed++;
            }
        }
        return claimed;
    }

    private void grantRewardFromMail(Long userId, RewardMail mail) {
        grantReward(userId, mail.getRewardType(), mail.getCoinAmount(), mail.getCouponType(), mail.getCouponName(),
                mail.getDiscountRate(), mail.getThresholdAmount(), mail.getReduceAmount(), mail.getCouponQuantity(), mail.getExpiresAt(),
                "MAIL", mail.getId());
    }

    @Transactional
    public ActivityCampaign createCampaign(User admin, RewardCampaignForm form) {
        validateRewardForm(form.getRewardType(), form.getCoinAmount(), form.getCouponType(), form.getDiscountRate(),
                form.getThresholdAmount(), form.getReduceAmount());
        ActivityCampaign campaign = new ActivityCampaign();
        campaign.setTitle(form.getTitle());
        campaign.setDescription(form.getDescription());
        campaign.setRewardType(form.getRewardType());
        campaign.setCoinAmount("COIN".equals(form.getRewardType()) ? form.getCoinAmount() : null);
        campaign.setCouponType("COUPON".equals(form.getRewardType()) ? form.getCouponType() : null);
        campaign.setDiscountRate("COUPON".equals(form.getRewardType()) && "DISCOUNT_RATE".equals(form.getCouponType())
                ? form.getDiscountRate() : null);
        campaign.setThresholdAmount("COUPON".equals(form.getRewardType()) && "AMOUNT_OFF".equals(form.getCouponType())
                ? form.getThresholdAmount() : null);
        campaign.setReduceAmount("COUPON".equals(form.getRewardType()) && "AMOUNT_OFF".equals(form.getCouponType())
                ? form.getReduceAmount() : null);
        campaign.setStartAt(form.getStartAt());
        campaign.setEndAt(form.getEndAt());
        campaign.setQuotaLimit(form.getQuotaLimit() == null ? 0 : form.getQuotaLimit());
        campaign.setCouponQuantity(resolveCouponQuantity(form.getCouponQuantity()));
        campaign.setCouponExpiryDays(form.getExpiryDays());
        campaign.setClaimedCount(0);
        campaign.setStatus("ACTIVE");
        campaign.setCouponName(form.getTitle());
        campaign.setFlashSale(form.isFlashSale());
        activityCampaignMapper.insert(campaign);
        return campaign;
    }

    @Transactional
    public void updateCampaignStatus(Long id, String status) {
        if (!"ACTIVE".equals(status) && !"PAUSED".equals(status) && !"ARCHIVED".equals(status)) {
            throw new IllegalArgumentException("非法的活动状态");
        }
        if (activityCampaignMapper.updateStatus(id, status) == 0) {
            throw new IllegalArgumentException("活动不存在");
        }
    }

    @Transactional
    public void deleteCampaign(Long id) {
        activityCampaignMapper.delete(id);
    }

    @Transactional
    public ActivityCampaign cloneCampaign(Long sourceId) {
        ActivityCampaign source = activityCampaignMapper.findById(sourceId);
        if (source == null) {
            throw new IllegalArgumentException("源活动不存在");
        }
        ActivityCampaign clone = new ActivityCampaign();
        clone.setTitle(source.getTitle() + "（副本）");
        clone.setDescription(source.getDescription());
        clone.setRewardType(source.getRewardType());
        clone.setCoinAmount(source.getCoinAmount());
        clone.setCouponType(source.getCouponType());
        clone.setCouponName(source.getCouponName());
        clone.setDiscountRate(source.getDiscountRate());
        clone.setThresholdAmount(source.getThresholdAmount());
        clone.setReduceAmount(source.getReduceAmount());
        clone.setStartAt(source.getStartAt());
        clone.setEndAt(source.getEndAt());
        clone.setQuotaLimit(source.getQuotaLimit());
        clone.setCouponQuantity(resolveCouponQuantity(source.getCouponQuantity()));
        clone.setCouponExpiryDays(source.getCouponExpiryDays());
        clone.setClaimedCount(0);
        clone.setStatus("PAUSED");
        clone.setFlashSale(source.getFlashSale());
        activityCampaignMapper.insert(clone);
        return clone;
    }

    @Transactional
    public RewardMail createMail(User sender, RewardMailForm form) {
        validateRewardForm(form.getRewardType(), form.getCoinAmount(), form.getCouponType(), form.getDiscountRate(),
                form.getThresholdAmount(), form.getReduceAmount());
        if (form.getUsername() == null || form.getUsername().isBlank()) {
            throw new IllegalArgumentException("请输入收件人用户名");
        }
        User target = userMapper.findByUsername(form.getUsername());
        if (target == null) {
            throw new IllegalArgumentException("收件人不存在");
        }
        return insertMail(sender, target.getId(), form);
    }

    @Transactional
    public int broadcastMail(User sender, RewardMailForm form) {
        validateRewardForm(form.getRewardType(), form.getCoinAmount(), form.getCouponType(), form.getDiscountRate(),
                form.getThresholdAmount(), form.getReduceAmount());
        List<User> users = userMapper.findAll();
        int count = 0;
        for (User user : users) {
            if ("CUSTOMER".equals(user.getRole()) && "ACTIVE".equals(user.getStatus())) {
                insertMail(sender, user.getId(), form);
                count++;
            }
        }
        return count;
    }

    private RewardMail insertMail(User sender, Long userId, RewardMailForm form) {
        RewardMail mail = new RewardMail();
        mail.setUserId(userId);
        mail.setSenderId(sender == null ? null : sender.getId());
        mail.setSenderName(sender == null ? "系统" : sender.getNickname());
        mail.setTitle(form.getTitle());
        mail.setMessage(form.getMessage());
        mail.setRewardType(form.getRewardType());
        mail.setCoinAmount("COIN".equals(form.getRewardType()) ? form.getCoinAmount() : null);
        mail.setCouponType("COUPON".equals(form.getRewardType()) ? form.getCouponType() : null);
        mail.setCouponName(form.getTitle());
        mail.setDiscountRate("COUPON".equals(form.getRewardType()) && "DISCOUNT_RATE".equals(form.getCouponType())
                ? form.getDiscountRate() : null);
        mail.setThresholdAmount("COUPON".equals(form.getRewardType()) && "AMOUNT_OFF".equals(form.getCouponType())
                ? form.getThresholdAmount() : null);
        mail.setReduceAmount("COUPON".equals(form.getRewardType()) && "AMOUNT_OFF".equals(form.getCouponType())
                ? form.getReduceAmount() : null);
        mail.setCouponQuantity(resolveCouponQuantity(form.getCouponQuantity()));
        mail.setExpiresAt(resolveExpiresAt(form.getExpiresAt(), form.getExpiryDays()));
        mail.setStatus("UNREAD");
        rewardMailMapper.insert(mail);
        return mail;
    }

    @Transactional
    public CouponCode createCouponCode(User admin, CouponCodeForm form) {
        validateRewardForm(form.getRewardType(), form.getCoinAmount(), form.getCouponType(), form.getDiscountRate(),
                form.getThresholdAmount(), form.getReduceAmount());
        String code = form.getCode();
        if (code == null || code.isBlank()) {
            code = generateCode();
        } else {
            code = code.trim().toUpperCase(Locale.ROOT);
            if (couponCodeMapper.findByCode(code) != null) {
                throw new IllegalArgumentException("兑换码已存在，请换一个");
            }
        }
        CouponCode cc = new CouponCode();
        cc.setCode(code);
        cc.setTitle(form.getTitle());
        cc.setRewardType(form.getRewardType());
        cc.setCoinAmount("COIN".equals(form.getRewardType()) ? form.getCoinAmount() : null);
        cc.setCouponType("COUPON".equals(form.getRewardType()) ? form.getCouponType() : null);
        cc.setCouponName(form.getTitle());
        cc.setDiscountRate("COUPON".equals(form.getRewardType()) && "DISCOUNT_RATE".equals(form.getCouponType())
                ? form.getDiscountRate() : null);
        cc.setThresholdAmount("COUPON".equals(form.getRewardType()) && "AMOUNT_OFF".equals(form.getCouponType())
                ? form.getThresholdAmount() : null);
        cc.setReduceAmount("COUPON".equals(form.getRewardType()) && "AMOUNT_OFF".equals(form.getCouponType())
                ? form.getReduceAmount() : null);
        cc.setCouponExpiryDays(form.getExpiryDays());
        cc.setCouponQuantity(resolveCouponQuantity(form.getCouponQuantity()));
        cc.setExpiresAt(form.getExpiresAt());
        cc.setTotalQuota(form.getTotalQuota() == null ? 0 : form.getTotalQuota());
        cc.setRedeemedCount(0);
        cc.setStatus("ACTIVE");
        cc.setCreatedBy(admin == null ? null : admin.getId());
        couponCodeMapper.insert(cc);
        return cc;
    }

    @Transactional
    public CouponCode redeemCouponCode(Long userId, String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new IllegalArgumentException("请输入兑换码");
        }
        String code = rawCode.trim().toUpperCase(Locale.ROOT);
        CouponCode cc = couponCodeMapper.findByCode(code);
        if (cc == null) {
            throw new IllegalArgumentException("兑换码无效");
        }
        if (!"ACTIVE".equals(cc.getStatus())) {
            throw new IllegalArgumentException("兑换码已停用");
        }
        if (cc.isExpired()) {
            throw new IllegalArgumentException("兑换码已过期");
        }
        if (couponCodeMapper.existsRedemption(cc.getId(), userId) > 0) {
            throw new IllegalArgumentException("你已经兑换过这个码了");
        }
        if (couponCodeMapper.increaseRedeemedCount(cc.getId()) == 0) {
            throw new IllegalArgumentException("兑换名额已用完");
        }
        couponCodeMapper.insertRedemption(cc.getId(), userId);
        LocalDateTime expiresAt = cc.getExpiresAt();
        if (expiresAt == null && cc.getCouponExpiryDays() != null && cc.getCouponExpiryDays() > 0) {
            expiresAt = LocalDateTime.now().plusDays(cc.getCouponExpiryDays());
        }
        grantReward(userId, cc.getRewardType(), cc.getCoinAmount(), cc.getCouponType(), cc.getCouponName(),
                cc.getDiscountRate(), cc.getThresholdAmount(), cc.getReduceAmount(), cc.getCouponQuantity(), expiresAt,
                "CDKEY", cc.getId());
        return cc;
    }

    @Transactional
    public void updateCouponCodeStatus(Long id, String status) {
        if (!"ACTIVE".equals(status) && !"PAUSED".equals(status)) {
            throw new IllegalArgumentException("非法的兑换码状态");
        }
        couponCodeMapper.updateStatus(id, status);
    }

    @Transactional
    public void deleteCouponCode(Long id) {
        couponCodeMapper.delete(id);
    }

    @Transactional
    public void grantWelcomeCoupon(Long userId) {
        UserCoupon coupon = new UserCoupon();
        coupon.setUserId(userId);
        coupon.setCouponType("AMOUNT_OFF");
        coupon.setCouponName("新人首单券·满100减20");
        coupon.setThresholdAmount(new BigDecimal("100.00"));
        coupon.setReduceAmount(new BigDecimal("20.00"));
        coupon.setExpiresAt(LocalDateTime.now().plusDays(30));
        coupon.setSourceType("WELCOME");
        coupon.setSourceRefId(null);
        coupon.setStatus("UNUSED");
        coupon.setQuantity(1);
        userCouponMapper.insert(coupon);
    }

    private void grantReward(Long userId, String rewardType, Integer coinAmount, String couponType, String couponName,
                             Double discountRate, BigDecimal thresholdAmount, BigDecimal reduceAmount, Integer couponQuantity,
                             LocalDateTime expiresAt, String sourceType, Long sourceRefId) {
        if (!"COIN".equals(rewardType) && !"COUPON".equals(rewardType)) {
            throw new IllegalArgumentException("未知奖励类型");
        }
        if ("COIN".equals(rewardType)) {
            int coins = coinAmount == null ? 0 : coinAmount;
            if (coins > 0) {
                userMapper.increaseCoins(userId, coins);
            }
            return;
        }
        UserCoupon coupon = new UserCoupon();
        coupon.setUserId(userId);
        coupon.setCouponType(couponType == null ? "DISCOUNT_RATE" : couponType);
        coupon.setCouponName(couponName);
        coupon.setDiscountRate(discountRate);
        coupon.setThresholdAmount(thresholdAmount);
        coupon.setReduceAmount(reduceAmount);
        coupon.setExpiresAt(expiresAt);
        coupon.setSourceType(sourceType);
        coupon.setSourceRefId(sourceRefId);
        coupon.setStatus("UNUSED");
        coupon.setQuantity(resolveCouponQuantity(couponQuantity));
        userCouponMapper.insert(coupon);
    }

    private int resolveCouponQuantity(Integer couponQuantity) {
        return couponQuantity == null || couponQuantity < 1 ? 1 : couponQuantity;
    }

    private void validateRewardForm(String rewardType, Integer coinAmount, String couponType, Double discountRate,
                                    BigDecimal thresholdAmount, BigDecimal reduceAmount) {
        if ("COIN".equals(rewardType)) {
            if (coinAmount == null || coinAmount <= 0) {
                throw new IllegalArgumentException("金币数量必须大于 0");
            }
            return;
        }
        if (!"COUPON".equals(rewardType)) {
            throw new IllegalArgumentException("请选择正确的奖励类型");
        }
        if ("AMOUNT_OFF".equals(couponType)) {
            if (thresholdAmount == null || reduceAmount == null
                    || thresholdAmount.compareTo(BigDecimal.ZERO) <= 0
                    || reduceAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("满减券需要填写有效的门槛金额和减免金额");
            }
            if (reduceAmount.compareTo(thresholdAmount) > 0) {
                throw new IllegalArgumentException("减免金额不能大于门槛金额");
            }
            return;
        }
        if (discountRate == null || discountRate <= 0 || discountRate >= 1) {
            throw new IllegalArgumentException("折扣券需要填写 0~1 之间的折扣，例如 0.9 表示九折");
        }
    }

    private LocalDateTime resolveExpiresAt(LocalDateTime expiresAt, Integer expiryDays) {
        if (expiresAt != null) {
            return expiresAt;
        }
        if (expiryDays != null && expiryDays > 0) {
            return LocalDateTime.now().plusDays(expiryDays);
        }
        return null;
    }

    private LocalDateTime resolveExpiresAt(Integer expiryDays, LocalDateTime fallback) {
        if (expiryDays != null && expiryDays > 0) {
            return LocalDateTime.now().plusDays(expiryDays);
        }
        return fallback;
    }

    private String generateCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 12; i++) {
                sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
                if (i == 3 || i == 7) {
                    sb.append('-');
                }
            }
            String candidate = sb.toString();
            if (couponCodeMapper.findByCode(candidate) == null) {
                return candidate;
            }
        }
        throw new IllegalStateException("无法生成唯一兑换码");
    }
}
