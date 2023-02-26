package com.gsc.nerrorserver.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;

    public static final String ePw = createKey();

    private MimeMessage createMessage(String to)throws Exception{
        log.info("수신자 : " + to);
        log.info("인증번호 : " + ePw);
        MimeMessage message = emailSender.createMimeMessage();


        message.addRecipients(MimeMessage.RecipientType.TO, to); //수신자 지정
        message.setSubject("[Marbon] 이메일 인증번호를 확인해주세요."); //발신 메일제목

        String msgg="";
        msgg+= "<div style='margin:20px;'>";
//        msgg+= "<h1> 안녕하세요 . </h1>";
//        msgg+= "<br>";
//        msgg+= "<p>Marbon 을 이용해주셔서 감사합니다.<p>";
        msgg+= "<br>";
        msgg+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgg+= "<h3 style='color:blue;'>아래는 회원가입 인증 코드입니다.</h3>";
        msgg+= "<div style='font-size:130%'>";
        msgg+= "CODE : <strong>";
        msgg+= ePw+"</strong><div><br/> ";
        msgg+= "</div>";
        message.setText(msgg, "utf-8", "html"); //내용 지정
        message.setFrom(new InternetAddress("marbonkr@gmail.com","Marbon")); //보내는 사람 지정

        return message;
    }

    // 인증번호 생성기
    public static String createKey() {
        StringBuilder key = new StringBuilder();
        Random rnd = new Random();

        for (int i = 0; i < 8; i++) { // 인증코드 8자리
            int index = rnd.nextInt(3); // 0~2 까지 랜덤

            switch (index) {
                case 0:
                    key.append((char) ((int) (rnd.nextInt(26)) + 97));
                    //  a~z  (ex. 1+97=98 => (char)98 = 'b')
                    break;
                case 1:
                    key.append((char) ((int) (rnd.nextInt(26)) + 65));
                    //  A~Z
                    break;
                case 2:
                    key.append((rnd.nextInt(10)));
                    // 0~9
                    break;
            }
        }
        return key.toString();
    }

    public String sendSimpleMessage(String to) throws Exception {

        MimeMessage message = createMessage(to);
        log.info(String.valueOf(message));

        try { //예외처리
            emailSender.send(message);
            log.info("이메일 전송에 성공했음다");
        } catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
        return ePw; // 반환값
    }
}
