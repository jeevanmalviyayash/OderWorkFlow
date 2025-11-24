package com.jeevan.orderMng.service;

import com.jeevan.orderMng.dto.OrderRequest;
import com.jeevan.orderMng.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IOrderService {

    public Order createOrder(OrderRequest request, String userEmail);
    public Order submitOrder(Long orderId, String userEmail);
    public Order approveOrder(Long orderId, String userEmail);
    public Order rejectOrder(Long orderId, String userEmail);
    public List<Order> getAllOrders();
    public Order getOrderById(Long orderId);
    Page<Order> getAllOrdersFiltered(String status, String department, Pageable pageable);

}
