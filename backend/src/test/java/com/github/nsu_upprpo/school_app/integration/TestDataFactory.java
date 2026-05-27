package com.github.nsu_upprpo.school_app.integration;

import com.github.nsu_upprpo.school_app.model.entity.Branch;
import com.github.nsu_upprpo.school_app.model.entity.Course;
import com.github.nsu_upprpo.school_app.model.entity.Group;
import com.github.nsu_upprpo.school_app.model.entity.GroupStudent;
import com.github.nsu_upprpo.school_app.model.entity.Notification;
import com.github.nsu_upprpo.school_app.model.entity.NotificationType;
import com.github.nsu_upprpo.school_app.model.entity.ParentChild;
import com.github.nsu_upprpo.school_app.model.entity.Payment;
import com.github.nsu_upprpo.school_app.model.entity.PaymentStatus;
import com.github.nsu_upprpo.school_app.model.entity.Role;
import com.github.nsu_upprpo.school_app.model.entity.User;
import com.github.nsu_upprpo.school_app.repository.BranchRepository;
import com.github.nsu_upprpo.school_app.repository.CourseRepository;
import com.github.nsu_upprpo.school_app.repository.GroupRepository;
import com.github.nsu_upprpo.school_app.repository.GroupStudentRepository;
import com.github.nsu_upprpo.school_app.repository.NotificationRepository;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import com.github.nsu_upprpo.school_app.repository.PaymentRepository;
import com.github.nsu_upprpo.school_app.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestDataFactory {

    private final UserRepository userRepository;
    private final ParentChildRepository parentChildRepository;
    private final BranchRepository branchRepository;
    private final CourseRepository courseRepository;
    private final GroupRepository groupRepository;
    private final GroupStudentRepository groupStudentRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataFactory(
            UserRepository userRepository,
            ParentChildRepository parentChildRepository,
            BranchRepository branchRepository,
            CourseRepository courseRepository,
            GroupRepository groupRepository,
            GroupStudentRepository groupStudentRepository,
            PaymentRepository paymentRepository,
            NotificationRepository notificationRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.parentChildRepository = parentChildRepository;
        this.branchRepository = branchRepository;
        this.courseRepository = courseRepository;
        this.groupRepository = groupRepository;
        this.groupStudentRepository = groupStudentRepository;
        this.paymentRepository = paymentRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createAdmin(String email) {
        return createUser("Иван", "Администратор", email, Role.ADMIN);
    }

    public User createTeacher(String email) {
        return createUser("Павел", "Преподаватель", email, Role.TEACHER);
    }

    public User createParent(String email) {
        return createUser("Ирина", "Родитель", email, Role.PARENT);
    }

    public User createChild(String email) {
        User user = new User();
        user.setFirstName("Маша");
        user.setLastName("Ученик");
        user.setBirthDate(LocalDate.of(2015, 6, 15));
        user.setEmail(email);
        user.setPhone("+7999" + ThreadLocalRandom.current().nextInt(100000, 1_000_000));
        user.setPasswordHash(passwordEncoder.encode(BaseIntegrationTest.TEST_PASSWORD));
        user.setRole(Role.STUDENT);
        user.setActive(true);
        return userRepository.save(user);
    }

    public ParentChild linkParentAndChild(User parent, User child) {
        ParentChild link = new ParentChild(parent.getId(), child.getId());
        return parentChildRepository.save(link);
    }

    public Branch createBranch(String suffix) {
        Branch branch = new Branch();
        branch.setName("Центральный " + suffix);
        branch.setCity("Новосибирск");
        branch.setAddress("ул. Ленина, " + suffix);
        branch.setPhone("+7383" + suffix.substring(Math.max(0, suffix.length() - 7)));
        branch.setActive(true);
        return branchRepository.save(branch);
    }

    public Course createCourse(String suffix) {
        Course course = new Course();
        course.setName("Графический дизайн " + suffix);
        course.setDescription("Основы графики, композиции и цвета");
        course.setMinAge(7);
        course.setMaxAge(14);
        course.setActive(true);
        return courseRepository.save(course);
    }

    public Group createGroup(User teacher, Branch branch, Course course, String suffix) {
        Group group = new Group();
        group.setTeacher(teacher);
        group.setBranch(branch);
        group.setCourse(course);
        group.setScheduleDescription("ПН, СР 10:00-11:30 " + suffix);
        group.setMaxStudents(12);
        group.setActive(true);
        return groupRepository.save(group);
    }

    public GroupStudent enroll(Group group, User child) {
        GroupStudent gs = new GroupStudent();
        gs.setGroupId(group.getId());
        gs.setChildId(child.getId());
        gs.setEnrolledAt(LocalDate.now());
        return groupStudentRepository.save(gs);
    }

    public Payment createUnpaidPayment(User child, Group group, LocalDate dueDate) {
        Payment payment = new Payment();
        payment.setChild(child);
        payment.setGroup(group);
        payment.setAmount(new BigDecimal("5500.00"));
        payment.setStatus(PaymentStatus.UNPAID);
        payment.setDueDate(dueDate);
        payment.setCoversFrom(dueDate.withDayOfMonth(1));
        payment.setCoversTo(dueDate.withDayOfMonth(dueDate.lengthOfMonth()));
        return paymentRepository.save(payment);
    }

    public Notification createUnreadNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.PAYMENT);
        notification.setMessageText(message);
        notification.setReferenceType("TEST");
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    private User createUser(String firstName, String lastName, String email, Role role) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone("+7999" + ThreadLocalRandom.current().nextInt(100000, 1_000_000));
        user.setPasswordHash(passwordEncoder.encode(BaseIntegrationTest.TEST_PASSWORD));
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }
}
