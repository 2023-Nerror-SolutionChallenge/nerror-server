package com.gsc.nerrorserver.api.mail;

import com.gsc.nerrorserver.api.service.MailAuthenticator;
import com.gsc.nerrorserver.api.service.dto.MailReceiveDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.commons.mail.util.MimeMessageUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.integration.StaticMessageHeaderAccessor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.mail.dsl.ImapIdleChannelAdapterSpec;
import org.springframework.integration.mail.dsl.Mail;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.activation.DataSource;
import javax.annotation.PreDestroy;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
public class ImapIntegrationFlowService {
    public static final String MAIL_IN_FLOW_ID_PREFIX = "NERROR_MAIL_FLOW_";
    @Autowired
    private IntegrationFlowContext flowContext;
//    @Value("${webmail.be.imap.message.delete}")
//    private boolean deleteMessages;
//    @Value("${webmail.be.imap.mark.message.read}")
//    private boolean markMessagesRead;
//    @Value("${webmail.be.mail.debug}")
//    private boolean mailDebug;
//    @Value("${webmail.be.imap.polling.seconds}")
//    private int pollingSeconds;
//    @Value("${webmail.be.imap.max.message.per.poll}")
//    private int maxMailMessagePerPoll;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    private Map<String, Closeable> closeables = new HashMap<String, Closeable>();

    public void nerrorImapMailReceiver(MailReceiveDto dto) {
        log.info("Nerror ImapMailReceiver");
        String mailAddress = dto.getUsername(); //Insert here you mail address (e.g. test@gmail.com)
        String username = dto.getUsername(); //Insert here username
        String password = dto.getPassword(); //Insert here password
        String mailServerHost = dto.getHost(); //Insert here you mail server hosts (e.g. imap.gmail.com)
        String mailServerPort = "993"; //insert here mail server post (e.g. 993)
        String flowId = MAIL_IN_FLOW_ID_PREFIX+mailAddress;
        String flowIdAutoClose = MAIL_IN_FLOW_ID_PREFIX+mailAddress+"AUTOCLOSE";
        if( flowContext.getRegistrationById(flowId) != null ) {
            if( log.isInfoEnabled() ) {
                log.info("Integration flow con id {} già esistente. Lo rimuovo", flowId);
            }
            closeFolder(mailAddress);
            flowContext.remove(flowId);
        }
        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.debug", "false");
        javaMailProperties.setProperty("mail.imap.starttls.enable", "true");
        javaMailProperties.setProperty("mail.imap.ssl.enable", "true");
        javaMailProperties.setProperty("mail.imap.auth", "true");
        javaMailProperties.setProperty("mail.store.protocol","imap");
        StringBuilder connectionUrl = new StringBuilder();
        connectionUrl.append("imap://");
        connectionUrl.append(URLEncoder.encode(username, StandardCharsets.UTF_8));
        connectionUrl.append(":");
        connectionUrl.append(URLEncoder.encode(password, StandardCharsets.UTF_8));
        connectionUrl.append("@");
        connectionUrl.append(mailServerHost);
        connectionUrl.append(":");
        connectionUrl.append(mailServerPort);
        connectionUrl.append("/INBOX");
        Function<MimeMessage, Boolean> selectFunction = (MimeMessage message) -> {
            try {
                return true;
            }catch (Exception e) {
                log.error("에러가 발생했습니다", e);
                return false;
            }
        };

        IntegrationFlow flow = null;
        IntegrationFlow flowAutoClose = null;

        String userFlag = mailServerHost + "_idle_adapter";
        ImapIdleChannelAdapterSpec imapIdleChannelAdapterSpec = Mail.imapIdleAdapter(connectionUrl.toString())
                .javaMailProperties(javaMailProperties)
                .shouldDeleteMessages(false)
                .shouldMarkMessagesAsRead(true)
                .autoStartup(true)
                .autoCloseFolder(false)
                .userFlag(userFlag)
                .id(userFlag)
                //.searchTermStrategy(this::notSeenTerm)
                .selector(selectFunction);
        ImapIdleChannelAdapterSpec imapIdleChannelAdapterSpecAutoClose = Mail.imapIdleAdapter(connectionUrl.toString())
                .javaMailProperties(javaMailProperties)
                .shouldDeleteMessages(false)
                .shouldMarkMessagesAsRead(true)
                .autoStartup(true)
                .autoCloseFolder(true)
                .userFlag(userFlag)
                .id(userFlag+"AUTOCLOSE")
                //.searchTermStrategy(this::notSeenTerm)
                .selector(selectFunction);

        imapIdleChannelAdapterSpec = imapIdleChannelAdapterSpec.javaMailAuthenticator(new MailAuthenticator(username, password));
        imapIdleChannelAdapterSpecAutoClose = imapIdleChannelAdapterSpecAutoClose.javaMailAuthenticator(new MailAuthenticator(username, password));
        flow = IntegrationFlows
                .from(imapIdleChannelAdapterSpec)
                .handle(message ->{
                    //Prendo il closable del messaggio e valorizzo i l'elenco di closeale da chiudere
                    Closeable closeable = StaticMessageHeaderAccessor.getCloseableResource(message);
                    if( !closeables.containsKey(mailAddress) ) {
                        closeables.put(mailAddress, closeable);
                    }
                    log.info("Publishing event");
                    publishMailEvent(message);
                })
                .get();
        flowAutoClose = IntegrationFlows
                .from(imapIdleChannelAdapterSpecAutoClose)
                .handle(message ->{
                    //Just print i received the messa
                    log.info("Message received : {}", message);
                })
                .get();

        flowContext.registration(flow).id(flowId).register();
        flowContext.registration(flowAutoClose).id(flowIdAutoClose).register();
    }

