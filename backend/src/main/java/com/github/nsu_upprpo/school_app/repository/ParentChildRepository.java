package com.github.nsu_upprpo.school_app.repository;

import com.github.nsu_upprpo.school_app.model.entity.ParentChild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParentChildRepository extends JpaRepository<ParentChild, ParentChild.ParentChildId> {
    List<ParentChild> findByParentId(UUID parentId);
    List<ParentChild> findByChildId(UUID childId);
    boolean existsByParentIdAndChildId(UUID parentId, UUID childId);
}
