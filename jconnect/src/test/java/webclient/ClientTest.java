package webclient;

import static org.junit.Assert.*;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import jconnect.HttpClient;
import jconnect.HttpProtocol;
import jconnect.UrlBuilder;
import jconnect.WebAr.HTTPS_OPTS;
import jconnect.WebAr.Response;
import jconnect.WebAr.TimeoutDefs;


public class ClientTest {

    public static final Logger Log = Logger.getLogger(HttpProtocol.class.getCanonicalName());

    HttpClient instance;

    @Before
    public void setUp() throws IllegalStateException, FileNotFoundException {
        instance = new HttpClient(HTTPS_OPTS.HTTPS_DEFAULT_PUBLIC_CA, null, new TimeoutDefs(), false, new InetSocketAddress("proxy.gov.si", 80), null, null);
    }

    @After
    public void tearDown() throws Exception {
        instance = null;
    }

    @Test
    public void test() {
        
        //String urlUnsafe = "https://mitja:gustin@mitja.si:443/index.html?noCache=true#idx123";
        String urlUnsafe = "https://google.com";
        URL url = null;
        try {
            url = UrlBuilder.build(urlUnsafe);
        } catch (MalformedURLException e) {
            Log.log(Level.SEVERE, "Error in url " + urlUnsafe, e);
            fail("Error in url");
        }
        
        try {
            Response request = instance.request("HEAD", url);
            assertNotNull("Reponse should not be null", request);

            Log.info(request.getResponseHeaders().toString());

        } catch (Exception e) {
            Log.log(Level.SEVERE, "Error connecting url " + urlUnsafe, e);
            fail("Error connecting url");
        }

    }

}
