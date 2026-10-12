/*
 * @File: TestMain.java
 *
 *
 *
 * All rights reserved.
 *
 * @Author:  tavrovsa
 *
 * @Version $Revision: #1 $Date: $
 *
 *
 */
package net.j7.commons.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;


public class ReaderJAXB {


   public static <E>E getLogonJAXBObject(Class<E> clsName, String xmlData) throws JAXBException {
         // Parse the XML
         InputSource xml = new InputSource(new StringReader(xmlData));
         return getLogonJAXBObject(clsName, xml);
   }

   public static <E>E getLogonJAXBObject(Class<E> clsName, InputStream xmlData) throws JAXBException {
       // Parse the XML
       InputSource xml = new InputSource(xmlData);
       return getLogonJAXBObject(clsName, xml);
 }

   public static <E>E getJaxbModel(Class<E> clsName, InputStream is) throws JAXBException, IOException {
	         JAXBContext jaxbContext   = JAXBContext.newInstance (clsName);

	         //To unmarshall an XML document, you create an Unmarshaller from the context,
	         Unmarshaller unmarshaller =  jaxbContext.createUnmarshaller();


	         Object result = unmarshaller.unmarshal(is);

	         is.close();

	         if (result instanceof JAXBElement) {
		         javax.xml.bind.JAXBElement<E> obj =   (JAXBElement<E>) result;
		         return obj != null ? obj.getValue() : null;
	         } else {
	        	 return (E)result;
	         }

	   }


   public static <E>E getLogonJAXBObject(Class<E> clsName, InputSource xml) throws JAXBException {
	      try {

	         // Create the JAXBContext
	         JAXBContext jc = JAXBContext.newInstance(clsName);

	         // Create the XMLFilter
	         XMLFilter filter = new NamespaceFilter();

	         // Set the parent XMLReader on the XMLFilter
	         SAXParserFactory spf = SAXParserFactory.newInstance();
	         SAXParser sp = spf.newSAXParser();
	         XMLReader xr = sp.getXMLReader();
	         filter.setParent(xr);

	         // Set UnmarshallerHandler as ContentHandler on XMLFilter
	         Unmarshaller unmarshaller = jc.createUnmarshaller();
	         UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
	         filter.setContentHandler(unmarshallerHandler);

	         // Parse the XML
	         filter.parse(xml);
	         E logonObj = (E) unmarshallerHandler.getResult();

	         return logonObj;

	      } catch (Exception e) {
	         throw new JAXBException("",e);
	      }
	   }

}
