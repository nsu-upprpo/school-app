package com.github.nsu_upprpo.school_app.repository;

import com.github.nsu_upprpo.school_app.model.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findByActiveTrue();
    List<Group> findByBranchIdAndActiveTrue(UUID branchId);
    List<Group> findByTeacherIdAndActiveTrue(UUID teacherId);
}

