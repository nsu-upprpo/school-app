package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.model.dto.request.CreateEventRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.EventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    public List<EventResponse> getUpcoming() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public EventResponse create(CreateEventRequest request, UUID createdBy) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public EventResponse update(UUID id, CreateEventRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
