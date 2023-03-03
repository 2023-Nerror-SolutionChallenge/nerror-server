package com.gsc.nerrorserver.api.mail;

import javax.activation.DataSource;
import java.io.File;
import java.util.Date;
import java.util.List;

public class MimeMessageInfoHolder {
    private String from;
    private List<String> tos;
    private List<String> ccs;
    private List<String> bccs;
    private String oggettoMail;
    private String corpoMail;
    private String idMessaggioMail;
    private List<DataSource> allegatiMail;
    private File mailEmlFile;
    private Date mailReceivedDate;
    private Date mailSendDate;
    public MimeMessageInfoHolder(String from, List<String> tos, List<String> ccs, List<String> bccs, String oggettoMail,
                                 String corpoMail, String idMessaggioMail, List<javax.activation.DataSource> allegatiMail, File mailEmlFile, Date mailReceivedDate, Date mailSendDate) {
        super();
        this.from = from;
        this.tos = tos;
        this.ccs = ccs;
        this.bccs = bccs;
        this.oggettoMail = oggettoMail;
        this.corpoMail = corpoMail;
        this.idMessaggioMail = idMessaggioMail;
        this.allegatiMail = allegatiMail;
        this.mailEmlFile = mailEmlFile;
        this.mailReceivedDate = mailReceivedDate;
        this.mailSendDate = mailSendDate;

    }
    public MimeMessageInfoHolder() {
        super();
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getTos() {
        return tos;
    }

    public void setTos(List<String> tos) {
        this.tos = tos;
    }

    public List<String> getCcs() {
        return ccs;
    }

    public void setCcs(List<String> ccs) {
        this.ccs = ccs;
    }

    public List<String> getBccs() {
        return bccs;
    }

    public void setBccs(List<String> bccs) {
        this.bccs = bccs;
    }

    public String getOggettoMail() {
        return oggettoMail;
    }

    public void setOggettoMail(String oggettoMail) {
        this.oggettoMail = oggettoMail;
    }

    public String getCorpoMail() {
        return corpoMail;
    }

    public void setCorpoMail(String corpoMail) {
        this.corpoMail = corpoMail;
    }

    public String getIdMessaggioMail() {
        return idMessaggioMail;
    }

    public void setIdMessaggioMail(String idMessaggioMail) {
        this.idMessaggioMail = idMessaggioMail;
    }

    public List<javax.activation.DataSource> getAllegatiMail() {
        return allegatiMail;
    }

    public void setAllegatiMail(List<javax.activation.DataSource> allegatiMail) {
        this.allegatiMail = allegatiMail;
    }

    public File getMailEmlFile() {
        return mailEmlFile;
    }
    public void setMailEmlFile(File mailEmlFile) {
        this.mailEmlFile = mailEmlFile;
    }
    public Date getMailReceivedDate() {
        return mailReceivedDate;
    }
    public void setMailReceivedDate(Date mailReceivedDate) {
        this.mailReceivedDate = mailReceivedDate;
    }
    public Date getMailSendDate() {
        return mailSendDate;
    }
    public void setMailSendDate(Date mailSendDate) {
        this.mailSendDate = mailSendDate;
    }


}
