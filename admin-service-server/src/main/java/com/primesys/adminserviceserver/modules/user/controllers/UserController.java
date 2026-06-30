package com.primesys.adminserviceserver.modules.user.controllers;

import com.primesys.adminservicecommon.dto.DivisionLoginDto;
import com.primesys.adminserviceserver.modules.user.dtos.UserDTO;
import com.primesys.adminserviceserver.modules.user.services.UserService;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import com.primesys.adminserviceserver.service.LoginDetailsService;
import com.primesys.adminserviceserver.service.impl.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v2/users")
@RequiredArgsConstructor
@Tag(name = "Users Controller")
public class UserController {

    private final UserService userService;
    private final LoginDetailsService loginDetailsService;
    private final JwtService jwtService;

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<UserDTO> userDTOS = userService.getAllUsers();
        return ResponseHandler.generateResponse(userDTOS, true, "Users fetched successfully", HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getUserById(@Valid @RequestParam String id) {
        UserDTO userDTO = userService.getUserById(id);
        return ResponseHandler.generateResponse(userDTO, true, "User fetched successfully", HttpStatus.OK);
    }

    @GetMapping("/details")
    HttpApiResponse<DivisionLoginDto> getUsers(@NotNull HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (StringUtils.isBlank(authHeader) || authHeader.length() < 7) {
            return new HttpApiResponse<>(DivisionLoginDto.builder().build(), Boolean.FALSE);
        }
        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);
        final DivisionLoginDto divisionLoginDto = loginDetailsService.getLoginDetails(userEmail);
        final HttpApiResponse<DivisionLoginDto> response = new HttpApiResponse<>(divisionLoginDto, true);
        return response;
    }
}
