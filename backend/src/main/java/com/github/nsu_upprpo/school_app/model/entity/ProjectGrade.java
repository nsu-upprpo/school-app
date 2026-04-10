package com.github.nsu_upprpo.school_app.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_grades")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectGrade extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    private Integer score;

    @Column(length = 255)
    private String comment;
}
