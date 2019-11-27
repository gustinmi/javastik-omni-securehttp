package com.gustinmi.cryptotest;

import java.security.Principal;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;

public class Validators {

    /**
     * Check that the principal we have been given is for the end entity.
     */
    public static boolean isEndEntity(SSLSession session, String peerName) throws SSLPeerUnverifiedException {
        final Principal id = session.getPeerPrincipal();
        if (id instanceof X500Principal) {
            final X500Principal x500 = (X500Principal) id;
            return x500.getName().equals(peerName);
        }
        return false;
    }

    /**
     * Verifier to check host has identified itself using "Test CA Certificate".
     */
    public static class HostnameValidator implements HostnameVerifier {

        private final String cnString;

        public HostnameValidator(String cnString) {
            super();
            this.cnString = cnString;
        }

        @Override
        public boolean verify(String hostName, SSLSession session) {
            try {
                X500Principal hostID = (X500Principal) session.getPeerPrincipal();

                return hostID.getName().equals(this.cnString);
            } catch (Exception e) {
                return false;
            }
        }
    }

}
