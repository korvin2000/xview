package net.j7.commons.xml;

import java.io.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class XMLTools {

	static Logger logger = LoggerFactory.getLogger(XMLTools.class);

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
	            logger.error("",e1);
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
	            logger.info("",e1);
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


}
