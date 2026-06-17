package com.school.manage.service;

import com.school.manage.model.Event;
import com.school.manage.model.Notification;
import com.school.manage.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    /**
     * Creates a new event. Automatically creates a notification for non-holiday events.
     */
    public Event createEvent(Event event) {
        event.setCreatedAt(LocalDateTime.now());
        event.setActive(true);
        Event saved = eventRepository.save(event);

        // Auto-create notification for non-holiday events
        if (!event.isHoliday()) {
            Notification notification = new Notification();
            notification.setTitle(event.getTitle());
            notification.setMessage(event.getDescription());
            notification.setType("EVENT");
            notification.setTargetAudience(event.getTargetAudience() != null ? event.getTargetAudience() : "ALL");
            notification.setTargetClass(event.getTargetClass());
            notification.setPriority("MEDIUM");
            notification.setCreatedBy(event.getCreatedBy());
            notificationService.createNotification(notification);
        }
        return saved;
    }

    /**
     * Returns all active events ordered by start date descending.
     */
    public List<Event> getAllEvents() {
        return eventRepository.findByActiveTrueOrderByStartDateDesc();
    }

    /**
     * Returns a single event by ID.
     */
    public Event getEventById(String id) {
        return eventRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Event not found: " + id));
    }

    /**
     * Returns events within a date range.
     */
    public List<Event> getEventsByDateRange(LocalDate from, LocalDate to) {
        return eventRepository.findByStartDateBetweenAndActiveTrue(from, to);
    }

    /**
     * Returns upcoming events (start date >= today).
     */
    public List<Event> getUpcomingEvents() {
        return eventRepository.findByStartDateGreaterThanEqualAndActiveTrueOrderByStartDateAsc(LocalDate.now());
    }

    /**
     * Returns all active holidays.
     */
    public List<Event> getHolidays() {
        return eventRepository.findByIsHolidayTrueAndActiveTrueOrderByStartDateAsc();
    }

    /**
     * Returns events filtered by category.
     */
    public List<Event> getEventsByCategory(String category) {
        return eventRepository.findByCategoryAndActiveTrue(category);
    }

    /**
     * Updates an existing event. Preserves createdAt and createdBy.
     */
    public Event updateEvent(String id, Event event) {
        Event existing = getEventById(id);
        event.setId(id);
        event.setCreatedAt(existing.getCreatedAt());
        event.setCreatedBy(existing.getCreatedBy());
        event.setUpdatedAt(LocalDateTime.now());
        event.setActive(true);
        return eventRepository.save(event);
    }

    /**
     * Soft-deletes an event by setting active = false.
     */
    public void deleteEvent(String id) {
        Event event = getEventById(id);
        event.setActive(false);
        eventRepository.save(event);
    }
}
