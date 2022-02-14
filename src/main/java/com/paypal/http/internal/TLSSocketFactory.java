package com.paypal.http.internal;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class TLSSocketFactory extends SSLSocketFactory {

    private SSLSocketFactory mInternalSSLSocketFactory;

    public TLSSocketFactory() throws SSLException {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null); // use system security providers
            mInternalSSLSocketFactory = sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException ex) {
            throw new SSLException(ex.getMessage());
        } catch (KeyManagementException e) {
            throw new SSLException(e.getMessage());
        }
    }

    /**
     * @see <a href="http://developer.android.com/training/articles/security-ssl.html#UnknownCa">Android Documentation</a>
     */
    public TLSSocketFactory(InputStream certificateStream) throws SSLException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            Collection<? extends Certificate> certificates =
                    cf.generateCertificates(certificateStream);
            for (Certificate cert : certificates) {
                if (cert instanceof X509Certificate) {
                    String subject = ((X509Certificate) cert).getSubjectDN().getName();
                    keyStore.setCertificateEntry(subject, cert);
                }
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            mInternalSSLSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new SSLException(e.getMessage());
        } finally {
            try {
                certificateStream.close();
            } catch (IOException ignored) {} catch (NullPointerException ignored) {}
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return mInternalSSLSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return mInternalSSLSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose)
            throws IOException {
        return enableTLSOnSocket(mInternalSSLSocketFactory.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return enableTLSOnSocket(mInternalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException {
        return enableTLSOnSocket(
                mInternalSSLSocketFactory.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTLSOnSocket(mInternalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
            int localPort) throws IOException {
        return enableTLSOnSocket(
                mInternalSSLSocketFactory.createSocket(address, port, localAddress, localPort));
    }

    private Socket enableTLSOnSocket(Socket socket) {
        if (socket instanceof SSLSocket) {
            ArrayList<String> supportedProtocols =
                    new ArrayList<String>(Arrays.asList(((SSLSocket) socket).getSupportedProtocols()));
            supportedProtocols.retainAll(Arrays.asList("TLSv1.2", "TLSv1.1", "TLSv1"));

            ((SSLSocket) socket).setEnabledProtocols(supportedProtocols.toArray(
                    new String[supportedProtocols.size()]));
        }

        return socket;
    }
}
