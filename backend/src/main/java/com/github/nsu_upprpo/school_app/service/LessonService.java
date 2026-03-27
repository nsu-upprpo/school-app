package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.model.dto.request.CreateLessonRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.LessonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonService {

    public List<LessonResponse> getScheduleByChild(UUID childId, LocalDateTime from, LocalDateTime to) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<LessonResponse> getScheduleByTeacher(UUID teacherId, LocalDateTime from, LocalDateTime to) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public LessonResponse create(CreateLessonRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void cancel(UUID id, String reason) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
