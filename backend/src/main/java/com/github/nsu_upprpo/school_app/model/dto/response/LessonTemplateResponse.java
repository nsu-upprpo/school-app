package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class LessonTemplateResponse {
    private final List<GroupTemplate> groups;

    @Getter
    @Builder
    public static class GroupTemplate {
        private final UUID groupId;
        private final String courseName;
        private final String scheduleDescription;
        private final List<RecentLessonTime> recentLessonTimes;
        private final List<ProjectOption> projects;
        private final List<String> recentTopics;
    }

    @Getter
    @Builder
    public static class RecentLessonTime {
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
    }

    @Getter
    @Builder
    public static class ProjectOption {
        private final UUID projectId;
        private final String name;
        private final String status;
    }
}
