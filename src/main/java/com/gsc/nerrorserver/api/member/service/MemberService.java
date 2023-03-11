package com.gsc.nerrorserver.api.member.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.gsc.nerrorserver.api.mail.service.MailService;
import com.gsc.nerrorserver.api.member.entity.Member;
import com.gsc.nerrorserver.api.member.dto.LoginRequestDto;
import com.gsc.nerrorserver.api.member.dto.SignupRequestDto;
import com.gsc.nerrorserver.api.member.dto.TokenResponseDto;
import com.gsc.nerrorserver.api.member.entity.UserDetailsImpl;
import com.gsc.nerrorserver.global.auth.JwtUtil;
import com.gsc.nerrorserver.global.auth.entity.RefreshToken;
import com.gsc.nerrorserver.global.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberFirebaseService memberFirebaseService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /* 회원가입 */
    @Transactional
    public void signup(HttpServletResponse res, SignupRequestDto signupRequestDto) throws Exception {

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        Member member = Member.builder()
                .id(signupRequestDto.getId())
                .nickname(signupRequestDto.getNickname())
                .password(encodedPassword)
                .picture(signupRequestDto.getPicture())
                .build();
//        memberRepository.save(member);

        // 파이어베이스 Realtime DB 저장
        memberFirebaseService.saveMember(member);

        // 토큰 생성
        TokenResponseDto tokenResponseDto = jwtUtil.createAllToken(signupRequestDto.getId()); // 토큰 생성

        // Refresh Token DB 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .email(signupRequestDto.getId())
                .token(tokenResponseDto.getRefreshToken())
                .build();
        refreshTokenRepository.save(refreshToken);

        jwtUtil.setHeaderToken(res, tokenResponseDto);
    }

    /* 로그인 */
    @Transactional
    public void login(HttpServletResponse res, LoginRequestDto loginRequestDto) throws Exception {
        // 존재하지 않는 회원에 대한 에러처리
        if (!memberFirebaseService.existsMemberById(loginRequestDto.getId())) {
            throw new BadCredentialsException("존재하지 않는 이메일입니다.");
        }

        else {
            Member member = memberFirebaseService.findById(loginRequestDto.getId());

            if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
                throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
            }

            memberFirebaseService.addAttendance(member.getId()); // 출석 수 추가
            TokenResponseDto tokenResponseDto = jwtUtil.createAllToken(member.getId());
            jwtUtil.setHeaderToken(res, tokenResponseDto);
        }
    }

}
