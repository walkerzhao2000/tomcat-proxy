
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

/**
 * Http request proxy.
 *
 */
public class LocationProxy extends HttpServlet {

    private static final Logger Log = Logger.getLogger(LocationProxy.class.getName());
    private static final long serialVersionUID = 1L;
    private static final String mUrl = "";

    private boolean authenticate(HttpServletRequest request) {
        String[] values = getCredential(request);
        String username = values[0];
        byte[] password = DatatypeConverter.parseHexBinary(values[1]);
        Log.info("username=" + username);
//        Log.info("password=" + values[1]);

        // verify authentication
        try (MySqlAccessor db = new MySqlAccessor()) {
            byte[] cachedPassword = db.readCredential(username);
//          Log.info("cachedPassword=" + DatatypeConverter.printHexBinary(cachedPassword));
            if (password.length != cachedPassword.length) {
                return false;
            }
            for (int i=0; i<cachedPassword.length; ++i) {
                if (password[i] != cachedPassword[i]) {
                    return false;
                }
            }
            return true;
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String[] getCredential(HttpServletRequest request) {
        // Extract username:password
        final String authorization = request.getHeader("authorization");
        if (authorization != null && authorization.startsWith("basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("basic".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials),
                    Charset.forName("UTF-8"));
            // credentials = username:password
            return credentials.split(":", 2);
        } else {
            return new String[]{"", ""};
        }
    }

    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException {

//        Log.info("LocationProxy started");

        String[] values = getCredential(request);
        if (values.length != 2) {
            Log.info("HTTP request format error: this request does not contain proper format 'username:password'");
            return;
        }
        String username = values[0];
        String password = values[1];

        // verify authentication
        if (!authenticate(request)) {
            Log.info("authentication failed: username=" + username + ",password=" + password);
            return;
        } else {
            Log.info("authentication succeeded: username=\" + username");
        }

        // forward request
        URL url = new URL(mUrl);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        OutputStream out = null;
        InputStream in = null;

        try {
            conn.setRequestProperty("Content-Type", request.getHeader("Content-Type"));
            conn.setRequestProperty("Content-Length", String.valueOf(request.getContentLength()));
            conn.setReadTimeout(3000 /* milliseconds */);
            conn.setConnectTimeout(5000 /* milliseconds */);
            conn.setRequestMethod(request.getMethod());
            conn.setDoOutput(true);
            conn.setDoInput(true);

//            for (String header : conn.getRequestProperties().keySet()) {
//                if (header != null) {
//                    for (String value : conn.getRequestProperties().get(header)) {
//                        Log.info(header + ":" + value);
//                    }
//                }
//            }

            String requestBody = org.apache.commons.io.IOUtils.toString(request.getReader());
            requestBody = requestBody.replace(
                    "<key key=\"\" username=\"\"/>",
                    "<key key=\"\" username=\"\"/>");
//            Log.info("requestBody = " + requestBody);
            out = new BufferedOutputStream(conn.getOutputStream());
            PrintWriter pw = new PrintWriter(out);
            pw.print(requestBody);
            pw.flush();
            pw.close();

            int status = conn.getResponseCode();
            Log.info("status = " + status);
            if (status == HttpsURLConnection.HTTP_OK) {
                in = conn.getInputStream();
            } else {
                in = conn.getErrorStream();
            }

//            for (String header : conn.getHeaderFields().keySet()) {
//                if (header != null) {
//                    for (String value : conn.getHeaderFields().get(header)) {
//                        Log.info(header + ":" + value);
//                    }
//                }
//            }

            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            // Response from server after login process will be stored in response variable.
            String responseBody = sb.toString();
//            Log.info("responseBody = " + responseBody);
            isr.close();
            reader.close();

            response.setContentType("text/xml");
            response.setCharacterEncoding("UTF-8");

            PrintWriter pwRsp = response.getWriter();
            pwRsp.println(responseBody);

        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            conn.disconnect();
//            Log.info("LocationProxy stopped");
        }
    }

    @Override
    public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException {
        doGet(request, response);
    }

//    public static void main(String[] args) throws Exception {
//        try (MySqlAccessor dao = new MySqlAccessor()) {
//            byte[] password = dao.readCredential("wjao");
//            System.out.println(DatatypeConverter.printHexBinary(password));
//            dao.addUser("testUser", "1234567890123456789012345678901234567890", "1234567890123456", "test@test.com", 1);
//        }
//    }
}

