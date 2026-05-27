package com.github.nsu_upprpo.school_app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupStudentId implements Serializable {
        private UUID groupId;
        private UUID childId;
    }
}
