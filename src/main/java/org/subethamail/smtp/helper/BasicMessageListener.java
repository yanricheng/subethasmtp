package org.subethamail.smtp.helper;

public interface BasicMessageListener {
    
    void messageArrived(String from, String to, byte[] data);

}
