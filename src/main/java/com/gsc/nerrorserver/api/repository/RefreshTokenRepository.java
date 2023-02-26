package com.gsc.nerrorserver.api.repository;

import com.gsc.nerrorserver.api.entity.Member;
import com.gsc.nerrorserver.api.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByEmail(String email);
}
