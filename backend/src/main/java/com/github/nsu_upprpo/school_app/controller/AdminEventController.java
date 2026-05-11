package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.model.dto.request.CreateEventRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.EventResponse;
import com.github.nsu_upprpo.school_app.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(@PathVariable UUID id,
                                                @Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.ok(eventService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
