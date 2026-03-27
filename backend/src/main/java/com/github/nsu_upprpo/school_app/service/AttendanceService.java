package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.model.dto.request.MarkAttendanceRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.AttendanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    public List<AttendanceResponse> getByLesson(UUID lessonId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void markAttendance(UUID lessonId, List<MarkAttendanceRequest> requests, UUID markedBy) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
