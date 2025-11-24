package com.jeevan.orderMng.service;

import com.jeevan.orderMng.entity.User;

import java.util.List;

public interface IUserService {
    User getUserByEmail(String email);
    List<User> getAllUsers();
}