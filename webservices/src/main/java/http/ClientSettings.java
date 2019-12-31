package http;

/** Dummy settings holder. 
 * @author HP
 *
 */
public final class ClientSettings {
    
    public static enum KeystoreType{
        FILE,
        JAR
    }
    
    private String keystoreLocation;
    private String keystorePassword;
    private String keystoreCertificatePassword;
    private String serverName;
    
    private KeystoreType type = KeystoreType.FILE;
    
    //private String httpContentType = "Content-Type: application/soap+xml;charset=UTF-8"; 

    public String getKeystoreLocation() {
        return this.keystoreLocation;
    }

    public void setKeystoreLocation(String keystoreLocation) {
        this.keystoreLocation = keystoreLocation;
    }

    public String getKeystorePassword() {
        return this.keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getKeystoreCertificatePassword() {
        return this.keystoreCertificatePassword;
    }

    public void setKeystoreCertificatePassword(String keystoreCertificatePassword) {
        this.keystoreCertificatePassword = keystoreCertificatePassword;
    }

    public String getServerName() {
        return this.serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public KeystoreType getType() {
        return this.type;
    }

    public void setType(KeystoreType type) {
        this.type = type;
    }
    


            
}