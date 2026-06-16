package com.example.ssmshop.service;

import com.example.ssmshop.domain.Announcement;
import com.example.ssmshop.domain.User;
import com.example.ssmshop.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService {
    private final AnnouncementMapper announcementMapper;

    public AnnouncementService(AnnouncementMapper announcementMapper) {
        this.announcementMapper = announcementMapper;
    }

    public List<Announcement> visibleAnnouncements() {
        return announcementMapper.findVisible();
    }

    public List<Announcement> allAnnouncements() {
        return announcementMapper.findAll();
    }

    public Announcement findById(Long id) {
        return announcementMapper.findById(id);
    }

    public Announcement findVisibleById(Long id) {
        Announcement announcement = announcementMapper.findById(id);
        return announcement != null && announcement.isVisible() ? announcement : null;
    }

    public Announcement featured() {
        return announcementMapper.findFeatured();
    }

    @Transactional
    public Announcement save(User admin, Announcement announcement) {
        validate(announcement);
        normalizeForSave(announcement);
        if (announcement.getId() == null) {
            announcement.setCreatedBy(admin == null ? null : admin.getId());
            announcement.setCreatedAt(LocalDateTime.now());
        }
        announcement.setUpdatedAt(LocalDateTime.now());
        if (announcement.getId() == null) {
            announcementMapper.insert(announcement);
        } else {
            announcementMapper.update(announcement);
        }
        return announcement;
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        if (!"DRAFT".equals(status) && !"PUBLISHED".equals(status) && !"ARCHIVED".equals(status)) {
            throw new IllegalArgumentException("非法公告状态");
        }
        if (announcementMapper.updateStatus(id, status) == 0) {
            throw new IllegalArgumentException("公告不存在");
        }
    }

    @Transactional
    public void togglePinned(Long id) {
        Announcement announcement = announcementMapper.findById(id);
        if (announcement == null) {
            throw new IllegalArgumentException("公告不存在");
        }
        announcementMapper.updatePinned(id, !announcement.pinnedEnabled());
    }

    private void validate(Announcement announcement) {
        if (announcement.getTitle() == null || announcement.getTitle().isBlank()) {
            throw new IllegalArgumentException("公告标题不能为空");
        }
        if (announcement.getContent() == null || announcement.getContent().isBlank()) {
            throw new IllegalArgumentException("公告内容不能为空");
        }
    }

    private void normalizeForSave(Announcement announcement) {
        if (announcement.getPinned() == null) {
            announcement.setPinned(false);
        }
        String status = announcement.getStatus();
        if (status == null || status.isBlank()) {
            status = "PUBLISHED";
        }
        if (!"DRAFT".equals(status) && !"PUBLISHED".equals(status) && !"ARCHIVED".equals(status)) {
            throw new IllegalArgumentException("请选择草稿、发布或归档状态");
        }
        announcement.setStatus(status);
        if (announcement.isPublished() && (announcement.getPublishedAt() == null
                || announcement.getPublishedAt().isAfter(LocalDateTime.now()))) {
            announcement.setPublishedAt(LocalDateTime.now());
        }
    }
}
