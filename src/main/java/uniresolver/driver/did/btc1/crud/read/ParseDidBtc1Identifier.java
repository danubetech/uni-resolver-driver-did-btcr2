package uniresolver.driver.did.btc1.crud.read;

import foundation.identity.did.DID;
import org.bitcoinj.base.Bech32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.did.btc1.Network;
import uniresolver.driver.did.btc1.appendix.Bech32EncodingAndDecoding;
import uniresolver.driver.did.btc1.crud.read.records.IdentifierComponents;

import java.util.Arrays;

public class ParseDidBtc1Identifier {

    private static final Logger log = LoggerFactory.getLogger(ParseDidBtc1Identifier.class);

    /*
     * 4.2.1 Parse did:btc1 Identifier
     */

    // See https://dcdpr.github.io/did-btc1/#parse-didbtc1-identifier
    public static IdentifierComponents parseDidBtc1Identifier(DID identifier) throws ResolutionException {

        if (identifier == null) throw new IllegalArgumentException("Identifier cannot be null");

        // 1. Set identifierComponents to an empty object.

        IdentifierComponents identifierComponents;
        Network identifierComponentsNetwork;
        Integer identifierComponentsVersion;
        String identifierComponentsHrp;
        byte[] identifierComponentsGenesisBytes;

        // 2. Using a colon (:) as the delimiter, split the identifier into an array of components.
        // 3. Set scheme to components[0].
        // 4. Set methodId to components[1].

        String scheme = DID.URI_SCHEME;
        String methodId = identifier.getMethodName();
        String methodSpecificId = identifier.getMethodSpecificId();
        String[] methodSpecificIdParts = methodSpecificId.split(":");

        // 5. The methodId MUST be the value btc1. If this requirement fails then a methodNotSupported error MUST be raised.

        if (! "btc1".equals(methodId)) {
            throw new ResolutionException(ResolutionException.ERROR_METHODNOTSUPPORTED, "Method not supported: " + methodId);
        }

        // 6. If the length of components equals 3, set identifierComponents.version to 1
        // and identifierComponents.network to mainnet. Set idBech32 to components[2].

        String version;
        String network;
        String idBech32;

        if (2 + methodSpecificIdParts.length == 3) {

            version = "1";
            network = "mainnet";
            idBech32 = methodSpecificIdParts[0];

        // 7. Else if length of components equals 4, check if components[2] can be cast to an
        // integer. If so, set identifierComponents.version to components[2] and
        // identifierComponents.network to mainnet. Otherwise, set identifierComponents.network
        // to components[2] and identifierComponents.version to 1. Set idBech32 to components[3].

        } else if (2 + methodSpecificIdParts.length == 4) {

            if (canBeConvertedToInteger(methodSpecificIdParts[0])) {
                version = methodSpecificIdParts[0];
                network = "mainnet";
            } else {
                version = "1";
                network = methodSpecificIdParts[0];
            }
            idBech32 = methodSpecificIdParts[1];

        // 8. Else if the length of components equals 5, set identifierComponents.version to
        // components[2], identifierComponents.network to components[3] and idBech32 to the components[4].

        } else if (2 + methodSpecificIdParts.length == 5) {

            version = methodSpecificIdParts[0];
            network = methodSpecificIdParts[1];
            idBech32 = methodSpecificIdParts[2];

        // 9. Else MUST raise InvalidDID error. There are an incorrect number of components to the identifier.

        } else {
            throw new ResolutionException(ResolutionException.ERROR_INVALIDDID, "Invalid number of components in method-specific identifier '" + methodSpecificId + "': " + methodSpecificIdParts.length);
        }

        // 10. Check the validity of the identifier components. The scheme MUST be the value did. The
        // identifierComponents.version MUST be convertible to a positive integer value. The
        // identifierComponents.network MUST be one of mainnet, signet, testnet, or regtest.
        // If any of these requirements fail then an invalidDid error MUST be raised.

        if (! canBeConvertedToPositiveInteger(version)) {
            throw new ResolutionException(ResolutionException.ERROR_INVALIDDID, "Invalid version (not positive integer): " + version);
        }
        if (! Arrays.asList(Network.stringValues()).contains(network)) {
            throw new ResolutionException(ResolutionException.ERROR_INVALIDDID, "Invalid network (not in " + Arrays.toString(Network.stringValues()) + "): " + network);
        }

        identifierComponentsVersion = Integer.parseInt(version);
        identifierComponentsNetwork = Network.valueOf(network);

        // 11. Decode idBech32 using the Bech32 algorithm to get decodeResult.

        Bech32.Bech32Data bech32Data = Bech32EncodingAndDecoding.bech32Decode(idBech32);

        // 12. Set identifierComponents.hrp to decodeResult.hrp.

        identifierComponentsHrp = bech32Data.hrp;

        // 13. Set identifierComponents.genesisBytes to decodeResult.value.

        identifierComponentsGenesisBytes = bech32Data.decode5to8();

        // 14. Return identifierComponents.

        identifierComponents = new IdentifierComponents(identifierComponentsNetwork, identifierComponentsVersion, identifierComponentsHrp, identifierComponentsGenesisBytes);

        if (log.isDebugEnabled()) log.debug("parseDidBtc1Identifier: " + identifierComponents);
        return identifierComponents;
    }

    /*
     * Helper methods
     */

    private static boolean canBeConvertedToInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static boolean canBeConvertedToPositiveInteger(String string) {
        try {
            return Integer.parseInt(string) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
