package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.ActivityCampaign;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ActivityCampaignMapper {
    int insert(ActivityCampaign campaign);

    ActivityCampaign findById(@Param("id") Long id);

    List<ActivityCampaign> findAll();

    List<ActivityCampaign> findForUser(@Param("userId") Long userId);

    int increaseClaimedCount(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int delete(@Param("id") Long id);
}
