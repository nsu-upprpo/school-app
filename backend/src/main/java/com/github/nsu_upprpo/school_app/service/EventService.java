package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateEventRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.EventResponse;
import com.github.nsu_upprpo.school_app.model.entity.Branch;
import com.github.nsu_upprpo.school_app.model.entity.Event;
import com.github.nsu_upprpo.school_app.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final BranchService branchService;

    public List<EventResponse> getUpcoming(UUID branchId) {
        List<Event> events;
        if (branchId != null) {
            events = eventRepository.findByBranchIdAndStartTimeAfterOrderByStartTimeAsc(branchId, LocalDateTime.now());
        } else {
            events = eventRepository.findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now());
        }
        return events.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public EventResponse create(CreateEventRequest request) {
        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchService.findById(request.getBranchId());
        }

        Event event = Event.builder()
                .name(request.getName())
                .description(request.getDescription())
                .branch(branch)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .build();
        event = eventRepository.save(event);
        return toResponse(event);
    }

    @Transactional
    public EventResponse update(UUID id, CreateEventRequest request) {
        Event event = findById(id);
        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchService.findById(request.getBranchId());
        }
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setBranch(branch);
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event = eventRepository.save(event);
        return toResponse(event);
    }

    @Transactional
    public void delete(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new NotFoundException("Мероприятие не найдено");
        }
        eventRepository.deleteById(id);
    }

    public Event findById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Мероприятие не найдено"));
    }

    private EventResponse toResponse(Event e) {
        return EventResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .branchId(e.getBranch() != null ? e.getBranch().getId() : null)
                .branchName(e.getBranch() != null ? e.getBranch().getName() : null)
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .location(e.getLocation())
                .build();
    }
}
