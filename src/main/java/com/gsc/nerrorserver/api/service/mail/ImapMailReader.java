package com.gsc.nerrorserver.api.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;
import java.util.Properties;

@Service
@Slf4j
public class ImapMailReader {

    /**
     * 모든 메일 가져오기
     * @param username : 이메일 전체 주소 ex)saltprezel@gmail.com
     * @param password : 메일 계정 비밀반호 ex)ydbiscnmmtwtkxua
     * @param host : 메일 서버별 호스트 이름 ex)imap.gmail.com
     * @param port : 정해진 포트 번호 ex)995
     * @throws javax.mail.MessagingException
     */
    public void receiveAll(String username, String password, String host, int port) throws MessagingException {

        String protocol = "pop3";

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", protocol);
        try {
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore(protocol);
            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX"); // inbox가 받은편지함 의미
            inbox.open(Folder.READ_ONLY);

            // 특정 기간만 읽어오기
//            SearchTerm olderThan = new ReceivedDateTerm (ComparisonTerm.LT, startDate);
//            SearchTerm newerThan = new ReceivedDateTerm (ComparisonTerm.GT, endDate);
//            SearchTerm andTerm = new AndTerm(olderThan, newerThan);

            // 받은 편지함에 있는 메일 모두 읽어오기
            Message[] arrayMessages = inbox.getMessages();
            int msgCount = arrayMessages.length;
            for (int i = msgCount; i>0; i--) {
                Message msg = arrayMessages[i-1];

                // 메일 내용 읽어오기
                String from = msg.getFrom()[0].toString();
                String subject = msg.getSubject();
                String contentType = msg.getContentType();
                String messageContent = ""; // 메일 내용
                String attachFiles = ""; // 첨부파일 리스트

                // 멀티파트 첨부파일 처리
                if (contentType.contains("multipart")) {
                    Multipart multipart = (Multipart) msg.getContent();
                    int numberOfParts = multipart.getCount();
                    for (int partCount = 0; partCount<numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(partCount);
                        // 첨부파일이 있는 경우 파일 저장하거나.. 일단은 로그로 파일 정보 찍어내보자
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            String fileName = part.getFileName();
                            attachFiles += fileName + ", ";
                            log.info("{}번째 메일에 첨부파일이 있습니다. 파일 제목 : {}", i, fileName);
                        } else { // 없으면 메일 내용만 저장
                            messageContent = part.getContent().toString();
                        }
                    }
                    if (attachFiles.length() > 1) {
                        attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
                    }
                } else if (contentType.contains("text/plain") || contentType.contains("text/html")) {
                    Object content = msg.getContent();
                    if (content != null) {
                        messageContent = content.toString();
                    }
                }

                // 읽어온 메일 로그로 출력
                log.info("#{}번째 메일을 읽어오겠습니당.", (i+1));
                log.info("발신자: {}", from);
                log.info("제목: {}", subject);
                log.info("메일 내용: {}", messageContent);
                log.info("첨부파일 목록: {}", attachFiles);
            }

            // 메일박스와 연결 끊기
            inbox.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            log.error("1 예외가 발생했습니다 : " + e);
        } catch (MessagingException e) {
            e.printStackTrace();
            log.error("2 예외가 발생했습니다 : " + e);
        } catch (IOException ex) {
            ex.printStackTrace();
            log.error("3 예외가 발생했습니다 : " + ex);
        }
    }
}
