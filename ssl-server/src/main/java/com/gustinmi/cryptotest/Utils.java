package com.gustinmi.cryptotest;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.x500.X500Principal;
import javax.security.auth.x500.X500PrivateCredential;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;

@SuppressWarnings("deprecation")
public class Utils {

    public static final boolean INFO_ENEABLED = true;
    public static final boolean SYSOUT_ENABLED = true;

	private static final String BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;

    // for base64 encoding
	static final String digits = "0123456789abcdef";

    // http server data 
    public static final String HOST = "localhost";
    public static final int PORT_NO = 9020;

    // strings

    public static String ROOT_ALIAS = "root";
    public static String INTERMEDIATE_ALIAS = "intermediate";
    public static String END_ENTITY_ALIAS = "end";

	public static final String SERVER_NAME = "server";
    public static final char[] SERVER_PASSWORD = "p".toCharArray();

	public static final String CLIENT_NAME = "client";
    public static final char[] CLIENT_PASSWORD = "p".toCharArray();

	public static final String TRUST_STORE_NAME = "trustStore";
    public static final char[] TRUST_STORE_PASSWORD = "p".toCharArray();

    // JKS and cert expiration
	private static final int VALIDITY_PERIOD = 364 * 24 * 60 * 60 * 1000; // year

	/**
	 * Gets the logger for caller class
	 * 
	 * @return
	 */
	public static Logger loggerForThisClass() {
		// We use the third stack element; second is this method, first is getStackTrace()
		final StackTraceElement myCaller = Thread.currentThread().getStackTrace()[2];
		return Logger.getLogger(myCaller.getClassName());
	}

	private static X500NameBuilder createStdBuilderSubject(String cn) {
		final X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
		builder.addRDN(BCStyle.CN, cn);
		// builder.addRDN(BCStyle.E, "gustinmi@gmail.com");
		return builder;
	}

    /**
     * Generate a sample V1 certificate to use as a CA root certificate
     */
    private static X509Certificate generateRootCertBuilder(KeyPair pair) throws Exception {

		// distinguished name table.
        final X500NameBuilder builderIssuer = createStdBuilderSubject("MoneyKing CA Root Certificate Issuer");
        final X500NameBuilder builderSubject = createStdBuilderSubject("MoneyKing Root Subject");
		final ContentSigner sigGen = new JcaContentSignerBuilder("SHA256withRSA").setProvider(BC).build(pair.getPrivate());
        final BigInteger serialNum = createSerialNum();
		final X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
		        builderIssuer.build(), // x500 name issuer
                serialNum, // serialNumber
		        new Date(System.currentTimeMillis()), // startDate
		        new Date(System.currentTimeMillis() + VALIDITY_PERIOD), // expiryDate
		        builderSubject.build(), // x500 name subject
		        pair.getPublic());

