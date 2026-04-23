package com.github.nsu_upprpo.school_app.repository;

import com.github.nsu_upprpo.school_app.model.entity.ProjectGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectGradeRepository extends JpaRepository<ProjectGrade, UUID> {
    List<ProjectGrade> findByProjectId(UUID projectId);
    List<ProjectGrade> findByChildId(UUID childId);
}

