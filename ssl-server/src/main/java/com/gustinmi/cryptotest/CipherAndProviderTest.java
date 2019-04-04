package com.gustinmi.cryptotest;

import java.security.*;
import java.util.Iterator;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

// import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CipherAndProviderTest 
{

	public static void main( String[] args ) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
		
		byte[] input = new byte[]{
				(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
				(byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
				(byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17 };
		
		System.out.println("input " + Utils.toHex(input, input.length));
		
		byte[] keyByte = new byte[]{
				(byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
				(byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
				(byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17 };
		
		SecretKeySpec key = new SecretKeySpec(keyByte, "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
		//Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding", "BC");
		
		
		byte[] cipherText = new byte[input.length];
		cipher.init(Cipher.ENCRYPT_MODE, key);
		
		int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
		ctLength += cipher.doFinal(cipherText, ctLength);
		
		System.out.println("cipher text" + Utils.toHex(cipherText, cipherText.length));
		
	}
	
	
    /** Capabilities of java crypto provider
     * @param args
     */
	public static void providerCapabilities( String[] args ) {
		
		Provider p = Security.getProvider("BC");
		Iterator<Object> it = p.keySet().iterator();
		while(it.hasNext()) {
			String s = (String) it.next();
			System.out.println(s);
		}
		
	}
	
	
    /** Provider precendence 
     * provider added in jre/lib/security java.security
    */
	public static void providerPrecendence( String[] args ) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
		
		Cipher cipher = Cipher.getInstance("Blowfish/ECB/NoPadding");
		System.out.println(cipher.getProvider());
		
		cipher = Cipher.getInstance("Blowfish/ECB/NoPadding", "BC");
		System.out.println(cipher.getProvider());
		
		
		
	}
	
    /**
     * 
     * provider added in jre/lib/security java.security
     * security.provider.11=org.bouncycastle.jce.provider.BouncyCastleProvider
     * @param provider BC
     */
    public static void testProvider(String provider) {
		
        // you can add provider Programatically
		//Security.addProvider(new BouncyCastleProvider());
		
		if (Security.getProvider(provider) == null) {
            throw new IllegalArgumentException(provider + " provider is not installed");
		}else {
			System.out.println(provider + " installed!");
		}
		
		
		
	}
	
    /**
     * Unlimited policy file provider (you download file from java website ) 
     */
    public static void testUnlimitedPolicyFiles() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
    	byte[] data = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
    	
    	SecretKey key64 = new SecretKeySpec(new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07}, "Blowfish");
    	Cipher c = Cipher.getInstance("Blowfish/ECB/NoPadding");
    	c.init(Cipher.ENCRYPT_MODE, key64);

        System.out.println( "64 bit test passed" );
        
        SecretKey key192 = new SecretKeySpec(
        		new byte[]{ 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 }
        		, "Blowfish");
        c.init(Cipher.ENCRYPT_MODE, key192);
        c.doFinal(data);
        
        System.out.println( "192 bit test passed" );
        
        System.out.println( "Tests completed" );
        
    }
}