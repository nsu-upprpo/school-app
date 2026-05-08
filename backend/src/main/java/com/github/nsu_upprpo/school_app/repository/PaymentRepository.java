package com.github.nsu_upprpo.school_app.repository;

import com.github.nsu_upprpo.school_app.model.entity.Payment;
import com.github.nsu_upprpo.school_app.model.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByChildIdOrderByCreatedAtDesc(UUID childId);

    List<Payment> findByChildIdAndStatusOrderByCreatedAtDesc(UUID childId, PaymentStatus status);

    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    List<Payment> findByStatusAndDueDateOrderByCreatedAtDesc(PaymentStatus status, LocalDate dueDate);

    List<Payment> findByStatusAndDueDateBeforeOrderByCreatedAtDesc(PaymentStatus status, LocalDate date);

    List<Payment> findByGroupIdOrderByCreatedAtDesc(UUID groupId);

    List<Payment> findByGroupIdAndStatusOrderByCreatedAtDesc(UUID groupId, PaymentStatus status);
}