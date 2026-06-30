package com.primesys.adminserviceserver.modules.user.services;

import com.primesys.adminserviceserver.modules.user.dtos.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    UserDTO getUserById(String id);
}
