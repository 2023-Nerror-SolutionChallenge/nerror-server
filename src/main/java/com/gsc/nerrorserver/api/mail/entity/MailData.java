package com.gsc.nerrorserver.api.mail.entity;

import com.google.errorprone.annotations.Keep;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MailData {

    private String subject; // 제목
    private String from; // 발신자
    private String contents; // 내용
    private List<File> attachments; // 첨부파일
    private Date receivedDate;
    private Long attachmentSize;

    @Builder
    public MailData(String subject, String from, String contents, List<File> attachments, Date receivedDate, Long attachmentSize) {
        this.subject = subject;
        this.from = from;
        this.contents = contents;
        this.attachments = attachments;
        this.receivedDate = receivedDate;
        this.attachmentSize = attachmentSize;
    }
}
