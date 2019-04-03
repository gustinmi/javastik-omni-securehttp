package webclient;

import static org.junit.Assert.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import jconnect.StreamUtils;
import jconnect.HttpProtocol;
import jconnect.WebAr;
import jconnect.WebAr.KeyValueData;
import jconnect.WebAr.UploadData;


public class ProtocolTester {

    File tempFile;

    @Before
    public void setUp() throws Exception {
        tempFile = File.createTempFile("multipart", "dat"); // temporary file storage, to track content length without memory buffers
        try (final OutputStream tmpOut = new FileOutputStream(tempFile)) {
            tmpOut.write(("CONTENT__OF_FILE_CONTENT__OF_FILE_CONTENT__OF_FILE").getBytes(StandardCharsets.UTF_8));
        }
    }

    @After
    public void tearDown() throws Exception {
        tempFile.delete();
    }

    @Test
    public void testMultipart() throws FileNotFoundException {
        
        WebAr.KeyValueData[] formData = new  WebAr.KeyValueData[] {new WebAr.KeyValueData("testKey", "testData")};
        WebAr.UploadData[] uploadFiles = new WebAr.UploadData[] { new WebAr.UploadData("testFile1", new FileInputStream(tempFile)) };

        try {

            File multipartWriter = HttpProtocol.buildMultipart(formData, uploadFiles);
            String contentsFile = StreamUtils.printFile(multipartWriter);
            assertTrue("Datoteka je prazna", contentsFile != null && !contentsFile.isEmpty());
            System.out.println(contentsFile);

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

}
