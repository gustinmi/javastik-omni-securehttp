package com.gustinmi.cryptotest;

import org.junit.Before;
import org.junit.Test;


public class ProviderTest {

    @Before
    public void setUp() throws Exception {

        System.out.println("======== Testing provider");
        // Ensure bouncy castle provider is installed
        CipherAndProviderTest.testProvider("BC");

        System.out.println("======== Testing providerPrecendence");
        CipherAndProviderTest.providerPrecendence();

        System.out.println("======== Listing provider capabilities");
        // Provider capabilities
        CipherAndProviderTest.providerCapabilities("BC");

        System.out.println("======== Testing unlimited policy files");

        // Ensure unlimited policy files present
        CipherAndProviderTest.testUnlimitedPolicyFiles();

    }


    @Test
    public void testRootCredential() {


        System.out.println("======== ALL PASSED ");

    }

}
