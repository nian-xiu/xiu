package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.Announcement;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AnnouncementMapper {
    int insert(Announcement announcement);

    int update(Announcement announcement);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updatePinned(@Param("id") Long id, @Param("pinned") boolean pinned);

    Announcement findById(Long id);

    List<Announcement> findVisible();

    List<Announcement> findAll();

    Announcement findFeatured();
}
