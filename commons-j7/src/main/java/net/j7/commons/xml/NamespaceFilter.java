/*
 * @File: NamespaceFilter.java
 *
 * Copyright (c) 2013 
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;


public class NamespaceFilter extends XMLFilterImpl {

   private static final String XML_NAMESPACE = "";

   public NamespaceFilter() {
      super();
   }

   public NamespaceFilter(XMLReader parent) {
      super(parent);
   }

   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
       int colonIndex = qName.indexOf(':');
       if(colonIndex >= 0) {
           qName = qName.substring(colonIndex + 1);
       }
       uri = XML_NAMESPACE; //to prevent unknown XML element exception, we have to specify the namespace here
       super.startElement(uri, localName, qName, attributes);
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
       int colonIndex = qName.indexOf(':');
       if(colonIndex >= 0) {
           qName = qName.substring(colonIndex + 1);
       }
       super.endElement(uri, localName, qName);
   }


}
