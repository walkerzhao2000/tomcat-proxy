
import java.io.*;
import java.net.URL;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Http request proxy.
 *
 */
public class LocationProxy extends HttpServlet {

    private static final Logger Log = Logger.getLogger(LocationProxy.class.getName());
    private static final long serialVersionUID = 1L;
    private static final String mUrl = "";

    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException {

//        Log.info("LocationProxy started");
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

}

