package org.subethamail.smtp.netty.mail;

import java.util.ArrayList;
import java.util.List;

public class Mail {
    private String fromAddress;
    private List<String> toAddress = new ArrayList<>();

    public Mail() {

    }

    public Mail(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public List<String> getToAddress() {
        return toAddress;
    }

    public void setToAddress(List<String> toAddress) {
        this.toAddress = toAddress;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
}
