package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Notice;
import com.school.manage.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public Notice create(Notice notice) {
        notice.setCreatedAt(LocalDateTime.now());
        notice.setReadBy(new ArrayList<>());
        log.info("[NoticeService] Creating notice title='{}'", notice.getTitle());
        return noticeRepository.save(notice);
    }

    public Notice update(String id, Notice notice) {
        Notice existing = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + id));
        existing.setTitle(notice.getTitle());
        existing.setContent(notice.getContent());
        existing.setCategory(notice.getCategory());
        existing.setPriority(notice.getPriority());
        existing.setTargetAudience(notice.getTargetAudience());
        existing.setTargetClass(notice.getTargetClass());
        existing.setAttachmentUrl(notice.getAttachmentUrl());
        existing.setAttachmentName(notice.getAttachmentName());
        existing.setPinned(notice.isPinned());
        existing.setPublished(notice.isPublished());
        existing.setExpiryDate(notice.getExpiryDate());
        existing.setUpdatedAt(LocalDateTime.now());
        log.info("[NoticeService] Updated notice id='{}'", id);
        return noticeRepository.save(existing);
    }

    public void delete(String id) {
        noticeRepository.deleteById(id);
        log.info("[NoticeService] Deleted notice id='{}'", id);
    }

    public List<Notice> getAll() {
        return noticeRepository.findByPublishedTrueOrderByPinnedDescCreatedAtDesc();
    }

    public List<Notice> getPublished() {
        List<Notice> notices = noticeRepository.findByPublishedTrueOrderByPinnedDescCreatedAtDesc();
        return filterExpired(notices);
    }

    public Notice getById(String id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + id));
    }

    public List<Notice> getByCategory(String category) {
        return noticeRepository.findByCategory(category);
    }

    public List<Notice> getByAudience(String audience) {
        return noticeRepository.findByTargetAudience(audience);
    }

    public Notice markAsRead(String noticeId, String userId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + noticeId));
        if (notice.getReadBy() == null) {
            notice.setReadBy(new ArrayList<>());
        }
        if (!notice.getReadBy().contains(userId)) {
            notice.getReadBy().add(userId);
            noticeRepository.save(notice);
            log.info("[NoticeService] Notice id='{}' marked as read by userId='{}'", noticeId, userId);
        }
        return notice;
    }

    public Notice togglePin(String id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + id));
        notice.setPinned(!notice.isPinned());
        notice.setUpdatedAt(LocalDateTime.now());
        log.info("[NoticeService] Toggled pin for notice id='{}' to {}", id, notice.isPinned());
        return noticeRepository.save(notice);
    }

    public Notice publish(String id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + id));
        notice.setPublished(true);
        notice.setUpdatedAt(LocalDateTime.now());
        log.info("[NoticeService] Published notice id='{}'", id);
        return noticeRepository.save(notice);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", noticeRepository.count());
        stats.put("publishedCount", noticeRepository.countByPublishedTrue());
        long pinnedCount = noticeRepository.findByPublishedTrueOrderByPinnedDescCreatedAtDesc()
                .stream().filter(Notice::isPinned).count();
        stats.put("pinnedCount", pinnedCount);
        return stats;
    }

    private List<Notice> filterExpired(List<Notice> notices) {
        LocalDate today = LocalDate.now();
        return notices.stream()
                .filter(n -> n.getExpiryDate() == null || !n.getExpiryDate().isBefore(today))
                .collect(Collectors.toList());
    }
}
