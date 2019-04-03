package jconnect;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/** Various domain object
 * @author Mitja Gustin gustinmi [at] gmail [dot] com
*/
public class WebAr {

    public static class Response {

        private final int statusCode;
        private final String content;
        private final HttpURLConnection conn;
        private final Map<String, List<String>> responseHeaders;

        public Response(int statusCode, String content, HttpURLConnection conn, Map<String, List<String>> responseHeaders) {
            super();
            this.statusCode = statusCode;
            this.content = content;
            this.conn = conn;
            this.responseHeaders = responseHeaders;
        }

        public Map<String, List<String>> getResponseHeaders() {
            return this.responseHeaders;
        }

        public int getStatusCode() {
            return this.statusCode;
        }

        public String getContent() {
            return this.content;
        }

        public HttpURLConnection getConn() {
            return this.conn;
        }

    }

    public static class TimeoutDefs {

        private final int readTimeout;
        private final int connectTimeout;
        private final int max_retries;

        public TimeoutDefs() {
            this.connectTimeout = 15000;
            this.readTimeout = 60000;
            this.max_retries = 5;
        }

        public TimeoutDefs(Integer readTimeout, Integer connectTimeout, Integer max_retries) {
            super();

            this.connectTimeout = connectTimeout == null ? 15000 : connectTimeout;
            this.readTimeout = readTimeout == null ? 60000 : readTimeout;
            this.max_retries = max_retries == null ? 5 : max_retries;
        }

        public int getReadTimeout() {
            return this.readTimeout;
        }

        public int getConnectTimeout() {
            return this.connectTimeout;
        }

        public int getMax_retries() {
            return this.max_retries;
        }

    }

    public static final class KeyValueData {
    
        private final String name;
        private final String value;
    
        public KeyValueData(String name, String value) {
            super();
            this.name = name;
            this.value = value;
        }
    
        public String getName() {
            return this.name;
        }
    
        public String getValue() {
            return this.value;
        }
    
    }

    public static final class UploadData {
    
        private final String name;
        private final InputStream content;
    
        public UploadData(String name, InputStream content) {
            super();
            this.name = name;
            this.content = content;
        }
    
        public String getName() {
            return this.name;
        }
    
        public InputStream getContent() {
            return this.content;
        }
    
    }

    public static enum HttpRequestFormat {
        /** GET, HEAD request withoput request body */
        NODATA,
        
        /** Raw HTTP request (usually for RESTAPIs that use JSON) */
        RAW, 
        
        /** Multipart formdata + attachements (upload files) */
        MULTIPART,
        
        /** Classis form post data - HTTP forms, documents etc */
        FORM_DATA;

        /** Determin how to create requewst headers and body */
        public static HttpRequestFormat get(String rawData, KeyValueData[] formData, UploadData[] files) {
    
            if (rawData != null && !rawData.isEmpty()) return HttpRequestFormat.RAW;
            if (rawData == null && formData == null && files == null) return NODATA;
            if (rawData == null && formData != null && files != null) return MULTIPART;
            if (rawData == null && formData != null && files == null) return FORM_DATA;
            if (rawData == null && formData == null && files != null) return FORM_DATA;
    
            return HttpRequestFormat.NODATA;
        }
    
    }

    public static enum HttpRequestType {
        GET, POST, PUT, DELETE, OPTIONS, HEAD
    }

    /** Levels of HTTPS security */
    public static enum HTTPS_OPTS {

        /**
         * Insecure requests with all trusting validators 
         */
        HTTPS_BASIC,

        /**
         * provided JKS store (system file or jar embedded) 
         */
        HTTPS_PROVIDED_CA,

        /**
         * Use java provided JKS stores 
         */
        HTTPS_DEFAULT_PUBLIC_CA,

        /**
         * Optional Private CA certificate bundle in PEM encoding 
         */
        HTTPS_PRIVATE_CA,

        /**
         * Create certificate from PEM encoded string.
         * @see jconnect.HttpClient.CaCertPem
         */
        HTTPS_DEVELOPMENT;
    }

}
