package com.github.nsu_upprpo.school_app.model.entity;

public enum ParticipationStatus {
    CANCELLED_BY_PARENT,
    RESCHEDULED_OUT,
    RESCHEDULED_IN,

    PRESENT,
    ABSENT,
    LATE,
    EXCUSED;

    public boolean isParentIntent() {
        return this == CANCELLED_BY_PARENT
                || this == RESCHEDULED_OUT
                || this == RESCHEDULED_IN;
    }

    public boolean isTeacherMark() {
        return this == PRESENT || this == ABSENT || this == LATE || this == EXCUSED;
    }
}
