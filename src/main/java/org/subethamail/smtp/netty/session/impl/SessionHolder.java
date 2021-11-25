package org.subethamail.smtp.netty.session.impl;

import org.subethamail.smtp.netty.session.SmtpSession;

import java.util.concurrent.ConcurrentHashMap;

public class SessionHolder {
    private static final ConcurrentHashMap<String, SmtpSession> sessionMap = new ConcurrentHashMap<>();

    public static SmtpSession get(String sessionId) {
        return sessionMap.get(sessionId);
    }

    public static void put(String id, SmtpSession session) {
        sessionMap.put(id, session);
    }

    public static void remove(String id) {
        sessionMap.remove(id);
    }
}
