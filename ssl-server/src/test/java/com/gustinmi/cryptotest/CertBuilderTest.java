package com.gustinmi.cryptotest;

import static org.junit.Assert.*;
import javax.security.auth.x500.X500PrivateCredential;
import org.junit.Before;
import org.junit.Test;
import com.gustinmi.ssl.CreateKeyStores;


public class CertBuilderTest {

    @Before
    public void setUp() throws Exception {

    }


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

    @Test
    public void testCreateAllCredentials() {

        try {
            CreateKeyStores.createAll();

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

}
