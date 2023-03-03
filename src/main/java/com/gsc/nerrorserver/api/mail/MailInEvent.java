package com.gsc.nerrorserver.api.mail;

import org.springframework.context.ApplicationEvent;

public class MailInEvent extends ApplicationEvent {

    private static final long serialVersionUID = 5168107269200915273L;
    private MimeMessageInfoHolder mailMessageInfoHolder;
    public MailInEvent(Object source, MimeMessageInfoHolder mailMessageInfoHolder) {
        super(source);
        this.mailMessageInfoHolder = mailMessageInfoHolder;
    }
    public MimeMessageInfoHolder getMailMessageInfoHolder() {
        return mailMessageInfoHolder;
    }
}
