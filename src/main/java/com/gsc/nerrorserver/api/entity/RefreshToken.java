package com.gsc.nerrorserver.api.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Entity
@NoArgsConstructor
public class RefreshToken {

    @Id
    @Column(name = "refresh")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String refreshToken;

    @NotNull
    private String email;

    @Builder
    public RefreshToken(String token, String email) {
        this.refreshToken = token;
        this.email = email;
    }

    public RefreshToken updateToken(String token) {
        this.refreshToken = token;
        return this;
    }
}
