package com.github.nsu_upprpo.school_app.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "group_students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(GroupStudent.GroupStudentId.class)
public class GroupStudent {

    @Id
    @Column(name = "group_id")
    private UUID groupId;

    @Id
    @Column(name = "child_id")
    private UUID childId;

    @Column(name = "enrolled_at")
    private LocalDate enrolledAt;

    @Column(name = "left_at")
    private LocalDate leftAt;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class GroupStudentId implements Serializable {
        private UUID groupId;
        private UUID childId;
    }
}
