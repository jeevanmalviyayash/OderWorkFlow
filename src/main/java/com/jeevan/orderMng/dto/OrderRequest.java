package com.jeevan.orderMng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String title;
    private String description;
    private String department;
    private Double amount;
}
