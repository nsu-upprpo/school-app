
package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.nsu_upprpo.school_app.common.exception.ForbiddenException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreatePaymentRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.RejectPaymentRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.PaymentResponse;
import com.github.nsu_upprpo.school_app.model.entity.Course;
import com.github.nsu_upprpo.school_app.model.entity.Group;
import com.github.nsu_upprpo.school_app.model.entity.Payment;
import com.github.nsu_upprpo.school_app.model.entity.PaymentStatus;
import com.github.nsu_upprpo.school_app.model.entity.User;
import com.github.nsu_upprpo.school_app.model.event.PaymentConfirmedEvent;
import com.github.nsu_upprpo.school_app.model.event.PaymentCreatedEvent;
import com.github.nsu_upprpo.school_app.model.event.PaymentRejectedEvent;
import com.github.nsu_upprpo.school_app.repository.GroupStudentRepository;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import com.github.nsu_upprpo.school_app.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserService userService;
    @Mock
    private GroupService groupService;
    @Mock
    private ParentChildRepository parentChildRepository;
    @Mock
    private GroupStudentRepository groupStudentRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createByAdmin_throwsForbiddenException_whenChildIsNotInGroup() {
        CreatePaymentRequest request = org.mockito.Mockito.mock(CreatePaymentRequest.class);
        when(request.getChildId()).thenReturn(SchoolSeedData.STUDENT1_ID);
        when(request.getGroupId()).thenReturn(SchoolSeedData.GROUP_GRAPHIC_ID);

        when(userService.findById(SchoolSeedData.STUDENT1_ID)).thenReturn(org.mockito.Mockito.mock(User.class));
        when(groupService.findById(SchoolSeedData.GROUP_GRAPHIC_ID)).thenReturn(org.mockito.Mockito.mock(Group.class));
        when(groupStudentRepository.existsByGroupIdAndChildIdAndLeftAtIsNull(
                SchoolSeedData.GROUP_GRAPHIC_ID, SchoolSeedData.STUDENT1_ID)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> paymentService.createByAdmin(request));
    }

    @Test
    void submitByParent_movesPaymentToPendingConfirmation() {
        Payment payment = paymentWithStatus(SchoolSeedData.PAYMENT_ID, SchoolSeedData.STUDENT1_ID, PaymentStatus.UNPAID);
        when(paymentRepository.findById(SchoolSeedData.PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(parentChildRepository.existsByParentIdAndChildId(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID))
                .thenReturn(true);
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse response = paymentService.submitByParent(SchoolSeedData.PARENT_ID, SchoolSeedData.PAYMENT_ID);

        assertEquals(PaymentStatus.PENDING_CONFIRMATION, payment.getStatus());
        assertEquals(PaymentStatus.PENDING_CONFIRMATION, response.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void confirmByAdmin_marksPaymentAsPaid_andPublishesEvent() {
        Payment payment = paymentWithStatus(
                SchoolSeedData.PAYMENT_ID, SchoolSeedData.STUDENT1_ID, PaymentStatus.PENDING_CONFIRMATION);
        User admin = org.mockito.Mockito.mock(User.class);
        when(admin.getId()).thenReturn(SchoolSeedData.ADMIN_ID);
        when(admin.getFullName()).thenReturn("Иван Администратор");

        when(paymentRepository.findById(SchoolSeedData.PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(userService.findById(SchoolSeedData.ADMIN_ID)).thenReturn(admin);
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse response = paymentService.confirmByAdmin(SchoolSeedData.PAYMENT_ID, SchoolSeedData.ADMIN_ID);

        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals(PaymentStatus.PAID, response.getStatus());
        assertEquals(SchoolSeedData.ADMIN_ID, response.getConfirmedById());

        ArgumentCaptor<PaymentConfirmedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentConfirmedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(SchoolSeedData.PAYMENT_ID, eventCaptor.getValue().paymentId());
        assertEquals(SchoolSeedData.STUDENT1_ID, eventCaptor.getValue().childId());
    }

    @Test
    void rejectByAdmin_marksPaymentAsRejected_andPublishesEvent() {
        RejectPaymentRequest request = org.mockito.Mockito.mock(RejectPaymentRequest.class);
        when(request.getReason()).thenReturn("Чек нечитабелен");

        Payment payment = paymentWithStatus(
                SchoolSeedData.PAYMENT_ID, SchoolSeedData.STUDENT1_ID, PaymentStatus.PENDING_CONFIRMATION);
        when(paymentRepository.findById(SchoolSeedData.PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse response = paymentService.rejectByAdmin(SchoolSeedData.PAYMENT_ID, request);

        assertEquals(PaymentStatus.REJECTED, payment.getStatus());
        assertEquals(PaymentStatus.REJECTED, response.getStatus());
        assertEquals("Чек нечитабелен", response.getRejectionReason());

        ArgumentCaptor<PaymentRejectedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentRejectedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(SchoolSeedData.PAYMENT_ID, eventCaptor.getValue().paymentId());
        assertEquals(SchoolSeedData.STUDENT1_ID, eventCaptor.getValue().childId());
        assertEquals("Чек нечитабелен", eventCaptor.getValue().reason());
    }

    @Test
    void createByAdmin_savesPaymentAndPublishesEvent_whenChildIsInGroup() {
        CreatePaymentRequest request = org.mockito.Mockito.mock(CreatePaymentRequest.class);
        when(request.getChildId()).thenReturn(SchoolSeedData.STUDENT1_ID);
        when(request.getGroupId()).thenReturn(SchoolSeedData.GROUP_GRAPHIC_ID);
        when(request.getAmount()).thenReturn(new BigDecimal("4500.00"));
        when(request.getDueDate()).thenReturn(SchoolSeedData.PAYMENT_DUE_DATE);
        when(request.getType()).thenReturn(null);
        when(request.getPeriod()).thenReturn(SchoolSeedData.PAYMENT_PERIOD);
        when(request.getCoversFrom()).thenReturn(SchoolSeedData.PAYMENT_COVERS_FROM);
        when(request.getCoversTo()).thenReturn(SchoolSeedData.PAYMENT_COVERS_TO);
        when(request.getLessonsCount()).thenReturn(null);

        User child = org.mockito.Mockito.mock(User.class);
        when(child.getId()).thenReturn(SchoolSeedData.STUDENT1_ID);
        when(child.getFullName()).thenReturn(SchoolSeedData.STUDENT1_FULL_NAME);

        Course course = org.mockito.Mockito.mock(Course.class);
        when(course.getName()).thenReturn(SchoolSeedData.COURSE_GRAPHIC_NAME);

        Group group = org.mockito.Mockito.mock(Group.class);
        when(group.getId()).thenReturn(SchoolSeedData.GROUP_GRAPHIC_ID);
        when(group.getCourse()).thenReturn(course);

        when(userService.findById(SchoolSeedData.STUDENT1_ID)).thenReturn(child);
        when(groupService.findById(SchoolSeedData.GROUP_GRAPHIC_ID)).thenReturn(group);
        when(groupStudentRepository.existsByGroupIdAndChildIdAndLeftAtIsNull(
                SchoolSeedData.GROUP_GRAPHIC_ID, SchoolSeedData.STUDENT1_ID)).thenReturn(true);

        Payment savedPayment = new Payment();
        savedPayment.setId(SchoolSeedData.PAYMENT_ID);
        savedPayment.setChild(child);
        savedPayment.setGroup(group);
        savedPayment.setAmount(new BigDecimal("4500.00"));
        savedPayment.setStatus(PaymentStatus.UNPAID);
        savedPayment.setDueDate(SchoolSeedData.PAYMENT_DUE_DATE);

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponse response = paymentService.createByAdmin(request);

        assertEquals(SchoolSeedData.PAYMENT_ID, response.getId());
        assertEquals(PaymentStatus.UNPAID, response.getStatus());
        assertEquals(new BigDecimal("4500.00"), response.getAmount());

        ArgumentCaptor<PaymentCreatedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(SchoolSeedData.PAYMENT_ID, eventCaptor.getValue().paymentId());
        assertEquals(SchoolSeedData.STUDENT1_ID, eventCaptor.getValue().childId());
    }

    private Payment paymentWithStatus(UUID paymentId, UUID childId, PaymentStatus status) {
        User child = org.mockito.Mockito.mock(User.class);
        when(child.getId()).thenReturn(childId);
        when(child.getFullName()).thenReturn(SchoolSeedData.STUDENT1_FULL_NAME);

        Course course = org.mockito.Mockito.mock(Course.class);
        when(course.getName()).thenReturn(SchoolSeedData.COURSE_GRAPHIC_NAME);

        Group group = org.mockito.Mockito.mock(Group.class);
        when(group.getId()).thenReturn(SchoolSeedData.GROUP_GRAPHIC_ID);
        when(group.getCourse()).thenReturn(course);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setChild(child);
        payment.setGroup(group);
        payment.setAmount(new BigDecimal("4500.00"));
        payment.setStatus(status);
        return payment;
    }
}
