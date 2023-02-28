package com.gsc.nerrorserver.api.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.gsc.nerrorserver.api.service.EmailService;
import com.gsc.nerrorserver.api.service.MemberService;
import com.gsc.nerrorserver.api.service.dto.EmailConfirmDto;
import com.gsc.nerrorserver.api.service.dto.LoginRequestDto;
import com.gsc.nerrorserver.api.service.dto.SignupRequestDto;
import com.gsc.nerrorserver.firebase.FirebaseService;
import com.gsc.nerrorserver.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final EmailService emailService;
    private final MemberService memberService;
//    private final MemberRepository memberRepository;
    private final FirebaseService firebaseService;

    @PostMapping("/confirm")
    public ApiResponse emailConfirm(@RequestBody EmailConfirmDto dto) throws Exception {

        String ePw = emailService.sendSimpleMessage(dto.email);
        return ApiResponse.success("AuthenticationCode", ePw);
    }

    @PostMapping("/signup")
    public ApiResponse signup(HttpServletResponse res, @RequestBody SignupRequestDto dto) throws Exception {
        // 중복가입 방지
        if (firebaseService.existsMemberByEmail(dto.getEmail())) {
            res.setStatus(HttpServletResponse.SC_CONFLICT);
            return ApiResponse.conflict("error", "존재하는 이메일입니다.");
        }

        // 닉네임 중복검사
        else if (firebaseService.existsMemberByNickname(dto.getNickname())) {
            res.setStatus(HttpServletResponse.SC_CONFLICT);
            return ApiResponse.conflict("error", "중복된 닉네임입니다.");
        }

        else {
            // 회원가입 진행
            memberService.signup(res, dto);
            return ApiResponse.success("msg", "회원가입에 성공했습니다.");
        }
    }

    @PostMapping("/login")
    public ApiResponse login(HttpServletResponse res, @RequestBody LoginRequestDto dto) throws Exception {
        try {
            memberService.login(res, dto);
            return ApiResponse.success("msg", "로그인에 성공했습니다.");
        } catch (BadCredentialsException e) {
            e.printStackTrace();
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return ApiResponse.unauthorized("error", "인증 정보가 부족합니다.");
        }
    }
}