package com.payconiq;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import static junit.framework.TestCase.assertTrue;
import java.util.Base64;

public class PayconiqSignatureValidatorTest {

    private static final String SHA_256_WITH_RSA = "SHA256WithRSA";

    private static final Logger LOG = LoggerFactory.getLogger(PayconiqSignatureValidatorTest.class);

    private String keyFolderPath = "";

    private String getPublicKeyPath() {
        /**
         * Convert public key into a DER public key
         * $ openssl rsa -in private_key.pem -pubout -outform DER -out public_key.der
         */
        return keyFolderPath + "public_key.der";
    }

    private String getPrivateKeyPath() {
        /**
         * Convert private key into a DER private key
         * $ openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt
         */
        return keyFolderPath + "private_key.der";
    }


    @Test
    public void testSDDTransactionSignature() throws Exception {
        String privateKeyPath = getPrivateKeyPath();
        String publicKeyPath = getPublicKeyPath();

        String partnerId = "58961529445edf0001fbb2b3", senderId = "589618f198fff10001106fc7",
                senderIBAN = "NL91ABNA0417164300", currency = "EUR", amount = "10";

        StringBuilder builder = new StringBuilder();
        builder.append(partnerId).append(senderId).
                append(senderIBAN).append(currency).append(amount);

        getSignature(privateKeyPath, publicKeyPath, builder);
    }

    @Test
    public void testSCTransactionSignature() throws Exception {

        String privateKeyPath = getPrivateKeyPath();
        String publicKeyPath = getPublicKeyPath();

        String partnerId = "58961529445edf0001fbb2b3", senderId = "58961529445edf0001fbb2b3",
                senderIBAN = "NL55INGB0000000000", recipientId = "5896190598fff10001106fc8",
                recipientIBAN = "NL02ABNA0457180536", currency = "EUR", amount = "10";

        StringBuilder builder = new StringBuilder();
        builder.append(partnerId).append(senderId)
                .append(senderIBAN).append(recipientId)
                .append(recipientIBAN).append(currency).append(amount);

        getSignature(privateKeyPath, publicKeyPath, builder);
    }

    @Test
    public void testP2PTransactionSignature() throws Exception {

        String privateKeyPath = getPrivateKeyPath();
        String publicKeyPath = getPublicKeyPath();

        String partnerId = "58961529445edf0001fbb2b3", senderId = "589618f198fff10001106fc7",
                senderIBAN = "NL91ABNA0417164300", recipientId = "5896190598fff10001106fc8",
                recipientIBAN = "NL02ABNA0457180536", currency = "EUR", amount = "10";

        StringBuilder builder = new StringBuilder();
        builder.append(partnerId).append(senderId)
                .append(senderIBAN).append(recipientId)
                .append(recipientIBAN).append(currency).append(amount);

        getSignature(privateKeyPath, publicKeyPath, builder);
    }


    private void getSignature(String privateKeyPath, String publicKeyPath, StringBuilder builder) throws Exception {
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        String signature = generateSignature(builder.toString(), privateKey);

        PublicKey publicKey = getPublicKey(publicKeyPath);
        boolean validation = validateSignature(builder.toString(), signature, publicKey);
        assertTrue(validation);
    }

    /**
     * @param data         the concatenation of the information related to the transaction
     * @param signature    the signature that needs to be verified against a Public Key
     * @param rsaPublicKey the Public Key to verify the signature
     * @return
     * @throws Exception
     */
    private boolean validateSignature(String data, String signature, PublicKey rsaPublicKey) throws Exception {
        try {
            Signature sig = Signature.getInstance(SHA_256_WITH_RSA);
            sig.initVerify(rsaPublicKey);
            sig.update(data.getBytes("UTF-8"));
            return sig.verify(base64ToBytes((signature)));
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException ex) {
            LOG.error(ex.getMessage());
            throw new Exception(ex.getMessage(), ex);
        }
    }

    /**
     * @param data          the concatenation of the information related to the transaction
     * @param rsaPrivateKey the PrivateKey
     * @return a signed version in Base64 of the information related to the transaction
     * @throws Exception
     */
    public String generateSignature(String data, PrivateKey rsaPrivateKey) throws Exception {
        try {
            Signature sig = Signature.getInstance(SHA_256_WITH_RSA);
            sig.initSign(rsaPrivateKey);
            sig.update(stringToBytes(data));
            String signature = bytesToBase64(sig.sign());
            return signature;
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException ex) {
            LOG.error(ex.getMessage());
            throw new Exception(ex.getMessage(), ex);
        }
    }

    /**
     * @param path to the private key in DER format
     * @return a PrivateKey
     * @throws Exception
     */
    private PrivateKey getPrivateKey(String path) throws Exception {
        try {
            KeyFactory fact = KeyFactory.getInstance("RSA");
            byte[] privateKey = Files.readAllBytes(new File(path).toPath());
            PKCS8EncodedKeySpec keySpecPv = new PKCS8EncodedKeySpec(privateKey);
            PrivateKey rsaPrivateKey = fact.generatePrivate(keySpecPv);
            return rsaPrivateKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
            LOG.error(ex.getMessage());
            throw new Exception(ex.getMessage(), ex);
        }
    }

    /**
     * @param path to the public key in DER format
     * @return a PublicKey
     * @throws Exception
     */
    private PublicKey getPublicKey(String path) throws Exception {
        try {
            byte[] keyBytes = Files.readAllBytes(new File(path).toPath());
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(spec);
            return publicKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
            LOG.error(ex.getMessage());
            throw new Exception(ex.getMessage(), ex);
        }
    }

    private final static String STRING_REFERENCE_ENCODING = "UTF-8";

    public byte[] stringToBytes(String string) throws Exception {
        try {
            return string.getBytes(STRING_REFERENCE_ENCODING);
        } catch (UnsupportedEncodingException ex) {
            throw new Exception(ex.getMessage(),ex);
        }
    }

    private String bytesToBase64(byte[] bytes) {
        return  Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] base64ToBytes(String base64) {
        return Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
    }
}
