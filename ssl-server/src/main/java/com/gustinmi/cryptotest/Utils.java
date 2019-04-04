package com.gustinmi.cryptotest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
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
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
//import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

@SuppressWarnings("deprecation")
public class Utils {

	private static final String BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;

	static final String digits = "0123456789abcdef";

	public static String ROOT_ALIAS = "root";
	public static String INTERMEDIATE_ALIAS = "intermediate";
	public static String END_ENTITY_ALIAS = "end";

	/**
	 * Names and passwords for the key store entries we need.
	 */
	public static final String SERVER_NAME = "server";
	public static final char[] SERVER_PASSWORD = "serverPassword".toCharArray();

	public static final String CLIENT_NAME = "client";
	public static final char[] CLIENT_PASSWORD = "clientPassword".toCharArray();

	public static final String TRUST_STORE_NAME = "trustStore";
	public static final char[] TRUST_STORE_PASSWORD = "trustPassword".toCharArray();

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

	/**
	 * Generate a sample V1 certificate to use as a CA root certificate
	 */

	public static X509Certificate generateRootCert(KeyPair pair) throws Exception {
		X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
		certGen.setSerialNumber(BigInteger.valueOf(1));
		certGen.setIssuerDN(new X500Principal("CN=TestIssuer CA Root Certificate"));
		certGen.setNotBefore(new Date(System.currentTimeMillis()));
		certGen.setNotAfter(new Date(System.currentTimeMillis() + VALIDITY_PERIOD));
		certGen.setSubjectDN(new X500Principal("CN=TestSubject CA Root Certificate"));
		certGen.setPublicKey(pair.getPublic());
		certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");
		return certGen.generateX509Certificate(pair.getPrivate(), "BC");
	}

	private static X500NameBuilder createStdBuilderIssuer() {
		final X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
		builder.addRDN(BCStyle.CN, "TestIssuer CA Root Certificate");
		return builder;
	}

	private static X500NameBuilder createStdBuilderSubject(String cn) {
		final X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
		builder.addRDN(BCStyle.CN, cn);
		// builder.addRDN(BCStyle.E, "gustinmi@gmail.com");
		return builder;
	}

	public static X509Certificate generateRootCertBuilder(KeyPair pair) throws Exception {

		// distinguished name table.
		final X500NameBuilder builderIssuer = createStdBuilderIssuer();
		final X500NameBuilder builderSubject = createStdBuilderSubject("TestSubject Certificate");
		final ContentSigner sigGen = new JcaContentSignerBuilder("SHA256withRSA").setProvider(BC).build(pair.getPrivate());
		
		final X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
		        builderIssuer.build(), // x500 name issuer
				BigInteger.valueOf(1), // serialNumber
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

	public static X509Certificate generateIntermediateCert1(PublicKey pubKey, PrivateKey caPrivKey, X509Certificate caCert) throws Exception {

		final X500NameBuilder builderSubject = createStdBuilderSubject("Test Intermediate Certificate");

		final X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
				caCert, BigInteger.valueOf(1),
				new Date(System.currentTimeMillis()),
				new Date(System.currentTimeMillis() + VALIDITY_PERIOD),
		        builderSubject.build(), pubKey);

		certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, fromKey(pubKey));

		// Extensions.

		final ContentSigner sigGen = new JcaContentSignerBuilder("SHA256withRSA").setProvider(BC).build(caPrivKey);
		final X509Certificate cert = new JcaX509CertificateConverter().setProvider(BC).getCertificate(certGen.build(sigGen));

		return cert;
	}

	/**
	 * Generate a sample V3 certificate to use as an intermediate CA certificate
	 */
	public static X509Certificate generateIntermediateCert(PublicKey intKey, PrivateKey caKey, X509Certificate caCert) throws Exception {
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(1));
		certGen.setIssuerDN(caCert.getSubjectX500Principal());
		certGen.setNotBefore(new Date(System.currentTimeMillis()));
		certGen.setNotAfter(new Date(System.currentTimeMillis() + VALIDITY_PERIOD));
		certGen.setSubjectDN(new X500Principal("CN=Test Intermediate Certificate"));
		certGen.setPublicKey(intKey);
		certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

		certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
		
		//certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(intKey))
		
		
		certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(0));
		certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.cRLSign));

		return certGen.generateX509Certificate(caKey, "BC");
	}

	/**
	 * Generate a sample V3 certificate to use as an end entity certificate
	 */
	public static X509Certificate generateEndEntityCert(PublicKey entityKey, PrivateKey caKey, X509Certificate caCert) throws Exception {
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(1));
		certGen.setIssuerDN(caCert.getSubjectX500Principal());
		certGen.setNotBefore(new Date(System.currentTimeMillis()));
		certGen.setNotAfter(new Date(System.currentTimeMillis() + VALIDITY_PERIOD));
		certGen.setSubjectDN(new X500Principal("CN=Test Client End Certificate"));
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
		KeyPair rootPair = generateRSAKeyPair();
		// X509Certificate rootCert = generateRootCert(rootPair);
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
	public static SecureRandom createFixedRandom() {
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
	public static SecretKey createKeyForAES(int bitLength, SecureRandom random) throws NoSuchAlgorithmException, NoSuchProviderException {
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
	
	public static void writeBase64(byte[] bytes) throws IOException {

		// byte[] bytes = null;
		// bytes = Base64.getEncoder().encode(bytes);
		// Files.write(Paths.get(path), bytes);

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

}
