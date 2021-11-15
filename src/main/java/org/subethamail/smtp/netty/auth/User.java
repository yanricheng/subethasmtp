package org.subethamail.smtp.netty.auth;

import java.io.Serializable;

public class User implements Serializable {
    private String userName;
    private String password;
    private MechanismEmum authMechanism;

    public User() {
    }

    public User(String userName, String password, MechanismEmum authMechanism) {
        this.userName = userName;
        this.password = password;
        this.authMechanism = authMechanism;
    }

    public MechanismEmum getAuthMechanism() {
        return authMechanism;
    }

    public void setAuthMechanism(MechanismEmum authMechanism) {
        this.authMechanism = authMechanism;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
