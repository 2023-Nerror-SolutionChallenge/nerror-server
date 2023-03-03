package com.gsc.nerrorserver.api.controller;

import com.gsc.nerrorserver.api.mail.ImapIntegrationFlowService;
import com.gsc.nerrorserver.api.service.MailService;
import com.gsc.nerrorserver.api.service.MemberService;
import com.gsc.nerrorserver.api.service.dto.*;
import com.gsc.nerrorserver.firebase.FirebaseService;
import com.gsc.nerrorserver.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final MailService mailService;
    private final MemberService memberService;
//    private final MemberRepository memberRepository;
    private final FirebaseService firebaseService;
    private final ImapIntegrationFlowService flowService;

    @PostMapping("/confirm")
    public ApiResponse emailConfirm(@RequestBody MailConfirmDto dto) throws Exception {

        String ePw = mailService.sendSimpleMessage(dto.email);
        return ApiResponse.success("AuthenticationCode", ePw);
    }

    @PostMapping("/signup")
    public ApiResponse signup(HttpServletResponse res, @RequestBody SignupRequestDto dto) throws Exception {
        // 중복가입 방지
        if (firebaseService.existsMemberById(dto.getId())) {
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

    @PostMapping("/addAccount")
    public ApiResponse addAccount(HttpServletResponse res, @RequestBody MailReceiveDto dto) {
        try {
            firebaseService.addAccount(dto);
            return ApiResponse.success("msg", "계정 추가에 성공했습니다.");
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return ApiResponse.fail();
        }
    }

    @PostMapping("/saveMailData")
    public ApiResponse saveMailData(HttpServletResponse res, @RequestBody MailReceiveDto dto) {
        try {
            mailService.readInboundMails(dto);
            return ApiResponse.success("msg", "메세지를 읽어오는 데에 성공했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ApiResponse.fail();
        }
    }
}