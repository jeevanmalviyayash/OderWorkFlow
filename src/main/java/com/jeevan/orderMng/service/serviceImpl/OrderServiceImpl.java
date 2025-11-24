package com.jeevan.orderMng.service.serviceImpl;

import com.jeevan.orderMng.constant.ApprovalStatus;
import com.jeevan.orderMng.constant.Role;
import com.jeevan.orderMng.constant.Status;
import com.jeevan.orderMng.dto.OrderRequest;
import com.jeevan.orderMng.entity.Approval;
import com.jeevan.orderMng.entity.AuditLog;
import com.jeevan.orderMng.entity.Order;
import com.jeevan.orderMng.entity.User;
import com.jeevan.orderMng.repository.ApprovalRepository;
import com.jeevan.orderMng.repository.AuditLogRepository;
import com.jeevan.orderMng.repository.OrderRepository;
import com.jeevan.orderMng.repository.UserRepository;
import com.jeevan.orderMng.service.IOrderService;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class OrderServiceImpl implements IOrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new Order in DRAFT status.
     * Generate order number after saving to get ID.
     */
    @Override
    public Order createOrder(OrderRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Order order = Order.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .department(request.getDepartment())
                .amount(request.getAmount())
                .status(Status.DRAFT)
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .build();

        // Save order to get generated ID
//        order = orderRepository.save(order);

        Random random = new Random();
        int randomFourDigit = random.nextInt(100000000);  // Generates 0 to 9999
        // Generate order number: ORD-<Year>-<ID padded>
        String orderNumber = String.format("ORD-%d-%04d", LocalDateTime.now().getYear(), randomFourDigit);
        order.setOrderNumber(orderNumber);

        // Save again with orderNumber updated
        order = orderRepository.save(order);

        log.info("Created new order with orderNumber {} by user {}", orderNumber, userEmail);
        logAudit("Order", order.getId(), "CREATE", user);

        return order;
    }

    /**
     * Submit an order from DRAFT to SUBMITTED.
     * Only creator can submit.
     * Create approvals based on amount.
     */
    @Override
    public Order submitOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        if (!order.getCreatedBy().getEmail().equals(userEmail)) {
            throw new RuntimeException("Only creator can submit order");
        }

        if (order.getStatus() != Status.DRAFT) {
            throw new RuntimeException("Order can only be submitted from DRAFT status");
        }

        order.setStatus(Status.SUBMITTED);
        order.setSubmittedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        log.info("Order {} submitted by user {}", order.getOrderNumber(), userEmail);
        logAudit("Order", order.getId(), "SUBMIT", user);

        createApprovals(order);

        return order;
    }

    /**
     * Approve order based on role and approval level.
     * MANAGER approves level 1, ADMIN approves final level.
     */
    @Override
    public Order approveOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        List<Approval> approvals = approvalRepository.findByOrder(order);
        Approval pendingApproval = approvals.stream()
                .filter(a -> a.getStatus() == ApprovalStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No pending approval found"));

        // Authorization check based on approval level and user role
        if (pendingApproval.getLevel() == 1 && user.getRole() != Role.MANAGER) {
            throw new RuntimeException("Only MANAGER can approve level 1");
        } else if (pendingApproval.getLevel() == 2 && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only ADMIN can approve level 2");
        }

        pendingApproval.setStatus(ApprovalStatus.APPROVED);
        approvalRepository.save(pendingApproval);

        log.info("Approval level {} for order {} approved by user {}", pendingApproval.getLevel(), order.getOrderNumber(), userEmail);
        logAudit("Approval", pendingApproval.getId(), "APPROVE", user);

        // Check if all approvals are done
        boolean allApproved = approvals.stream()
                .allMatch(a -> a.getStatus() == ApprovalStatus.APPROVED);

        if (allApproved) {
            order.setStatus(Status.APPROVED);
            order.setApprovedAt(LocalDateTime.now());
            orderRepository.save(order);
            log.info("Order {} fully approved", order.getOrderNumber());
            logAudit("Order", order.getId(), "APPROVE", user);
        }

        return order;
    }

    /**
     * Reject an order.
     */
    @Override
    public Order rejectOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Authorization: Only MANAGER or ADMIN can reject - to enforce via controller or custom annotation

        order.setStatus(Status.REJECTED);
        order.setRejectedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order {} rejected by user {}", order.getOrderNumber(), userEmail);
        logAudit("Order", order.getId(), "REJECT", user);

        return order;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    /**
     * Create approval entries based on order amount.
     * Level 1 approver is any MANAGER.
     * Level 2 approver is any ADMIN if amount > 100000.
     */
    private void createApprovals(Order order) {
        // Find any MANAGER user for level 1 approval
        Optional<User> managerOpt = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.MANAGER && u.isActive())
                .findAny();

        if (managerOpt.isEmpty()) {
            throw new RuntimeException("No active MANAGER found for approval");
        }

        User manager = managerOpt.get();

        Approval approvalLevel1 = Approval.builder()
                .order(order)
                .approver(manager)
                .level(1)
                .status(ApprovalStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        approvalRepository.save(approvalLevel1);

        // If amount > 100000, add second level approval by ADMIN
        if (order.getAmount() != null && order.getAmount() > 100000) {
            Optional<User> adminOpt = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN && u.isActive())
                    .findAny();

            if (adminOpt.isEmpty()) {
                throw new RuntimeException("No active ADMIN found for approval");
            }

            User admin = adminOpt.get();

            Approval approvalLevel2 = Approval.builder()
                    .order(order)
                    .approver(admin)
                    .level(2)
                    .status(ApprovalStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            approvalRepository.save(approvalLevel2);
        }
    }

    @Override
    public Page<Order> getAllOrdersFiltered(String status, String department, Pageable pageable) {
        // Handle filtering by status and department with pagination
        // If no filters, just return paged orders.
        if ((status == null || status.isBlank()) && (department == null || department.isBlank())) {
            return orderRepository.findAll(pageable);
        }

        // Filter by status and department dynamically
        return orderRepository.findAll((root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (status != null && !status.isBlank()) {
                try {
                    Status orderStatus = Status.valueOf(status.toUpperCase());
                    predicate = cb.and(predicate, cb.equal(root.get("status"), orderStatus));
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException("Invalid status filter value: " + status);
                }
            }
            if (department != null && !department.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("department")), "%" + department.toLowerCase() + "%"));
            }
            return predicate;
        }, pageable);
    }


    /**
     * Helper to log audit actions.
     */
    private void logAudit(String entityType, Long entityId, String action, User user) {
        AuditLog auditLog = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .user(user)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
    }
}
