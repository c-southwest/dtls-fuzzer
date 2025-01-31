package se.uu.it.dtlsfuzzer.components.sul.core.config;

import com.beust.jcommander.Parameter;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.Delegate;
import java.io.File;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;

import de.rub.nds.x509attacker.config.X509CertificateConfig;
import de.rub.nds.x509attacker.constants.X509NamedCurve;
import de.rub.nds.x509attacker.filesystem.CertificateBytes;
import de.rub.nds.x509attacker.filesystem.CertificateIo;

import javax.crypto.interfaces.DHPrivateKey;

/**
 * Implements arguments for configuring the certificate/key to use.
 * This can also be configured by changing the TLS-Attacker config file (set via -sulConfig).
 * Code is derived from {@link de.rub.nds.tlsattacker.core.config.delegate.CertificateDelegate}.
 */
@SuppressWarnings("deprecation")
public class CertificateKeyDelegate extends Delegate {
    @Parameter(names = "-cert", description = "PEM encoded certificate file")
    private String certificate;

    @Parameter(names = "-key", description = "PEM encoded private key")
    private String key;

    public CertificateKeyDelegate() {
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public void applyDelegate(Config config) {
        PrivateKey privateKey = null;
        if (key != null) {
            LOGGER.debug("Loading private key");
            privateKey = de.rub.nds.x509attacker.signatureengine.keyparsers.PemUtil.readPrivateKey(new File(key));
            adjustPrivateKey(config.getCertificateChainConfig().get(0), privateKey);
        }
        if (certificate != null) {
            if (privateKey == null) {
                LOGGER.warn("Certificate provided without chain");
            }
            LOGGER.debug("Loading certificate chain");
            try {
                List<CertificateBytes> byteList =
                        CertificateIo.readPemCertificateByteList(
                                new FileInputStream(new File(certificate)));
                config.setDefaultExplicitCertificateChain(byteList);
            } catch (Exception ex) {
                LOGGER.warn("Could not read certificate", ex);
            }
        }
    }
    private void adjustPrivateKey(X509CertificateConfig config, PrivateKey privateKey) {
        if (privateKey instanceof RSAPrivateKey) {
            RSAPrivateKey rsaKey = (RSAPrivateKey) privateKey;
            config.setRsaPrivateKey(rsaKey.getPrivateExponent());
            config.setRsaModulus(rsaKey.getModulus());
        } else if (privateKey instanceof DSAPrivateKey) {
            DSAPrivateKey dsaKey = (DSAPrivateKey) privateKey;
            config.setDsaGenerator(dsaKey.getParams().getG());
            config.setDsaPrimeP(dsaKey.getParams().getP());
            config.setDsaPrimeQ(dsaKey.getParams().getQ());
            config.setDsaPrivateKey(dsaKey.getX());
        } else if (privateKey instanceof DHPrivateKey) {
            DHPrivateKey dhKey = (DHPrivateKey) privateKey;
            config.setDhPrivateKey(dhKey.getX());
            config.setDhModulus(dhKey.getParams().getP());
            config.setDhGenerator(dhKey.getParams().getG());
        } else if (privateKey instanceof ECPrivateKey) {
            ECPrivateKey ecKey = (ECPrivateKey) privateKey;
            config.setEcPrivateKey(ecKey.getS());
            config.setDefaultSubjectNamedCurve(X509NamedCurve.getX509NamedCurve(ecKey));
        } else {
            throw new UnsupportedOperationException("This private key is not supported:" + key);
        }
    }
}
