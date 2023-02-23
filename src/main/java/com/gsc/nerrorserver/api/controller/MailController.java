package com.gsc.nerrorserver.api.controller;

import com.gsc.nerrorserver.api.service.dto.EmailConfirmDto;
import com.gsc.nerrorserver.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import com.gsc.nerrorserver.api.service.EmailService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MailController {

    private final EmailService emailService;

    @PostMapping("/member/auth")
    public ApiResponse emailConfirm(@RequestBody EmailConfirmDto dto) throws Exception {

        String ePw = emailService.sendSimpleMessage(dto.email);
        return ApiResponse.success("AuthenticationCode", ePw);
    }
}
