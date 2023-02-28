package com.gsc.nerrorserver.api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;

@Getter
@NoArgsConstructor
public class Member extends BaseTimeEntity implements UserDetails {

    private String email;
    private String password;
    private String nickname;
    private String picture;

    private int deletedCount;
    private int currentLevel;
    private int totalCount;


    // 회원가입용
    @Builder
    public Member(String email, String password, String nickname, String picture) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.picture = picture;
        this.currentLevel = 0;
    }

//    public void setMember(Member member) {
//        this.member = member;
//    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    // 계정 만료되었는지 (true - 만료 안됨)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠겨있는지 (true - 안잠김)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 계정 비밀번호 만료되었는지 (true - 만료 X)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 상태인지 (true - 활성화)
    @Override
    public boolean isEnabled() {
        return true;
    }

}
