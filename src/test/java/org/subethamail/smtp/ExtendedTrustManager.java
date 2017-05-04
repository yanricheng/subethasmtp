package org.subethamail.smtp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

public final class ExtendedTrustManager extends X509ExtendedTrustManager {

    private final X509TrustManager defaultTm; // cacerts
    private final X509TrustManager customTm;

    public ExtendedTrustManager(InputStream trustStoreInputStream, char[] trustStorePassword, boolean extend) {
        try {
            if (extend) {
                X509TrustManager defaultTm = null;
                {
                    TrustManagerFactory tmf = TrustManagerFactory
                            .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init((KeyStore) null);
                    for (TrustManager tm : tmf.getTrustManagers()) {
                        if (tm instanceof X509TrustManager) {
                            defaultTm = (X509TrustManager) tm;
                            break;
                        }
                    }
                }
                this.defaultTm = defaultTm;
            } else {
                defaultTm = null;
            }
            final KeyStore trustStore;
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(trustStoreInputStream, trustStorePassword);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            X509TrustManager customTm = null;
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    customTm = (X509TrustManager) tm;
                    break;
                }
            }
            this.customTm = customTm;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (defaultTm != null) {
            defaultTm.checkClientTrusted(chain, authType);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        try {
            customTm.checkServerTrusted(chain, authType);
        } catch (CertificateException e) {
            if (defaultTm != null) {
                defaultTm.checkServerTrusted(chain, authType);
            }
        }

    }

    @Override
    public final X509Certificate[] getAcceptedIssuers() {
        if (defaultTm != null) {
            return defaultTm.getAcceptedIssuers();
        } else
            return new X509Certificate[0];
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
        checkClientTrusted(chain, authType);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
            throws CertificateException {
        checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
        checkServerTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
            throws CertificateException {
        checkServerTrusted(chain, authType);
    }

    public static SSLContext createTlsContextWithExtendedTrustManager(InputStream trustStore, String trustStorePassword,
            boolean extend) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager trustManager = new ExtendedTrustManager(trustStore, trustStorePassword.toCharArray(), extend);
        TrustManager[] trustManagers = new TrustManager[] { trustManager };
        sslContext.init(null, trustManagers, new java.security.SecureRandom());
        return sslContext;
    }

    public static SSLContext createTlsContextWithAlwaysHappyExtendedTrustManager()
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager trustManager = new AlwaysHappyTrustManager();
        TrustManager[] trustManagers = new TrustManager[] { trustManager };
        sslContext.init(null, trustManagers, new java.security.SecureRandom());
        return sslContext;
    }

    public static class AlwaysHappyTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // don't throw exception
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // don't throw exception
        }

        @Override
        public final X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    public static class AlwaysHappyHostnameVerifier implements HostnameVerifier {
        @Override
        public final boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
