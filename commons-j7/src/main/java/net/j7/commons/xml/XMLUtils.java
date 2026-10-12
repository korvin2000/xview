package net.j7.commons.xml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class XMLUtils {

   /** The technical logger to use */
   private static final Logger logger = LoggerFactory.getLogger(XMLUtils.class);

   private static ThreadLocal<DocumentBuilderProvider> documentBuilderProvider = new ThreadLocal<DocumentBuilderProvider>();

   public static Document buildDocument() throws ParserConfigurationException {

      return buildDocument(false, false);
   }

   public static Document buildDocument(boolean ignoreWhitespace, boolean namespaceAware) throws ParserConfigurationException {

      return getDocumentBuilder(ignoreWhitespace, namespaceAware).newDocument();
   }

   public static Document buildDocument(String tag) throws ParserConfigurationException {

      Document document = XMLUtils.buildDocument();
      document.appendChild(document.createElement(tag));
      return document;
   }

   public static Document buildAndParseDocument(String in) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(in));
			return db.parse(is);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


   public static Document parseDocument(String xmlDoc) {

      if (xmlDoc == null)
         return null;
      Document document = null;
      try {
         BufferedReader bufferedReader = new BufferedReader(new StringReader(xmlDoc));
         document = parseDocument(new InputSource(bufferedReader), false, false);
      } catch (Exception ex) {
        logger.error("Error while parsing document \n" + xmlDoc, ex);
      }
      return document;
   }

   public static Document parseDocument(InputStream input, boolean ignoreWhitespace, boolean namespaceAware) throws ParserConfigurationException, SAXException, IOException {

      DocumentBuilder documentBuilder = getDocumentBuilder(ignoreWhitespace, namespaceAware);
      return documentBuilder.parse(input);
   }

   public static Document parseDocument(InputSource input, boolean ignoreWhitespace, boolean namespaceAware) throws ParserConfigurationException, SAXException, IOException {

      DocumentBuilder documentBuilder = getDocumentBuilder(ignoreWhitespace, namespaceAware);
      return documentBuilder.parse(input);
   }

   public static Document parseDocument(File input, boolean ignoreWhitespace, boolean namespaceAware) throws ParserConfigurationException, SAXException, IOException {

      DocumentBuilder documentBuilder = getDocumentBuilder(ignoreWhitespace, namespaceAware);
      return documentBuilder.parse(input);
   }

   public static Document parseDocument(Reader input, boolean ignoreWhitespace, boolean namespaceAware) throws ParserConfigurationException, SAXException, IOException {

      DocumentBuilder documentBuilder = getDocumentBuilder(ignoreWhitespace, namespaceAware);
      return documentBuilder.parse(new InputSource(input));
   }

   /**
    * Decode the xml data and encode it into a target code format Example: From UTF-8 into ISO-xxx
    *
    * @param String xmlDoc
    * @param xmlEncode - Target coding
    * @return target coded xml data as String
    */
   public static String encodeXML(String xmlDoc, String xmlEncode) {

      if (xmlDoc == null)
         return null;
      Document document = null;
      try {
         DocumentBuilder documentBuilder = getDocumentBuilder(false, false);
         BufferedReader bufferedReader = new BufferedReader(new StringReader(xmlDoc));
         document = documentBuilder.parse(new InputSource(bufferedReader));

         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         if (logger.isInfoEnabled()) {
            logger.info("TransformerFactory class: " + transformerFactory.getClass().getName());
         }
         Transformer transformer = transformerFactory.newTransformer();
         if (logger.isInfoEnabled()) {
            logger.info("Transformer class: " + transformer.getClass().getName());
         }
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty(OutputKeys.ENCODING, xmlEncode);
         transformer.setOutputProperty(OutputKeys.METHOD, "xml");
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         StreamResult result = new StreamResult(new OutputStreamWriter(baos, xmlEncode));
         transformer.transform(new DOMSource(document.getDocumentElement()), result);

         return prettyPrintXML(baos.toString(xmlEncode), xmlEncode);
      } catch (Exception ex) {
         logger.error("Error while encoding document \n" + xmlDoc, ex);
      }
      return null;
   }

   private static javax.xml.parsers.DocumentBuilder getDocumentBuilder(boolean ignoreWhitespace, boolean namespaceAware)
         throws javax.xml.parsers.ParserConfigurationException {

      DocumentBuilderProvider provider = documentBuilderProvider.get();
      if(provider == null) {
         provider = new DocumentBuilderProvider();
         documentBuilderProvider.set(provider);
      }
      return provider.getDocumentBuilder(ignoreWhitespace, namespaceAware);
   }

   /**
    * Check is the byteArray is an compressed xml
    * @param xmlByteArray
    * @return
    * @throws IOException
    */
   public static boolean checkCompressedXmlByteArray(byte[] xmlByteArray) throws IOException {

      // check gzipHeader
      if (xmlByteArray == null || xmlByteArray.length < 2)
         return false;

      int gzipHeader = ((int) xmlByteArray[1] << 8) | xmlByteArray[0];

      if (gzipHeader == 0x8b1f)
         return true;


      // The data is usually compressed, but...
      if (!(xmlByteArray[0] == '<')) {
         logger.trace("xmlByteArray is compressed");
         return true;
      } else {
         logger.trace("xmlByteArray is uncompressed.");
         return false;
      }
   }

   /**
    * Decompress the xmlByteArray
    */
   public static byte[] compressByteArray(String xml, String encoding) throws IOException {
     byte[]result = null;
      if (xml != null) {
         GZIPOutputStream gzos = null;
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try {
            gzos = new GZIPOutputStream(baos);
            gzos.write(xml.getBytes(encoding));
            gzos.finish();
            result =  baos.toByteArray();

         } catch (IOException ioExc) {
            logger.error("Exception occured while compressing String: " + ioExc.getMessage(), ioExc);
         } finally {
            try {
               gzos.close();
            } catch (IOException ioExc) {
               logger.error("Exception occured while closing streams: " + ioExc.getMessage(), ioExc);
            }
         }
      }
      return result;
   }


   /**
    * uncompress the xmlByteArray
    */
   public static byte[] uncompressByteArray(byte[] xmlByteArray) throws IOException {
      byte[] tmp = new byte[2048];
      int byteCount = 0;
      ByteArrayOutputStream uncompressedData = new ByteArrayOutputStream();
      GZIPInputStream gzipIS = new GZIPInputStream(new ByteArrayInputStream(xmlByteArray));

      while ((byteCount = gzipIS.read(tmp)) != -1) {
         uncompressedData.write(tmp, 0, byteCount);
      }

      return uncompressedData.toByteArray();
   }


   /**
    * same as {@link prettyPrintXML(Source in, Result out) prettyPrintXML(Source in, Result out)} with other params.
    *
    * @param xml as <code>DOM</code>
    * @param out e.g. <code>Stream.out</code>
    */
   public static void prettyPrintXML(Document doc, OutputStream out, String encoding) {

      prettyPrintXML(new DOMSource(doc), new StreamResult(out), encoding);
   }

   /**
    * same as {@link prettyPrintXML(Source in, Result out) prettyPrintXML(Source in, Result out)} with other params.
    *
    * @param xml as <code>DOM</code>
    * @param out as Writer (toFile)
    */
   public static void prettyPrintXML(Document doc, Writer out, String encoding) {

      prettyPrintXML(new DOMSource(doc), new StreamResult(out), encoding);
   }

   /**
    * same as {@link prettyPrintXML(Source in, Result out) prettyPrintXML(Source in, Result out)} with other params.
    *
    * @param xml as <code>DOM</code>
    * @param out as Writer (toFile)
    */
   public static void prettyPrintXML(Node n, Writer out, String encoding) {

      prettyPrintXML(new DOMSource(n), new StreamResult(out), encoding);
   }

   /**
    * same as {@link prettyPrintXML(Source in, Result out) prettyPrintXML(Source in, Result out)} with other params.
    *
    * @param xml as String
    * @param out e.g. <code>Stream.out</code>
    */
   public static void prettyPrintXML(String xml, OutputStream out, String encoding) {

      try {
         prettyPrintXML(new SAXSource(new InputSource(new StringReader(xml))), new StreamResult(out), encoding);

      } catch (Throwable e) {

         // just print the original instead
         try {
            out.write(xml.getBytes());
         } catch (IOException e1) {
            logger.error("Error printing xml", e1);
         }
      }
   }

   /**
    * same as {@link prettyPrintXML(Source in, Result out) prettyPrintXML(Source in, Result out)} with other params.
    *
    * @param xml as String
    * @param out as Writer (toFile)
    */
   public static void prettyPrintXML(String xml, Writer out, String encoding) {

      try {
         prettyPrintXML(new SAXSource(new InputSource(new StringReader(xml))), new StreamResult(out), encoding);

      } catch (Throwable e) {

         // just print the original instead
         try {
            out.write(xml);
         } catch (IOException e1) {
            logger.info("error printing xml", e1);
         }
      }
   }

   /**
    * same as {@link prettyPrintXML(Source in, Result out) prettyPrintXML(Source in, Result out)} with other params.
    *
    * @param xml as String
    * @param out as Writer (toFile)
    */
   public static String prettyPrintXML(String xml, String encoding) {

      StringWriter outputString = new StringWriter();

      prettyPrintXML(xml, outputString, encoding);

      return outputString.toString();

   }

   /**
    * same as {@link prettyPrintXML(Source in, Result out) prettyPrintXML(Source in, Result out)} with other params.
    *
    * @param xml as String
    * @param out as Writer (toFile)
    */
   public static String prettyPrintXML(Document doc, String encoding) {

      // StringWriter outputString = new StringWriter();
      ByteArrayOutputStream outStr = new ByteArrayOutputStream();

      try {
         OutputStreamWriter owriter = new OutputStreamWriter(outStr, encoding);

         prettyPrintXML(doc, owriter, encoding);
         return new String(outStr.toByteArray(), encoding);

      } catch (UnsupportedEncodingException e) {
         logger.error("unknown encoding \"" + encoding + "\"", e);
         return "";
      }

   }

   /**
    * same as {@link prettyPrintXML(Source in, Result out) prettyPrintXML(Source in, Result out)} with other params.
    *
    * @param xml as String
    * @param out as Writer (toFile)
    */
   public static String prettyPrintXML(Node n, String encoding) {

      // StringWriter outputString = new StringWriter();
      ByteArrayOutputStream outStr = new ByteArrayOutputStream();

      try {
         OutputStreamWriter owriter = new OutputStreamWriter(outStr, encoding);

         prettyPrintXML(n, owriter, encoding);
         return new String(outStr.toByteArray(), encoding);

      } catch (UnsupportedEncodingException e) {
         logger.error("unknown encoding \"" + encoding + "\"", e);
         return "";
      }

   }

   public static String prettyPrintXML(Document doc) {

      return prettyPrintXML(doc, "UTF-8");
   }

   /**
    * transforms the content of the <code>Source</code> to the <code>Result</code>. It indents it with tree blanks
    * and uses <code>"Cp1252"</code> as encoding.
    *
    * @param in
    * @param out
    */
   public static void prettyPrintXML(Source in, Result out, String encoding) {

      TransformerFactory tf = TransformerFactory.newInstance();

      Transformer t;
      try {
         t = tf.newTransformer();

         t.setOutputProperty(OutputKeys.METHOD, "xml");
         t.setOutputProperty(OutputKeys.INDENT, "yes");
         t.setOutputProperty(OutputKeys.ENCODING, encoding);
         t.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "3");

         t.transform(in, out);

      } catch (Exception e) {
         throw new RuntimeException(e);
      }

   }

   public static void removeAllAttributes(Node node, String attrName) {

      // check if this node contains the attribute and remove it
      NamedNodeMap attrs = node.getAttributes();
      if (attrs != null && attrs.getNamedItem(attrName) != null) {
         attrs.removeNamedItem(attrName);
      }

      // process recursively all children
      NodeList list = node.getChildNodes();
      for (int i = 0; i < list.getLength(); i++) {
         // Get child node
         Node childNode = list.item(i);

         // Visit child node
         removeAllAttributes(childNode, attrName);
      }
   }

   public void createTextNode(Element header, String nodeName, String value) {

      Element targetElement = header.getOwnerDocument().createElement(nodeName);
      Text textnode = header.getOwnerDocument().createTextNode(value);
      targetElement.appendChild(textnode);
      header.appendChild(targetElement);

   }


   public static String xmlEscape(final String str) {
      if (null == str) return null;
      final int len = str.length();
      StringBuilder sb = new StringBuilder(len);
      char c;
      for (int i = 0; i < len; i++) {
         c = str.charAt(i);
         switch (c) {
         case '&':
             sb.append("&amp;");
             break;
         case '<':
             sb.append("&lt;");
             break;
         case '>':
             sb.append("&gt;");
             break;
         case '"':
             sb.append("&quot;");
             break;
         case '\'':
             sb.append("&apos;");
             break;
         case '\n':
            sb.append("&#xA;");
            break;
          case '\r':
            sb.append("&#xD;");
            break;
          case '\t':
            sb.append("&#x9;");
         case '\000': case '\001': case '\002': case '\003': case '\004':
         case '\005': case '\006': case '\007': case '\010': case '\013':
         case '\014': case '\016': case '\017': case '\020': case '\021':
         case '\022': case '\023': case '\024': case '\025': case '\026':
         case '\027': case '\030': case '\031': case '\032': case '\033':
         case '\034': case '\035': case '\036': case '\037':
           // do nothing, these are disallowed characters
            sb.append(' ');
         break;

         default:
             sb.append(c);
         }
      }
      return sb.toString();
   }

   //DocumentBuilderProvider is NOT ThreadSafe, never use without synchronization or pinned to ThreadLocal
   static class DocumentBuilderProvider {
      //DocumentBuilderFactory are ThreadSafe
      private static Map<DocumentBuilderKey, DocumentBuilderFactory> factories = new ConcurrentHashMap<DocumentBuilderKey, DocumentBuilderFactory>();

      //DocumentBuilder are NOT ThreadSafe
      private Map<DocumentBuilderKey, DocumentBuilder> documentBuilder = new HashMap<DocumentBuilderKey, DocumentBuilder>();

      static {
         //Default Factory
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factories.put(new DocumentBuilderKey(false, false), factory);

         //Factory IgnoringElementContentWhitespace = true
         factory = DocumentBuilderFactory.newInstance();
         factory.setIgnoringElementContentWhitespace(true);
         factories.put(new DocumentBuilderKey(true, false), factory);

         //Factory NamespaceAware = true
         factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         factories.put(new DocumentBuilderKey(false, true), factory);

         //Factory IgnoringElementContentWhitespace = true & NamespaceAware = true
         factory = DocumentBuilderFactory.newInstance();
         factory.setIgnoringElementContentWhitespace(true);
         factory.setNamespaceAware(true);
         factories.put(new DocumentBuilderKey(true, true), factory);
      }

      public javax.xml.parsers.DocumentBuilder getDocumentBuilder(boolean ignoreWhitespace, boolean namespaceAware)
            throws javax.xml.parsers.ParserConfigurationException {

         DocumentBuilderKey builderKey = new DocumentBuilderKey(ignoreWhitespace, namespaceAware);
         javax.xml.parsers.DocumentBuilder result = documentBuilder.get(builderKey);
         if(result == null) {
            result = factories.get(builderKey).newDocumentBuilder();
            documentBuilder.put(builderKey, result);
         } else {
            result.reset();
         }
         return result;
      }
   }

   static class DocumentBuilderKey {
      private boolean ignoreWhitespace = false;
      private boolean namespaceAware = false;

      public DocumentBuilderKey(boolean ignoreWhitespace, boolean namespaceAware) {
         this.ignoreWhitespace = ignoreWhitespace;
         this.namespaceAware = namespaceAware;
      }

      @Override
      public int hashCode() {

         final int prime = 31;
         int result = 1;
         result = prime * result + (ignoreWhitespace ? 1231 : 1237);
         result = prime * result + (namespaceAware ? 1231 : 1237);
         return result;
      }

      @Override
      public boolean equals(Object obj) {

         if (obj == null)
            return false;
         DocumentBuilderKey other = (DocumentBuilderKey) obj;
         if (ignoreWhitespace != other.ignoreWhitespace)
            return false;
         if (namespaceAware != other.namespaceAware)
            return false;
         return true;
      }
   }
}
