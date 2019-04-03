package webclient;

import static org.junit.Assert.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import jconnect.UrlBuilder;


public class UrlBuilderTest {

    public static final Logger log = Logger.getLogger(UrlBuilderTest.class.getCanonicalName());

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testUrlFromString() throws MalformedURLException {

        final URL urlBuilder = UrlBuilder.build("https://mitja:gustin@mitja.si:443/index.html?noCache=true#idx123");
        log.info(urlBuilder.toString());

        final URL urlBuilder1 = UrlBuilder.build("https://mitja:gustin@mitja.si:443/index.html?noCache=true#idx123");
        log.info(urlBuilder1.toString());

        final URL urlBuilder2 = UrlBuilder.build("https://mitja.si/index.html");
        log.info(urlBuilder2.toString());

        final URL urlBuilder3 = UrlBuilder.build("http://mitja.si");
        log.info(urlBuilder3.toString());

        final URL urlBuilder4 = UrlBuilder.build("https://google.com");
        log.info(urlBuilder4.toString());

    }

    @Test
    public void testUrlFromParams() throws MalformedURLException {

        // Full url

        final URL urlBuilder = UrlBuilder.build("https", "mitja", "gustin", "mitja.si", 443, "index.html", "noCache=true", "idx123");
        log.info(urlBuilder.toString());

        assertTrue("Protocol should be https but is " + urlBuilder.getProtocol(), urlBuilder.getProtocol().equalsIgnoreCase("https"));
        assertTrue("Path should be index.html but is " + urlBuilder.getPath(), urlBuilder.getPath().equalsIgnoreCase("/index.html"));
        assertTrue("Port should be 443 but is " + urlBuilder.getPort(), urlBuilder.getPort() == 443);
        assertTrue("Query should be index.html but is " + urlBuilder.getQuery(), urlBuilder.getQuery().equalsIgnoreCase("noCache=true"));
        assertTrue("Host should be mitja.si but is " + urlBuilder.getHost(), urlBuilder.getHost().equalsIgnoreCase("mitja.si"));
        assertTrue("Userinfo should be mitja.si but is " + urlBuilder.getUserInfo(), urlBuilder.getUserInfo().equalsIgnoreCase("mitja:gustin"));
        assertTrue("Fragment should be idx123 but is " + urlBuilder.getRef(), urlBuilder.getRef().equalsIgnoreCase("idx123"));

        final URL urlBuilder1 = UrlBuilder.build(null, "mitja", "gustin", "mitja.si", 443, "index.html", "noCache=true", "idx123");
        log.info(urlBuilder1.toString());

        final URL urlBuilder2 = UrlBuilder.build(null, null, null, "mitja.si", null, "index.html", null, null);
        log.info(urlBuilder2.toString());

        final URL urlBuilder3 = UrlBuilder.build(null, null, null, "mitja.si", null, null, null, null);
        log.info(urlBuilder3.toString());

    }

}
