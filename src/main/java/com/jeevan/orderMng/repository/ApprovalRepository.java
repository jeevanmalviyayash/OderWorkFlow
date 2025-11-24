package com.jeevan.orderMng.repository;

import com.jeevan.orderMng.entity.Approval;
import com.jeevan.orderMng.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByOrder(Order order);
}
