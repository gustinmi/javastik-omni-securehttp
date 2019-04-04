package com.gustinmi.cryptotest;

import static org.junit.Assert.*;
import javax.security.auth.x500.X500PrivateCredential;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.gustinmi.ssl.CreateKeyStores;


public class CertBuilderTest {

    @Before
    public void setUp() throws Exception {

        // Ensure bouncy castle provider is installed
        CipherAndProviderTest.testProvider("BC");

        // Ensure unlimited policy files present
        CipherAndProviderTest.testUnlimitedPolicyFiles();

    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testRootCredential() {


        try {
            final X500PrivateCredential createRootCredential = CreateKeyStores.createRootCredential();
            assertNotNull("Root credentials are empty", createRootCredential);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }


    }

}
