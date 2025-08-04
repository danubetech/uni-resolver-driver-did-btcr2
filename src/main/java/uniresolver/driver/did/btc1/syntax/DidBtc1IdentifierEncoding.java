package uniresolver.driver.did.btc1.syntax;

import foundation.identity.did.DID;
import foundation.identity.did.parser.ParserException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.crypto.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.appendix.Bech32mEncoding;
import uniresolver.driver.did.btc1.syntax.records.IdentifierComponents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class DidBtc1IdentifierEncoding {

    private static final Logger log = LoggerFactory.getLogger(DidBtc1IdentifierEncoding.class);

    /*
     * 3.2 did:btc1 Identifier Encoding
     */

    // See https://dcdpr.github.io/did-btc1/#didbtc1-identifier-encoding
    public static DID didBtc1IdentifierEncoding(String idType, Integer version, Object networkValue, byte[] genesisBytes) throws ResolutionException {

        // If idType is not a valid value per above, raise invalidDid error.

        if (! "key".equals(idType) && ! "external".equals(idType)) {
            throw new ResolutionException(ResolutionException.ERROR_INVALID_DID, "Invalid 'idType' value: " + idType);
        }

        // If version is greater than 1, raise invalidDid error.

        if (version > 1) {
            throw new ResolutionException(ResolutionException.ERROR_INVALID_DID, "Unsupported 'version' value: " + version);
        }

        // If network is not a valid value per above, raise invalidDid error.

        if (networkValue instanceof Network) networkValue = networkValue.toString();

        if (! (networkValue instanceof String networkValueString && Arrays.asList(Network.values()).contains(Network.valueOf(networkValueString))) && ! (networkValue instanceof Number)) {
            throw new ResolutionException(ResolutionException.ERROR_INVALID_DID, "Unsupported 'network' value: " + networkValue);
        }

        // if network is a number and is outside the range of 1-8, raise invalidDid error.

        if (networkValue instanceof Number networkValueNumber && (networkValueNumber.intValue() < 1 || networkValueNumber.intValue() > 8)) {
            throw new ResolutionException(ResolutionException.ERROR_INVALID_DID, "Unsupported 'network' number value: " + networkValueNumber);
        }

        // If idType is “key” and genesisBytes is not a valid compressed secp256k1 public key, raise invalidDid error.

        if ("key".equals(idType)) {
            try {
                ECKey ecKey = ECKey.fromPublicOnly(genesisBytes);
                if (! ecKey.isCompressed()) throw new IllegalArgumentException("Not compressed");
            } catch (Exception ex) {
                throw new ResolutionException("Genesis bytes " + Hex.encodeHexString(genesisBytes) + " are not a valid compressed secp256k1 public key: " + ex.getMessage(), ex);
            }
        }

        // Map idType to hrp from the following:

        String hrp = switch (idType) {
            case "key" -> "k";
            case "external" -> "x";
            default -> throw new ResolutionException(ResolutionException.ERROR_INVALID_DID, "Invalid 'idType' value in: " + idType);
        };

        // Create an empty nibbles numeric array.

        ByteArrayOutputStream nibbles = new ByteArrayOutputStream();

        // Set fCount equal to (version - 1) / 15, rounded down.

        Integer fCount = (version - 1) / 15;

        // Append hexadecimal F (decimal 15) to nibbles fCount times.

        for (int i=0; i<fCount; i++) nibbles.write((byte) 0x0f);

        // Append (version - 1) mod 15 to nibbles.

        nibbles.write((version - 1) % 15);

        // If network is a string, append the numeric value from the following map to nibbles:

        if (networkValue instanceof String networkValueString) {
            nibbles.write(Network.valueOf(networkValueString).toInt());
        }

        // If network is a number, append network + 7 to nibbles.

        if (networkValue instanceof Number networkValueNumber) {
            nibbles.write(networkValueNumber.intValue() + 7);
        }

        // If the number of entries in nibbles is odd, append 0.

        if (nibbles.size() % 2 == 1) {
            nibbles.write((byte) 0x00);
        }

        // Create a dataBytes byte array from nibbles

        byte[] nibblesBytes = nibbles.toByteArray();
        ByteArrayOutputStream dataBytes = new ByteArrayOutputStream();
        for (int i=0; i<nibblesBytes.length; i+=2) {
            dataBytes.write((nibblesBytes[i] << 4) + nibblesBytes[i+1]);
        }

        // Append genesisBytes to dataBytes.

        try {
            dataBytes.write(genesisBytes);
        } catch (IOException ex) {
            throw new ResolutionException(ResolutionException.ERROR_INVALID_DID, "Cannot write genesis bytes " + Hex.encodeHexString(genesisBytes) + ": " + ex.getMessage(), ex);
        }

        // Set identifier to “did:btc1:”.

        String identifier = "did:btc1:";

        // Pass hrp and dataBytes to the Bech32m Encoding algorithm, retrieving encodedString.

        String encodedString = Bech32mEncoding.bech32Encode(hrp, dataBytes.toByteArray());

        // Append encodedString to identifier.

        identifier += encodedString;

        // Return identifier.

        if (log.isDebugEnabled()) log.debug("didBtc1IdentifierEncoding: " + identifier);
        try {
            return DID.fromString(identifier);
        } catch (ParserException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    // See https://dcdpr.github.io/did-btc1/#didbtc1-identifier-encoding
    public static DID didBtc1IdentifierEncoding(IdentifierComponents identifierComponents) throws ResolutionException {

        return didBtc1IdentifierEncoding(identifierComponents.idType(), identifierComponents.version(), identifierComponents.network(), identifierComponents.genesisBytes());
    }
}