		final X509Certificate cert = new JcaX509CertificateConverter().setProvider(BC).getCertificate(certGen.build(sigGen));
		return cert;
	}

	private static ASN1Sequence fromKey(PublicKey pubKey) throws InvalidKeyException {
        try {
			SubjectPublicKeyInfo info = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(pubKey.getEncoded()).readObject());

			return (ASN1Sequence) new AuthorityKeyIdentifier(info).toASN1Object();
		} catch (Exception e) {
			throw new InvalidKeyException("can't process key: " + e);
		}
	}

    //TODO need to change this. There is problem with key usage
    private static X509Certificate generateIntermediateCertWithBuilder(PublicKey pubKey, PrivateKey caPrivKey, X509Certificate caCert) throws Exception {

        final X500NameBuilder builderSubject = createStdBuilderSubject("MoneyKing Division Mars Intermediate Certificate");
        final BigInteger serialNum = createSerialNum();
		final X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
                caCert,
                serialNum,
				new Date(System.currentTimeMillis()),
				new Date(System.currentTimeMillis() + VALIDITY_PERIOD),
		        builderSubject.build(),
		        pubKey);

        // Add Extensions
		certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, fromKey(pubKey));

		final ContentSigner sigGen = new JcaContentSignerBuilder("SHA256withRSA").setProvider(BC).build(caPrivKey);
		final X509Certificate cert = new JcaX509CertificateConverter().setProvider(BC).getCertificate(certGen.build(sigGen));

		return cert;
	}

	/**
	 * Generate a sample V3 certificate to use as an intermediate CA certificate
	 */
    private static X509Certificate generateIntermediateCert(PublicKey intKey, PrivateKey caKey, X509Certificate caCert) throws Exception {
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        final BigInteger serialNum = createSerialNum();
        certGen.setSerialNumber(serialNum);
		certGen.setIssuerDN(caCert.getSubjectX500Principal());
		certGen.setNotBefore(new Date(System.currentTimeMillis()));
		certGen.setNotAfter(new Date(System.currentTimeMillis() + VALIDITY_PERIOD));
        certGen.setSubjectDN(new X500Principal("CN=MoneyKing Division Mars Intermediate Certificate"));
		certGen.setPublicKey(intKey);
		certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

		certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
		certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(0));
		certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.cRLSign));

		return certGen.generateX509Certificate(caKey, "BC");
	}

	/**
	 * Generate a sample V3 certificate to use as an end entity certificate
	 */
    private static X509Certificate generateEndEntityCert(PublicKey entityKey, PrivateKey caKey, X509Certificate caCert) throws Exception {
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        final BigInteger serialNum = createSerialNum();
        certGen.setSerialNumber(serialNum);
		certGen.setIssuerDN(caCert.getSubjectX500Principal());
		certGen.setNotBefore(new Date(System.currentTimeMillis()));
		certGen.setNotAfter(new Date(System.currentTimeMillis() + VALIDITY_PERIOD));
        certGen.setSubjectDN(new X500Principal("CN=Mickey Mouse End Certificate"));
		certGen.setPublicKey(entityKey);
		certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

		certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
		// certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(entityKey));
		certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
		certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

		return certGen.generateX509Certificate(caKey, "BC");
	}

	/**
	 * Generate a X500PrivateCredential for the root entity.
	 */
	public static X500PrivateCredential createRootCredential() throws Exception {
        final KeyPair rootPair = generateRSAKeyPair();
		final X509Certificate rootCert = generateRootCertBuilder(rootPair);
		return new X500PrivateCredential(rootCert, rootPair.getPrivate(), ROOT_ALIAS);
	}

	/**
	 * Generate a X500PrivateCredential for the intermediate entity.
	 */
	public static X500PrivateCredential createIntermediateCredential(PrivateKey caKey, X509Certificate caCert) throws Exception {
		KeyPair interPair = generateRSAKeyPair();
        X509Certificate interCert = generateIntermediateCert(interPair.getPublic(), caKey, caCert);
		return new X500PrivateCredential(interCert, interPair.getPrivate(), INTERMEDIATE_ALIAS);
	}

	/**
	 * Generate a X500PrivateCredential for the end entity.
	 */
	public static X500PrivateCredential createEndEntityCredential(PrivateKey caKey, X509Certificate caCert) throws Exception {
		KeyPair endPair = generateRSAKeyPair();
		X509Certificate endCert = generateEndEntityCert(endPair.getPublic(), caKey, caCert);
		return new X500PrivateCredential(endCert, endPair.getPrivate(), END_ENTITY_ALIAS);
	}

	private static class FixedRand extends SecureRandom {
		private static final long serialVersionUID = 1L;
		MessageDigest sha;
		byte[] state;

		FixedRand() {
			try {
				this.sha = MessageDigest.getInstance("SHA-1");
				this.state = sha.digest();
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("can't find SHA-1!");
			}
		}

		@Override
		public void nextBytes(byte[] bytes) {
			int off = 0;

			sha.update(state);

			while (off < bytes.length) {
				state = sha.digest();

				if (bytes.length - off > state.length) {
					System.arraycopy(state, 0, bytes, off, state.length);
				} else {
					System.arraycopy(state, 0, bytes, off, bytes.length - off);
				}

				off += state.length;

				sha.update(state);
			}
		}
	}

	/**
	 * Return a SecureRandom which produces the same value. <b>This is for
	 * testing only!</b>
	 * 
	 * @return a fixed random
	 */
    static SecureRandom createFixedRandom() {
		return new FixedRand();
	}

	/**
	 * Create a random 1024 bit RSA key pair
	 */
	public static KeyPair generateRSAKeyPair() throws Exception {
		KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", "BC");
		kpGen.initialize(1024, new SecureRandom());
		return kpGen.generateKeyPair();
	}

	/**
	 * Create a key for use with AES.
	 * 
	 * @param bitLength
	 * @param random
	 * @return an AES key.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
    static SecretKey createKeyForAES(int bitLength, SecureRandom random) throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyGenerator generator = KeyGenerator.getInstance("AES", "BC");
		generator.init(256, random);
		return generator.generateKey();
	}

	/**
	 * Create an IV suitable for using with AES in CTR mode.
	 * <p>
	 * The IV will be composed of 4 bytes of message number, 4 bytes of random
	 * data, and a counter of 8 bytes.
	 * 
	 * @param messageNumber
	 *            the number of the message.
	 * @param random
	 *            a source of randomness
	 * @return an initialised IvParameterSpec
	 */
	public static IvParameterSpec createCtrIvForAES(int messageNumber, SecureRandom random) {
		byte[] ivBytes = new byte[16];

		// initially randomize

		random.nextBytes(ivBytes);

		// set the message number bytes

		ivBytes[0] = (byte) (messageNumber >> 24);
		ivBytes[1] = (byte) (messageNumber >> 16);
		ivBytes[2] = (byte) (messageNumber >> 8);
		ivBytes[3] = (byte) (messageNumber >> 0);

		// set the counter bytes to 1

		for (int i = 0; i != 7; i++) {
			ivBytes[8 + i] = 0;
		}

		ivBytes[15] = 1;

		return new IvParameterSpec(ivBytes);
	}

	/**
	 * Convert a byte array of 8 bit characters into a String.
	 * 
	 * @param bytes
	 *            the array containing the characters
	 * @param length
	 *            the number of bytes to process
	 * @return a String representation of bytes
	 */
	public static String toString(byte[] bytes, int length) {
		char[] chars = new char[length];

		for (int i = 0; i != chars.length; i++) {
			chars[i] = (char) (bytes[i] & 0xff);
		}

		return new String(chars);
	}

	public static byte[] streamToByteArray(InputStream inputStream) {

		byte[] buffer = new byte[1024];
		int length = 0;
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
			while ((length = inputStream.read(buffer)) != -1) {
				baos.write(buffer, 0, length);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			System.err.print(e.getMessage());
		}
		throw new IllegalStateException("Napaka pri stream serializaciji!");
	}

	public static byte[] fileToStream(File f) {

		try (final InputStream is = new FileInputStream(f)) {

			return Utils.streamToByteArray(is);

		} catch (FileNotFoundException e) {
			System.err.print(e.getMessage());
		} catch (IOException e) {
			System.err.print(e.getMessage());
		}

		throw new IllegalStateException("Napaka pri branju datoteke v stream!");

	}

	public static String fileToHex(String name) {
			
		byte[] contents = fileToStream(new File(name));
		return toHex(contents, contents.length);

	}

	/**
	 * Convert a byte array of 8 bit characters into a String.
	 * 
	 * @param bytes
	 *            the array containing the characters
	 * @return a String representation of bytes
	 */
	public static String toString(byte[] bytes) {
		return toString(bytes, bytes.length);
	}

	/**
	 * Convert the passed in String to a byte array by taking the bottom 8 bits
	 * of each character it contains.
	 * 
	 * @param string
	 *            the string to be converted
	 * @return a byte array representation
	 */
	public static byte[] toByteArray(String string) {
		byte[] bytes = new byte[string.length()];
		char[] chars = string.toCharArray();

		for (int i = 0; i != chars.length; i++) {
			bytes[i] = (byte) chars[i];
		}

		return bytes;
	}

	public static String toHex(byte[] data, int len) {

		StringBuffer buf = new StringBuffer();

		for (int i = 0; i != len; i++) {
			int v = data[i] & 0xff;
			buf.append(digits.charAt(v >> 4));
			buf.append(digits.charAt(v & 0xf));

		}

		return buf.toString();

	}

    public static BigInteger createSerialNum() {

        final BigInteger maxLimit = new BigInteger("5000000000000");
        final BigInteger minLimit = new BigInteger("25000000000");
        final BigInteger bigInteger = maxLimit.subtract(minLimit);
        final Random randNum = new Random();
        int len = maxLimit.bitLength();

        BigInteger biResult = new BigInteger(len, randNum);
        if (biResult.compareTo(minLimit) < 0) biResult = biResult.add(minLimit);
        if (biResult.compareTo(bigInteger) >= 0) biResult = biResult.mod(bigInteger).add(minLimit);

        return biResult;

    }


}
