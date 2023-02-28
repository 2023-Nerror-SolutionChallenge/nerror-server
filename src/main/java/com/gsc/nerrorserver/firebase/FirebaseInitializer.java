package com.gsc.nerrorserver.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

/* 파이어베이스 초기화 */
@Slf4j
@Service
public class FirebaseInitializer {
    @Value("${app.firebase-configuration-file}")
    private String firebaseConfigPath;

    @Bean
    public void initialize() throws IOException {
        FileInputStream serviceAccount = new FileInputStream(firebaseConfigPath);
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials).build();
        FirebaseApp.initializeApp(options);

        log.info("파이어베이스 초기화가 완료되었습니다.");
    }
}
