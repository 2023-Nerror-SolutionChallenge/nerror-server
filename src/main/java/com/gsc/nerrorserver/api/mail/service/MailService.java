package com.gsc.nerrorserver.api.mail.service;


import com.gsc.nerrorserver.api.mail.dto.MailDeleteDto;
import com.gsc.nerrorserver.api.mail.dto.MailReceiveDto;
import com.gsc.nerrorserver.api.mail.entity.MailData;
import com.gsc.nerrorserver.global.auth.MailAuthenticator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import java.io.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender emailSender;
    public static final String ePw = createKey();
    public static final String MAIL_IMG_LOGO_ROUTE = "https://ifh.cc/g/yl5Kbp.png";

    private MimeMessage createMessage(String to) throws Exception {
        log.info("수신자 : " + to);
        log.info("인증번호 : " + ePw);
        MimeMessage message = emailSender.createMimeMessage();


        message.addRecipients(MimeMessage.RecipientType.TO, to); //수신자 지정
        message.setSubject("[Marbon] 이메일 인증을 완료해주세요."); //발신 메일제목

        String msgg = "";
        msgg += "<div style='margin:20px;'>";
//        msgg+= "<h1> 안녕하세요 . </h1>";
//        msgg+= "<br>";
//        msgg+= "<p>Marbon 을 이용해주셔서 감사합니다.<p>";
        msgg += "<br>";
        msgg += "<img src=" + MAIL_IMG_LOGO_ROUTE + " />";
        msgg += "<h2>회원가입을 위한 이메일 인증을 진행합니다.<br>아래 8자리 인증코드를 입력하여 이메일 인증을 완료해주세요.</h2>";
        msgg += "<div style='font-size:170%'>";
        msgg += "인증번호 : <strong>" + ePw + "</strong><br/> ";
        msgg += "</div>";
        message.setText(msgg, "utf-8", "html"); //내용 지정
        message.setFrom(new InternetAddress("marbonkr@gmail.com", "Marbon")); //보내는 사람 지정

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
        } catch (MailException es) {
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
        return ePw; // 반환값
    }

    public int readMailCount(MailReceiveDto dto) throws Exception {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");

        Session session = Session.getDefaultInstance(props, new MailAuthenticator(dto.getUsername(), dto.getPassword()));
        Store store = session.getStore("imaps");
        store.connect(dto.getHost(), dto.getUsername(), dto.getPassword());

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        int count = inbox.getMessageCount();
        inbox.close(false);
        store.close();
        return count;
    }

    public MailData handleMailData(Message msg) throws Exception {
        String from = msg.getFrom()[0].toString();
        String subject = msg.getSubject();
        String contentType = msg.getContentType();
        Date receivedDate = msg.getReceivedDate();
        Long attachmentSize = 0L;
        String messageContent = "";
        Collection<InputStream> attachments = new ArrayList<>();
        log.info("제목 : " + subject);

        if (contentType.contains("multipart")) {
            // 멀티파트 처리
            Multipart multipart = (Multipart) msg.getContent();
            for (int idx = 0; idx < multipart.getCount(); idx++) {
                MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(idx);
                // 첨부파일이 있는 경우
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    attachmentSize += part.getSize();
                    String fileName = part.getFileName();
                    log.info("파일 제목 : " + fileName); // 파일 정보 출력
                    // 파일 inputStream 으로 저장
                    attachments.add(part.getInputStream());
                } else { // 없으면 메일 내용만 저장
                    messageContent = part.getContent().toString();
                }
            }
        } else {
            Object content = msg.getContent();
            if (content != null) {
                messageContent = content.toString();
//                    log.info("메일 내용 : " + messageContent);
            }
        }
        String removeHtmlTag = messageContent.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
        return MailData.builder()
                .subject(subject)
                .from(from)
                .contents(removeHtmlTag)
                .receivedDate(receivedDate)
                .attachmentSize(attachmentSize)
                .build();
    }

    public Collection<MailData> readAllMails(MailReceiveDto dto) throws Exception {

        String protocol = "imaps"; //imaps

        // TODO : Collection 으로 받아와서 일괄저장할건지, 불러올때마다 firestore 호출할건지?
        Collection<MailData> mailList = new ArrayList<>();

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", protocol);

        Session session = Session.getDefaultInstance(props, new MailAuthenticator(dto.getUsername(), dto.getPassword()));
        Store store = session.getStore(protocol);
        store.connect(dto.getHost(), dto.getUsername(), dto.getPassword());

        Folder inbox = store.getFolder("INBOX"); // inbox = 받은편지함
        inbox.open(Folder.READ_WRITE);

        // 받은 편지함에 있는 메일 모두 읽어오기
        Message[] arrayMessages = inbox.getMessages();

        for (Message msg : arrayMessages) {
            mailList.add(handleMailData(msg));
        }

        // 메일박스와 연결 끊기
        inbox.close(false);
        store.close();

        return mailList;
    }

    // 새로고침시 새로운 메일 읽어오기
    public Collection<MailData> readRecentMails(MailReceiveDto dto, Date lastReadDate) throws Exception {

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");

        Session session = Session.getDefaultInstance(props, new MailAuthenticator(dto.getUsername(), dto.getPassword()));
        Store store = session.getStore("imaps");
        store.connect(dto.getHost(), dto.getUsername(), dto.getPassword());

        Folder inbox = store.getFolder("INBOX"); // inbox = 받은편지함
        inbox.open(Folder.READ_WRITE);

        Collection<MailData> mailList = new ArrayList<>();

        // 안 읽은 메일 필터링해서 불러오기
        SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GT, lastReadDate);
        Message[] recentMessages = inbox.search(newerThan);

        // 읽어서 데이터 저장
        for (Message msg : recentMessages) {
            mailList.add(handleMailData(msg));
        }

        inbox.close(false);
        store.close();
        return mailList;
    }

    // 특정 메일 삭제 요청
    public void deleteSpecificMail(MailDeleteDto dto) {

        // 서버에도 delete 태깅해야 하고, db에서도 삭제해야 함
        // 프로젝트 구조도 바꿔야돼 ㅠㅠ
    }
}
