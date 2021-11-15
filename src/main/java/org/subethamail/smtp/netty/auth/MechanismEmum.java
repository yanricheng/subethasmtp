package org.subethamail.smtp.netty.auth;

public enum MechanismEmum {
    LOGIN,PLAIN,OTHER;

    public static MechanismEmum byName(String name) {
        for (MechanismEmum x : MechanismEmum.values()) {
            if (x.name().equals(name)) {
                return x;
            }
        }
        return OTHER;
    }

}
