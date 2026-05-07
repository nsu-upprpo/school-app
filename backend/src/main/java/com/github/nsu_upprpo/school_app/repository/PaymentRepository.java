package com.github.nsu_upprpo.school_app.repository;

import com.github.nsu_upprpo.school_app.model.entity.Payment;
import com.github.nsu_upprpo.school_app.model.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByChildIdOrderByCreatedAtDesc(UUID childId);

    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    List<Payment> findByChildIdAndGroupIdOrderByCreatedAtDesc(UUID childId, UUID groupId);
}
