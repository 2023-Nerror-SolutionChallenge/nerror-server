package com.gsc.nerrorserver.api.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.gsc.nerrorserver.api.entity.MailData;
import com.gsc.nerrorserver.api.service.dto.MailReceiveDto;
import com.gsc.nerrorserver.firebase.FirebaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender emailSender;

    private final FirebaseService firebaseService;

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

    public void readInboundMails(MailReceiveDto dto) {

        String protocol = "imaps"; //imaps

        String username = dto.getUsername();
        String password = dto.getPassword();
        String host = dto.getHost();
        int port = Integer.parseInt(dto.getPort());

        Collection<MailData> mailList = new ArrayList<>();

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", protocol);
        try {
            Session session = Session.getDefaultInstance(props, new MailAuthenticator(username, password));
            Store store = session.getStore(protocol);
            store.connect(host, port, username, password);

            Folder inbox = store.getFolder("INBOX"); // inbox = 받은편지함
            inbox.open(Folder.READ_WRITE);

            // 받은 편지함에 있는 메일 모두 읽어오기
            Message[] arrayMessages = inbox.getMessages();
            int msgCount = arrayMessages.length - 1;
            for (int i = msgCount; i>0; i--) {
                Message msg = arrayMessages[i-1];

                // 메일 내용 읽어오기
                String from = msg.getFrom()[0].toString();
                String subject = msg.getSubject();
                String contentType = msg.getContentType();
                Date receivedDate = msg.getReceivedDate();
                String messageContent = ""; // 메일 내용
                Collection<MimeBodyPart> attachments = new ArrayList<>();

                // 멀티파트 첨부파일 처리 - 파트별로 일일이 검사해서 저장해줘야 함
                if (contentType.contains("multipart")) {
                    Multipart multipart = (Multipart) msg.getContent();
                    int numberOfParts = multipart.getCount();
                    for (int partCount = 0; partCount<numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(partCount);
                        // 첨부파일이 있는 경우
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            String fileName = part.getFileName();
                            log.info("파일 제목 : " + i + fileName); // 파일 정보 출력
                            attachments.add(part);
                        } else { // 없으면 메일 내용만 저장
                            messageContent = part.getContent().toString();
                        }
                    }
                } else if (contentType.contains("text/plain") || contentType.contains("text/html")) {
                    Object content = msg.getContent();
                    if (content != null) {
                        messageContent = content.toString();
                        log.info(messageContent);
                    }
                }

                String removeTag = messageContent.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
                MailData data = MailData.builder()
                        .subject(subject)
                        .from(from)
                        .contents(removeTag)
                        .receivedDate(receivedDate)
                        .build();

                // 읽어온 메일 로그로 출력
                log.info("mail count : " + (i+1));
                log.info("발신자: " + from);
                log.info("제목: " + subject);
                log.info("첨부파일 목록: " + attachments);
//                log.info("내용: " + messageContent);

                firebaseService.saveMailData(dto, data);
            }

            // 메일박스와 연결 끊기
            inbox.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("메일을 저장하는 과정에서 오류가 발생했습니다.");
        }
    }
}
