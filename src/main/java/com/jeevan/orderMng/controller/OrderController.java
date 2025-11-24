package com.jeevan.orderMng.controller;

import com.jeevan.orderMng.constant.Role;
import com.jeevan.orderMng.dto.OrderRequest;
import com.jeevan.orderMng.entity.Order;
import com.jeevan.orderMng.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Order Management", description = "Endpoints for managing Orders")
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    /*** Create a new order in DRAFT status.Accessible only by USER role. */
    @PostMapping
    @Operation(summary = "Create a new order (DRAFT)")
    @PreAuthorize("hasRole('USER')")
    public Order createOrder(@Valid @RequestBody OrderRequest request, Authentication authentication) {
        String email = authentication.getName();
        return orderService.createOrder(request, email);
    }

    /*** Submit an existing order to move from DRAFT to SUBMITTED state. Only the creator (USER) can submit.*/
    @PutMapping("/submit/{orderId}")
    @Operation(summary = "Submit an order (USER only, creator)")
    @PreAuthorize("hasRole('USER')")
    public Order submitOrder(@PathVariable Long orderId, Authentication authentication) {
        String email = authentication.getName();
        return orderService.submitOrder(orderId, email);
    }

    /**Approve an order at the required level. Only MANAGER (level 1) or ADMIN (level 2) have access.*/
    @PutMapping("/approve/{orderId}")
    @Operation(summary = "Approve order (MANAGER or ADMIN)")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public Order approveOrder(@PathVariable Long orderId, Authentication authentication) {
        String email = authentication.getName();
        return orderService.approveOrder(orderId, email);
    }

    /**Reject an order. Only MANAGER or ADMIN can reject.
     */
    @PutMapping("/reject/{orderId}")
    @Operation(summary = "Reject order (MANAGER or ADMIN)")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public Order rejectOrder(@PathVariable Long orderId, Authentication authentication) {
        String email = authentication.getName();
        return orderService.rejectOrder(orderId, email);
    }

    /** Get all orders.Accessible by all authenticated users.Supports pagination and filtering by status and department via Pageable and optional parameters.*/
    @GetMapping
    @Operation(summary = "Get all orders with pagination and optional filtering")
    @PreAuthorize("isAuthenticated()")
    public Page<Order> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            Pageable pageable) {
        // For simplicity, this example shows basic pagination;
        // You can enhance the service to filter by status and department as needed.
        return orderService.getAllOrdersFiltered(status, department, pageable);
    }

    /**Get order details by ID. Accessible by authenticated users.*/
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    @PreAuthorize("isAuthenticated()")
    public Order getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }
}