    private void publishMailEvent( Message<?> message ) {
        try {
            //recupero il mime message e propago l'evento
            MimeMessage mimeMessage = (MimeMessage) message.getPayload();
            if( log.isDebugEnabled() ) {

                try {
                    mimeMessage.getAllHeaders().asIterator().forEachRemaining(header->{
                        log.debug("Header name {} header value {}", header.getName(), header.getValue());
                    });
                } catch (MessagingException e) {
                    log.error("Errore nella lettura degli header", e);
                }
            }
            MimeMessageParser parser = new MimeMessageParser(mimeMessage);
            parser = parser.parse();
            String msgId = mimeMessage.getMessageID();
            MimeMessageInfoHolder mmih = new MimeMessageInfoHolder();
            mmih.setFrom(parser.getFrom());
            mmih.setBccs(toStringAddress(parser.getBcc()));
            mmih.setCcs(toStringAddress(parser.getCc()));
            mmih.setTos(toStringAddress(parser.getTo()));
            List<DataSource> allegati = parser.getAttachmentList();
            if( allegati != null && !allegati.isEmpty() ) {
                mmih.setAllegatiMail(allegati);
            }else {
                mmih.setAllegatiMail(Collections.emptyList());
            }
            String corpoMail = parser.getHtmlContent();
            if( !StringUtils.hasText(corpoMail) ) {
                corpoMail = parser.getPlainContent();
            }
            mmih.setMailReceivedDate(mimeMessage.getReceivedDate());

            mmih.setCorpoMail(corpoMail);
            mmih.setIdMessaggioMail(msgId);
            mmih.setOggettoMail(mimeMessage.getSubject());
            File emlFile = File.createTempFile(mimeMessage.getSubject().replaceAll("[^a-zA-Z0-9\\.\\-]", "_"), ".eml");
            MimeMessageUtils.writeMimeMessage(mimeMessage, emlFile);
            mmih.setMailEmlFile(emlFile);
            MailInEvent mie = new MailInEvent(this, mmih);
            applicationEventPublisher.publishEvent(mie);
        } catch (Exception e) {
            log.error("Errore durante la pubblicazione dell'evento di ricezione mail {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private List<String> toStringAddress( List<Address> nerrorAddress ){
        if( nerrorAddress == null || nerrorAddress.isEmpty() ) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>(nerrorAddress.size());
        nerrorAddress.forEach(mail -> {
            String indirizzoMail = mail.toString();
            if( mail instanceof InternetAddress) {

                indirizzoMail = ((InternetAddress) mail).getAddress();
            }
            result.add(indirizzoMail);
        });
        return result;
    }
    private void closeFolder(String nerrorMail) {
        if( closeables.containsKey(nerrorMail) ) {
            Closeable closeable = closeables.get(nerrorMail);
            try {
                if(closeable!=null) {closeable.close();}
            } catch (IOException e) {
                log.warn("메일을 읽어오는 과정에서 오류가 발생했습니다. {}", nerrorMail, e);
            }
        }
    }

    private SearchTerm notSeenTerm(Flags supportedFlags, Folder folder) {

        return new FlagTerm(new Flags(Flags.Flag.SEEN), false);

    }

    @PreDestroy
    public void preDestroy() {
        if( closeables != null && !closeables.isEmpty() ) {

            closeables.forEach( (key, value) ->{

                try {
                    value.close();
                } catch (Exception e) {
                    log.warn("Errore nella chiusura del folder associato alla mail con indirizzo {}", key);
                }
            } );
        }
    }
}
