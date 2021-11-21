package org.subethamail.smtp.netty.mail;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Mail {
    private String fromAddress;
    private List<String> toAddress = new ArrayList<>();
    private final StringBuilder data = new StringBuilder();
    private final ByteArrayOutputStream dataByteOutStream = new ByteArrayOutputStream();

    public Mail() {

    }

    public Mail(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public ByteArrayOutputStream getDataByteOutStream() {
        return dataByteOutStream;
    }

    public StringBuilder getData() {
        return data;
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
