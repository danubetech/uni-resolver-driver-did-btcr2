package uniresolver.driver.did.btcr2.tls;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;

public class Tls {

    public static SSLSocketFactory getSslSocketFactory(String certString) throws IOException {

        try {

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            byte[] decoded = Base64.getDecoder().decode(certString.trim().getBytes(StandardCharsets.UTF_8));

            Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(decoded));

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca-cert", certificate);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, trustManagerFactory.getTrustManagers(), null);
            return context.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | KeyManagementException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }
}
