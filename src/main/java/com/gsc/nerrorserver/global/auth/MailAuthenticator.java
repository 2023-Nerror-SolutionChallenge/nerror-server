package com.gsc.nerrorserver.global.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

@Slf4j
public class MailAuthenticator extends Authenticator {
    private String username;
    private String password;
    public MailAuthenticator(String username, String password) {
        super();

        if( !StringUtils.hasText(username) ) {
            throw new IllegalArgumentException("Passata una username utente casella postale non valida ["+username+"]");
        }
        if( !StringUtils.hasText(password) ) {
            throw new IllegalArgumentException("Passata una password utente casella postale non valida ["+password+"]");
        }
        this.username = username;
        this.password = password;
    }
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        if( log.isDebugEnabled() ) {
            log.debug("확인중");
        }
        return new PasswordAuthentication(username, password);
    }
}