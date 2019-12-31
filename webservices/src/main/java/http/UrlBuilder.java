package http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Build URL and ensures valid composition
 * @author gustin
 *
 */
public class UrlBuilder {

    /**
     *  Capital cased, to enable easy transition to Android logging 
     */
    public static final Logger Log = Logger.getLogger(HttpClient.class.getCanonicalName());

    /**
     * @param schema optional, default https
     * @param user optional (Basic auth)
     * @param password optional (Basic auth)
     * @param hostname required
     * @param port optional default 80
     * @param path optional (if emtpy, you're requesting index page)
     * @param query optional
     * @param fragment optional
     * @return
     * @throws MalformedURLException 
     */
    public static URL build(String schema, String user, String password, String hostname, Integer port, String path, String query, String fragment) throws MalformedURLException {

        // rebuild URL
        StringBuilder sb = new StringBuilder();
        if (schema == null || schema.isEmpty()) sb.append("https").append("://");
        else sb.append(schema).append("://");

        if (user != null && !user.isEmpty() && password != null && !password.isEmpty()) sb.append(user).append(":").append(password).append("@");

        if (hostname == null) throw new IllegalArgumentException("Hostname cannot be null");
        sb.append(hostname);
        if (port != null) sb.append(':').append(String.valueOf(port));
        if (path != null && !path.isEmpty()) {

            if (path.startsWith("/")) sb.append(path);
            else sb.append("/" + path);

        }

        if (query != null && !query.isEmpty()) sb.append('?').append(query);
        if (fragment != null && !fragment.isEmpty()) sb.append('#').append(fragment);
        try {
            return new URL(sb.toString());
        } catch (MalformedURLException e) {
            Log.log(Level.SEVERE, "Exception: " + e.toString(), e);
            throw e;
        }

    }

    /**
     * scheme://user:password@hostname:port/path?query#fragment
     * @throws MalformedURLException 
     * @throws Exception  
     */
    public static URL build(String url) throws MalformedURLException {

        if (url == null || url.isEmpty()) throw new IllegalArgumentException("URL cannot be emtpy");

        try { // Formal validation 
            new URL(url);
        } catch (MalformedURLException e) {
            Log.log(Level.SEVERE, "Mallformed " + url + " : " + e.toString(), e);
            throw e;
        }

        // parse given URL
        final Matcher m = Pattern.compile("^([hH][tT][tT][pP][sS]?)://(?:(\\w+):(\\w+)@)?((?:\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(?:[\\w-]+(?:\\.[\\w-]+)+))(?::(\\d+))?(/?.*?)(?:\\?(.*?))?(?:#(.*?))?$").matcher(url);
        if (!m.matches()) return null;

        // update given componenets
        final String schema;
        if (m.group(1) != null) schema = m.group(1);
        else schema = "http";

        final String user;
        if (m.group(2) != null) user = m.group(2);
        else user = null;

        final String password;
        if (m.group(3) != null) password = m.group(3);
        else password = null;

        final String hostname;
        if (m.group(4) != null) hostname = m.group(4);
        else throw new IllegalArgumentException("Hostname is null in url " + url);

        final int port;
        if (m.group(5) != null) port = Integer.parseInt(m.group(5));
        else port = 80;

        final String path;
        if (m.group(6) != null) path = m.group(6);
        else path = null;

        final String query;
        if (m.group(7) != null) query = m.group(7);
        else query = null;

        final String fragment;
        if (m.group(8) != null) fragment = m.group(8);
        else fragment = null;

        return build(schema, user, password, hostname, port, path, query, fragment);

    }

}
