package com.example.soaprequesttester;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Servlet implementation class SoapRequestServlet
 */
public class SoapRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    private static final String CONTENT_TYPE =
        "text/html; charset=windows-1252";
    private static final String DOC_TYPE =
        "<!DOCTYPE html  \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" +
        "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";

    private String wsURL = "";
    private String requestMessage = "";

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE);
        //Write the XHTML
        PrintWriter printWriter = response.getWriter();
        printWriter.println("<?xml version=\"1.0\"?>");
        printWriter.println(DOC_TYPE);
        printWriter.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
        printWriter.println("<head><title>Webservice tester</title></head>");
        printWriter.println("<body >");
        printWriter.println("<form action=\"soaprequestservlet\" method=\"post\">");
        printWriter.println("<label for=\"wsdl\">Webservice endpoint</label>");
        printWriter.println("<input type=\"text\" name=\"endpoint\" value=\"\" size=\"120\" id=\"wsdl\" style= \"font-size: 12px; color: blue;\" />");
        printWriter.println("<label for=\"FormatXML\">Format XML</label>");
        printWriter.println("<input id=\"FormatXML\" type=\"checkbox\" name=\"formatXML\" value=\"formatXML\" checked=\"checked\"/>");
        printWriter.println("<p/><table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">");
        printWriter.println("<tr>");
        printWriter.println("<td width=\"45%\">");
        printWriter.println("<h3>SOAP Message Request</h3>");
        printWriter.println("<textarea style= \" font-size: 11px; color: blue;\" name=\"soapmessage\" cols=\"80\" rows=\"40\" " +
                            "title=\"SOAP MessageRequest\" id=\"soaprequest\"></textarea>");
        printWriter.println("</td>");
        printWriter.println("<td width=\"45%\">");
        printWriter.println("<h3>SOAP Message Response</h3>");
        printWriter.println("<textarea style= \"font-size: 11px; color: blue;\"  name=\"soapmessageresponse\" cols=\"80\" rows=\"40\"");
        printWriter.println("title=\"Soap Request\" id=\"soapresponse\"></textarea>");
        printWriter.println("</td>");
        printWriter.println("</tr>");
        printWriter.println("</table>");
        printWriter.println("<input type=\"submit\" name=\"Test SOAP Request\" value=\"Test SOAP Request\"/>");
        printWriter.println("</form>");
        printWriter.println("</body></html>");
        printWriter.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Whether to format the SOAP message response or not.
        String formatXML = request.getParameter("formatXML");

        //Get the endpoint
        if (!request.getParameter("endpoint").equals(""))
            wsURL = request.getParameter("endpoint");
        else
            throw new ServletException("Missing endpoint location!");

        //Get the SOAP message request
        String xmlInput = request.getParameter("soapmessage");
        requestMessage = xmlInput;

        //Code to make a webservice HTTP request
        URL url = new URL(wsURL);
        URLConnection connection = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection)connection;
        String responseString = "";
        String outputString = "";
        //Optional: set your action
        //String SOAPAction =
        //    "http://litwinconsulting.com/webservices/GetWeather";
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        OutputStream out = null;
        InputStreamReader isr = null;
        BufferedReader in = null;
        byte[] buffer = new byte[xmlInput.length()];
        buffer = xmlInput.getBytes();
        bout.write(buffer);
        byte[] b = bout.toByteArray();

        // Set the appropriate HTTP parameters.
        httpConn.setRequestProperty("Content-Length",
                                    String.valueOf(b.length));
        httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        //Optional: set your action
        //httpConn.setRequestProperty("SOAPAction", SOAPAction);
        httpConn.setRequestMethod("POST");
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        out = httpConn.getOutputStream();

        // write the content of the request to the outputstream of the HTTP Connection.
        out.write(b);
        out.close();
        // ready with sending the request

        // Read the response.
        isr = new InputStreamReader(httpConn.getInputStream());
        in = new BufferedReader(isr);

        //Write the SOAP message response to a String.
        while ((responseString = in.readLine()) != null) {
            outputString =
                    outputString + responseString + (formatXML == null ? "\n" :
                                                     "");
        }
        //Format the message when the checkbox is checked.
        if (formatXML != null)
            outputString = format(outputString);


        //write the XHTML response.
        response.setContentType(CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();
        printWriter.println("<?xml version=\"1.0\"?>");
        printWriter.println(DOC_TYPE);
        printWriter.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
        printWriter.println("<head><title>Webservice tester</title></head>");
        printWriter.println("<body>");
        printWriter.println("<form action=\"soaprequestservlet\" method=\"post\">");
        printWriter.println("<label for=\"wsdl\">Webservice endpoint</label>");
        printWriter.println("<input type=\"text\" name=\"endpoint\" value=\"" +
                            wsURL +
                            "\" size=\"120\" id=\"wsdl\" style= \"; font-size: 12px; color: blue;\" />");
        printWriter.println("<label for=\"FormatXML\">Format XML</label>");
        printWriter.println("<input id=\"FormatXML\" type=\"checkbox\" name=\"formatXML\" value=\"formatXML\" checked=\"checked\"/>");
        printWriter.println("<p/><table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">");
        printWriter.println("<tr>");
        printWriter.println("<td width=\"45%\">");
        printWriter.println("<h3>SOAP Message Request</h3>");
        printWriter.println("<textarea style= \"; font-size: 11px; color: blue\";  name=\"soapmessage\" cols=\"80\" rows=\"40\" title=\"SOAP Message Request\" id=\"soaprequest\">" +
                            requestMessage + "</textarea>");
        printWriter.println("</td>");
        printWriter.println("<td width=\"45%\">");
        printWriter.println("<h3>SOAP Message Response</h3>");
        printWriter.println("<textarea style= \"; font-size: 11px; color: blue\"; name=\"soapmessageresponse\" cols=\"80\" rows=\"40\"");
        printWriter.println("title=\"SOAP Message Response\" id=\"soapresponse\"");

        //write the SOAP message response to the textarea.
        printWriter.println(outputString);

        //Continue writing XHTML
        printWriter.println("</textarea>");
        printWriter.println("</td>");
        printWriter.println("</tr>");
        printWriter.println("</table>");
        printWriter.println("<input type=\"submit\" name=\"Test SOAP Request\" value=\"Test SOAP Request\"/>");
        printWriter.println("</form>");
        printWriter.println("</body></html>");
        printWriter.close();
	}

    //format the XML in your String

    public String format(String unformattedXml) {
        try {
            Document document = parseXmlFile(unformattedXml);
            OutputFormat format = new OutputFormat(document);
            format.setIndenting(true);
            format.setIndent(3);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
