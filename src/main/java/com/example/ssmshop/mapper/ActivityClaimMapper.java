package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.ActivityClaim;
import org.apache.ibatis.annotations.Param;

public interface ActivityClaimMapper {
    int insert(ActivityClaim claim);

    int existsByCampaignAndUser(@Param("campaignId") Long campaignId, @Param("userId") Long userId);
}
