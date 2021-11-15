package org.subethamail.smtp.netty.auth.ext;

import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.netty.auth.UsernameAndPsdValidator;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple authentication validator
 * Don't use plain credentials on Production
 *
 * @author Reda Messoudi
 */
public class MemoryBaseNameAndPsdValidator implements UsernameAndPsdValidator {

//    private final String CREDENTIALS_LOGIN = "yrc@yanrc.net";
//    private final String CREDENTIALS_PASSWORD = "123456";
    private final Map<String, String> nameAndPsdMap = new HashMap();

    @Override
    public void login(String username, String password, SmtpSession context) throws LoginFailedException {
        System.out.printf(String.format("login:%s,psd:%s", username, password));
        if (password.equals(nameAndPsdMap.get(username))) {
        } else {
            System.err.println("Invalid authentication !");
            throw new LoginFailedException();
        }
    }

    @Override
    public boolean add(String userName, String password) {
        nameAndPsdMap.putIfAbsent(userName, password);
        return true;
    }
}
