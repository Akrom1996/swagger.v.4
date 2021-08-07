package uz.pet.utils;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.ssl.SSLContexts;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.net.ssl.SSLContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.lang.reflect.Method;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Properties;

/**
 * Created by lex on 29.10.19.
 */
@SuppressWarnings("unchecked")
public class CommonUtils {
    Logger log = LogManager.getLogger("CommonUtils");
    private Properties props;
    private SSLConnectionSocketFactory sslConnectionSocketFactory;

    public SSLConnectionSocketFactory getSslConnectionSocketFactory() {
        return sslConnectionSocketFactory;
    }

    public CommonUtils() {
        try {
            loadProperties();
            sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    createSslCustomContext(),
                    new String[]{"TLSv1.2"},
                    null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public Properties getProps() {
        return props;
    }

    @SuppressWarnings("unchecked")
    public void copyObject(Object source, Object dest) throws Exception {

        Method[] gettersAndSetters = source.getClass().getMethods();

        for (int i = 0; i < gettersAndSetters.length; i++) {
            String methodName = gettersAndSetters[i].getName();
            try {
                if (methodName.startsWith("get")) {
                    dest.getClass().getMethod(methodName.replaceFirst("get", "set"), gettersAndSetters[i].getReturnType()).invoke(dest, gettersAndSetters[i].invoke(source, null));
                } else if (methodName.startsWith("is")) {
                    dest.getClass().getMethod(methodName.replaceFirst("is", "set"), gettersAndSetters[i].getReturnType()).invoke(dest, gettersAndSetters[i].invoke(source, null));
                }

            } catch (NoSuchMethodException e) {
                // TODO: handle exception
            } catch (IllegalArgumentException e) {
                // TODO: handle exception
            }

        }
    }

    @SuppressWarnings("unchecked")
    public void copyObjectMsgMethodparams(Object source, Object dest) throws Exception {

        Method[] gettersAndSetters = source.getClass().getMethods();

        for (int i = 0; i < gettersAndSetters.length; i++) {
            String methodName = gettersAndSetters[i].getName();
            try {
                if (methodName.startsWith("get")) {
                    dest.getClass().getMethod(methodName.replaceFirst("get", "set"), gettersAndSetters[i].getReturnType()).invoke(dest, gettersAndSetters[i].invoke(source, null));
                } else if (methodName.startsWith("is")) {
                    dest.getClass().getMethod(methodName.replaceFirst("is", "set"), gettersAndSetters[i].getReturnType()).invoke(dest, gettersAndSetters[i].invoke(source, null));
                }

            } catch (NoSuchMethodException e) {
                // TODO: handle exception
            } catch (IllegalArgumentException e) {
                // TODO: handle exception
            }

        }
    }

    @SuppressWarnings("unchecked")
    public String convertClassToXML(Object o, boolean without_xml_header) throws Exception {
        JAXBContext context = JAXBContext.newInstance(o.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter xmlString = new StringWriter();
        marshaller.marshal(o, xmlString);
        if (without_xml_header)
            return xmlString.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
        else
            return xmlString.toString();
    }

    public <T> T castObject(Class<T> clazz, Object object) {
        return (T) object;
    }

    @SuppressWarnings("unchecked")
    public Object convertFaultToClass(Detail body,String faultCode,String faultString, Object o) throws Exception {
        JAXBContext context = JAXBContext.newInstance(o.getClass());
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Object res=null;
        try {
            res=castObject(o.getClass(), unmarshaller.unmarshal(body.getFirstChild()));
        } catch (Exception ex){
            res=o;
        }

        Method setFaultStringMethod = res.getClass().getMethod("setFaultString",String.class);
        Method setFaultCodeMethod = res.getClass().getMethod("setFaultCode",String.class);
        setFaultStringMethod.invoke(res,faultString);
        setFaultCodeMethod.invoke(res,faultCode);
        return res;
    }

    @SuppressWarnings("unchecked")
    public String transformReponseWithXSL(String xslname, String xml) throws IOException, TransformerException {
        String result=null;
        try (InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xslname)) {

            TransformerFactory factory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(resourceStream);
            Transformer transformer = factory.newTransformer(xslt);
            ByteArrayInputStream bais=new ByteArrayInputStream(xml.getBytes("UTF-8"));
            Source text = new StreamSource(bais);
            ByteArrayOutputStream outresourceStream = new ByteArrayOutputStream();
            transformer.transform(text, new StreamResult(outresourceStream));
            result=outresourceStream.toString("UTF-8");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public String nodeToString(org.w3c.dom.Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
        }
        return sw.toString();
    }

    @SuppressWarnings("unchecked")
    public Object convertNodeToClass(SOAPBody body, Object o) throws Exception {
        JAXBContext context = JAXBContext.newInstance(o.getClass());
        Unmarshaller unmarshaller = context.createUnmarshaller();
        body.addNamespaceDeclaration("xsd1","xmlns:xsd1=\"http://www.w3.org/2001/XMLSchema\"");
        Document e=body.extractContentAsDocument();
        return castObject(o.getClass(), unmarshaller.unmarshal(e));
    }

    public Object convertXMLToClass(String xml, Object o) throws Exception {
        JAXBContext context = JAXBContext.newInstance(o.getClass());
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return castObject(o.getClass(), unmarshaller.unmarshal(new StringReader(xml)));
    }

    public String getAuthorizationHeader(String login, String password) throws UnsupportedEncodingException {
        return new String(Base64.getEncoder().encode(
                (login + ":" + password).getBytes("UTF-8")),"UTF-8");
    }





    public void loadProperties() throws Exception {
        try (InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            props = new Properties();
            props.load(resourceStream);
        }
    }

        public SSLContext createSslCustomContext() throws Exception {

            SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustAllStrategy()) // use it to customize
//                    .loadKeyMaterial(cks, props.getProperty("cert.pwd").toCharArray()) // load client certificate
                    .build();
            return sslcontext;
        }

    private SSLContext loadSSLContext() throws KeyManagementException, UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        //from https://developer.visa.com/pages/working-with-visa-apis/two-way-ssl
        SSLContext sslcontext = SSLContexts.custom()

                .loadKeyMaterial(new File(props.getProperty("cert.path")+File.separator+props.getProperty("cert.filename.jks")),
                        props.getProperty("cert.pwd").toCharArray(),
                        props.getProperty("cert.pwd").toCharArray())
                .loadTrustMaterial(new File(props.getProperty("cert.path")+File.separator+props.getProperty("cert.filename.jks")),
                        props.getProperty("cert.pwd").toCharArray())
                .build();

        return sslcontext;
    }

      public SOAPBody getSoapBodyFromResponse(String xmlResponse) throws SOAPException, IOException, SAXException, ParserConfigurationException {
        //This needs be your body xml that you want to set on SOAPBody
        InputStream is = new ByteArrayInputStream(xmlResponse.getBytes("UTF-8"));

//convert inputStream to Source
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        Document document = builder.parse(is);
        DOMSource domSource = new DOMSource(document);

//create new SOAPMessage instance
        MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage message = mf.createMessage();
        SOAPPart part = message.getSOAPPart();

//Set the xml in SOAPPart
        part.setContent(domSource);
        message.saveChanges();

//access the body
        SOAPBody body = message.getSOAPPart().getEnvelope().getBody();

//for you to access the values inside body, you need to access the node childs following the structures.
//example

        return body;
    }

}
