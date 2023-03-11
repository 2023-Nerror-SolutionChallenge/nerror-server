package com.gsc.nerrorserver.api.member.controller;

import com.gsc.nerrorserver.api.member.dto.LoginRequestDto;
import com.gsc.nerrorserver.api.member.dto.MailConfirmDto;
import com.gsc.nerrorserver.api.member.dto.SignupRequestDto;
import com.gsc.nerrorserver.api.mail.service.MailService;
import com.gsc.nerrorserver.api.member.service.MemberFirebaseService;
import com.gsc.nerrorserver.api.member.service.MemberService;
import com.gsc.nerrorserver.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class MemberController {

    private final MailService mailService;
    private final MemberService memberService;
    private final MemberFirebaseService memberFirebaseService;

    @PostMapping("/confirm")
    public ApiResponse emailConfirm(@RequestBody MailConfirmDto dto) throws Exception {

        String ePw = mailService.sendSimpleMessage(dto.email);
        return ApiResponse.success("AuthenticationCode", ePw);
    }

    @PostMapping("/signup")
    public ApiResponse signup(HttpServletResponse res, @RequestBody SignupRequestDto dto) throws Exception {
        // 중복가입 방지
        if (memberFirebaseService.existsMemberById(dto.getId())) {
            res.setStatus(HttpServletResponse.SC_CONFLICT);
            return ApiResponse.conflict("error", "존재하는 이메일입니다.");
        }

        // 닉네임 중복검사
        else if (memberFirebaseService.existsMemberByNickname(dto.getNickname())) {
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