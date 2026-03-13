package com.github.nsu_upprpo.school_app.repository;

import com.github.nsu_upprpo.school_app.model.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {
    List<Branch> findByActiveTrue();
    List<Branch> findByCityAndActiveTrue(String city);
}
