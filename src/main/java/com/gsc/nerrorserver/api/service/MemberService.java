package com.gsc.nerrorserver.api.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.gsc.nerrorserver.api.auth.JwtUtil;
import com.gsc.nerrorserver.api.auth.UserDetailsImpl;
import com.gsc.nerrorserver.api.entity.Member;
import com.gsc.nerrorserver.api.entity.RefreshToken;
import com.gsc.nerrorserver.api.repository.RefreshTokenRepository;
import com.gsc.nerrorserver.api.service.dto.LoginRequestDto;
import com.gsc.nerrorserver.api.service.dto.SignupRequestDto;
import com.gsc.nerrorserver.api.service.dto.TokenResponseDto;
import com.gsc.nerrorserver.firebase.FirebaseService;
import com.gsc.nerrorserver.response.ApiResponse;
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

//    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final FirebaseService firebaseService;

    /* 회원가입 */
    @Transactional
    public void signup(HttpServletResponse res, SignupRequestDto signupRequestDto) throws Exception {

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        Member member = Member.builder()
                .email(signupRequestDto.getEmail())
                .nickname(signupRequestDto.getNickname())
                .password(encodedPassword)
                .picture(signupRequestDto.getPicture())
                .build();
//        memberRepository.save(member);

        // 파이어베이스 Realtime DB 저장
        firebaseService.saveMember(member);

        // 토큰 생성
        TokenResponseDto tokenResponseDto = jwtUtil.createAllToken(signupRequestDto.getEmail()); // 토큰 생성

        // Refresh Token DB 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .email(signupRequestDto.getEmail())
                .token(tokenResponseDto.getRefreshToken())
                .build();
        refreshTokenRepository.save(refreshToken);

        jwtUtil.setHeaderToken(res, tokenResponseDto);
    }

    /* 로그인 */
    @Transactional
    public void login(HttpServletResponse res, LoginRequestDto loginRequestDto) throws Exception {
        // 존재하지 않는 회원에 대한 에러처리
        if (!firebaseService.existsMemberByEmail(loginRequestDto.getEmail())) {
            throw new BadCredentialsException("존재하지 않는 이메일입니다.");
        }

        else {
            Member member = firebaseService.findByEmail(loginRequestDto.getEmail());

            if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
                throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
            }

            TokenResponseDto tokenResponseDto = jwtUtil.createAllToken(member.getEmail());
            jwtUtil.setHeaderToken(res, tokenResponseDto);
        }
    }
}
