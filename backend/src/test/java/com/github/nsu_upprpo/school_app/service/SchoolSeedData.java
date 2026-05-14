
package com.github.nsu_upprpo.school_app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

final class SchoolSeedData {

    static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final UUID MANAGER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    static final UUID TEACHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    static final UUID PARENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    static final UUID STUDENT1_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    static final UUID STUDENT2_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");

    static final UUID BRANCH_CENTRAL_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    static final UUID BRANCH_LEFT_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");

    static final UUID COURSE_GRAPHIC_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    static final UUID COURSE_ARCH_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");

    static final UUID GROUP_GRAPHIC_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    static final UUID GROUP_ARCH_ID = UUID.fromString("30000000-0000-0000-0000-000000000002");

    static final UUID EVENT_OPEN_DAY_ID = UUID.fromString("50000000-0000-0000-0000-000000000001");
    static final UUID PROJECT_POSTER_ID = UUID.fromString("60000000-0000-0000-0000-000000000001");
    static final UUID LESSON_INTRO_ID = UUID.fromString("70000000-0000-0000-0000-000000000001");
    static final UUID LESSON_COLOR_ID = UUID.fromString("70000000-0000-0000-0000-000000000002");
    static final UUID GRADE_ID = UUID.fromString("80000000-0000-0000-0000-000000000001");
    static final UUID ATTENDANCE_PRESENT_ID = UUID.fromString("90000000-0000-0000-0000-000000000001");
    static final UUID ATTENDANCE_ABSENT_ID = UUID.fromString("90000000-0000-0000-0000-000000000002");
    static final UUID PAYMENT_ID = UUID.fromString("a0000000-0000-0000-0000-000000000001");
    static final UUID NOTIFICATION_ID = UUID.fromString("b0000000-0000-0000-0000-000000000001");

    static final String ADMIN_EMAIL = "admin@school.ru";
    static final String PARENT_EMAIL = "parent@test.com";
    static final String TEACHER_EMAIL = "teacher@school.ru";
    static final String STUDENT1_EMAIL = "student1@school.ru";

    static final String BRANCH_CENTRAL_NAME = "Центральный";
    static final String BRANCH_LEFT_NAME = "Левобережный";
    static final String CITY_NSK = "Новосибирск";

    static final String COURSE_GRAPHIC_NAME = "Графический дизайн";
    static final String COURSE_ARCH_NAME = "Архитектурный дизайн";

    static final String TEACHER_FULL_NAME = "Павел Преподаватель";
    static final String STUDENT1_FULL_NAME = "Маша Ученик";
    static final String STUDENT2_FULL_NAME = "Петя Ученик";

    static final String PAYMENT_PERIOD = "2025-09";

    static final LocalDate PAYMENT_DUE_DATE = LocalDate.of(2025, 9, 10);
    static final LocalDate PAYMENT_COVERS_FROM = LocalDate.of(2025, 9, 1);
    static final LocalDate PAYMENT_COVERS_TO = LocalDate.of(2025, 9, 30);

    static final LocalDateTime EVENT_START = LocalDateTime.of(2025, 10, 1, 12, 0);
    static final LocalDateTime EVENT_END = LocalDateTime.of(2025, 10, 1, 14, 0);

    static final LocalDateTime LESSON1_START = LocalDateTime.of(2025, 9, 2, 10, 0);
    static final LocalDateTime LESSON1_END = LocalDateTime.of(2025, 9, 2, 11, 30);
    static final LocalDateTime LESSON2_START = LocalDateTime.of(2025, 9, 4, 10, 0);
    static final LocalDateTime LESSON2_END = LocalDateTime.of(2025, 9, 4, 11, 30);

    private SchoolSeedData() {
    }
}
