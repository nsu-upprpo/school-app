package com.github.nsu_upprpo.school_app.repository;

import com.github.nsu_upprpo.school_app.model.entity.GroupStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface GroupStudentRepository extends JpaRepository<GroupStudent, GroupStudent.GroupStudentId> {
    List<GroupStudent> findByGroupId(UUID groupId);
    List<GroupStudent> findByChildId(UUID childId);
    List<GroupStudent> findByGroupIdAndLeftAtIsNull(UUID groupId);
    boolean existsByGroupIdAndChildIdAndLeftAtIsNull(UUID groupId, UUID childId);
    long countByGroupIdAndLeftAtIsNull(UUID groupId);
}
