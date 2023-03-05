package com.gsc.nerrorserver.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsc.nerrorserver.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // UsernamePasswordAuthenticationFilter 보다 선행되어 실행되며, 딱 한번만 실행

        // Access / Refresh 헤더에서 토큰을 가져옴.
        String accessToken = jwtUtil.getHeaderToken(request, "Access");
        String refreshToken = jwtUtil.getHeaderToken(request, "Refresh");

        if(accessToken != null) {
            // 1. Access Token 유효 -> setAuthentication 통해 인증 정보 컨텍스트에 저장
            if(jwtUtil.tokenValidation(accessToken)){
                setAuthentication(jwtUtil.getEmailFromToken(accessToken));
            }
            // 2. Access Token 만료 && Refresh Token 존재
            else if (refreshToken != null) {
                // Refresh Token 유효성 검증 및 DB 존재유무 확인
                boolean isRefreshToken = jwtUtil.refreshTokenValidation(refreshToken);
                if (isRefreshToken) {
                    String loginId = jwtUtil.getEmailFromToken(refreshToken); // Refresh Token 으로 이메일 가져오기
                    String newAccessToken = jwtUtil.createToken(loginId, "Access"); // 새로운 Access Token 발급
                    jwtUtil.setHeaderAccessToken(response, newAccessToken); // 헤더에 Access Token 추가
                    setAuthentication(jwtUtil.getEmailFromToken(newAccessToken)); // 컨텍스트에 인증 정보 넣기
                }
                // Refresh Token 만료됐거나 OR DB에 존재하지 않는 경우, 예외처리 (BAD REQUEST 발생)
                else {
                    jwtExceptionHandler(response, "Refresh Token 이 만료되었습니다.", HttpStatus.BAD_REQUEST);
                    return;
                }
            }
        }

        filterChain.doFilter(request,response);
    }

    // Authentication 을 컨텍스트에 넣어줌
    public void setAuthentication(String email) {
        Authentication authentication = jwtUtil.createAuthentication(email);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Jwt 예외처리
    public void jwtExceptionHandler(HttpServletResponse response, String msg, HttpStatus status) {
        response.setStatus(status.value());
        response.setContentType("application/json");
        try {
            String json = new ObjectMapper().writeValueAsString(new GlobalResponse(msg, status.value()));
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
