package com.gsc.nerrorserver.api.member.service;

import com.gsc.nerrorserver.api.member.entity.Member;
import com.gsc.nerrorserver.api.member.entity.UserDetailsImpl;
import com.gsc.nerrorserver.global.firebase.FirebaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final FirebaseService firebaseService;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Member member = null;

        try {
            member = firebaseService.findById(id);
            UserDetailsImpl userDetails = new UserDetailsImpl();
            userDetails.setMember(member);

            return userDetails;
        } catch (Exception e) {
            throw new RuntimeException("해당 사용자가 DB에 없습니다.");
        }
    }
}
