package uniresolver.driver.did.btcr2.dataintegrity;

import com.apicatalog.multicodec.MulticodecDecoder;
import com.danubetech.dataintegrity.DataIntegrityProof;
import com.danubetech.dataintegrity.suites.DataIntegritySuite;
import com.danubetech.dataintegrity.verifier.DataIntegrityProofLdVerifier;
import com.danubetech.dataintegrity.verifier.LdVerifierRegistry;
import com.danubetech.keyformats.crypto.impl.secp256k1_ES256KS_PublicKeyVerifier;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.VerificationMethod;
import foundation.identity.jsonld.JsonLDDereferencer;
import foundation.identity.jsonld.JsonLDException;
import io.ipfs.multibase.Multibase;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.crypto.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btcr2.crud.update.jsonld.DIDUpdate;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class DataIntegrity {

    private static final Logger log = LoggerFactory.getLogger(DataIntegrity.class);

    public static boolean verifyProofAlgorithm(String mediaType, byte[] documentBytes, DataIntegritySuite cryptosuite, String expectedProofPurpose, /* TODO: extra, not in spec */ DIDUpdate update, DataIntegrityProof dataIntegrityProof, DIDDocument contemporaryDIDDocument) throws ResolutionException {

        VerificationMethod verificationMethod = VerificationMethod.fromJsonLDObject(JsonLDDereferencer.findByIdInJsonLdObject(contemporaryDIDDocument, dataIntegrityProof.getVerificationMethod(), null));
        byte[] publicKeyBytes = MulticodecDecoder.getInstance().decode(Multibase.decode(verificationMethod.getPublicKeyMultibase()));
        if (log.isDebugEnabled()) log.debug("Public key bytes: {}", Hex.encodeHexString(publicKeyBytes));

        DataIntegrityProofLdVerifier dataIntegrityProofLdVerifier = (DataIntegrityProofLdVerifier) LdVerifierRegistry.getLdVerifierByDataIntegritySuite(cryptosuite);
        dataIntegrityProofLdVerifier.setVerifier(new secp256k1_ES256KS_PublicKeyVerifier(ECKey.fromPublicOnly(publicKeyBytes)));

        boolean verificationResult;
        try {
            verificationResult = dataIntegrityProofLdVerifier.verify(update, dataIntegrityProof);
        } catch (IOException | GeneralSecurityException | JsonLDException ex) {
            throw new ResolutionException("Cannot verify update " + update + ": " + ex.getMessage(), ex);
        }

        if (log.isDebugEnabled()) log.debug("verifyProofAlgorithm for 'update' " + update + " and 'dataIntegrityProof' " + dataIntegrityProof + ": " + verificationResult);
        return verificationResult;
    }
}
