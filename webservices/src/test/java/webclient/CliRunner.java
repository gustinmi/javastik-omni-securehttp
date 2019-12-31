package webclient;

import http.ClientSettings;
import http.GenericClient;
import http.GenericClient.HttpRequestType;

public class CliRunner {

    // for command line execution
    
    public static void main(String[] args) throws Exception {
        
        // TEST POST : Connecting to non SSL webservice url
        
        final ClientSettings cs = new ClientSettings();
        cs.setServerName("http://localhost:8088/mockAreaServiceSOAP");
                
        final GenericClient client = GenericClient.instance;
        client.configure(cs);
        
        final String xmlInput = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:are=\"http://tempuri.org/AreaService/\"> <soapenv:Header/> <soapenv:Body> <are:parameters> <width>1</width> <height>1</height> </are:parameters> </soapenv:Body> </soapenv:Envelope>";
        System.out.println("Sending request : " + xmlInput);
        
        final StringBuilder response = client.createPostRequest(HttpRequestType.POST, xmlInput, cs.getServerName());
        System.out.println("Sucessfully issued request.");
        System.out.println(response.toString());
        
        // TEST POST : Connecting to SSL
        
        final ClientSettings cs1 = new ClientSettings();
        cs1.setServerName("https://localhost:8443/mockAreaServiceSOAP");
        cs1.setKeystoreLocation("C:\\Program Files (x86)\\Java\\jdk1.7.0_67\\bin\\.keystore");
        cs1.setKeystorePassword("pa$$word1.");
                
        final GenericClient client1 = GenericClient.instance;
        client1.configure(cs1);
        
        final String xmlInput1 = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:are=\"http://tempuri.org/AreaService/\"> <soapenv:Header/> <soapenv:Body> <are:parameters> <width>1</width> <height>1</height> </are:parameters> </soapenv:Body> </soapenv:Envelope>";
        System.out.println("Sending request : " + xmlInput1);
        
        final StringBuilder response1 = client.createPostRequest(HttpRequestType.POST, xmlInput1, cs1.getServerName());
        System.out.println("Sucessfully SSL issued request.");
        System.out.println(response1.toString());
        
        
        // TEST GET WITH PROXY 
        
        final ClientSettings csProxyGet = new ClientSettings();
        csProxyGet.setServerName("http://www.google.si");
        
        final GenericClient client3 = GenericClient.instance;
        client3.configure(csProxyGet);
        
        final StringBuilder response3 = client3.createPostRequest(HttpRequestType.GET, null, csProxyGet.getServerName());
        System.out.println("Sucessfully issued request.");
        System.out.println(response3.toString());
        
        
        

    }

}