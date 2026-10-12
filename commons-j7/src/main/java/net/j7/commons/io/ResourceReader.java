package net.j7.commons.io;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceReader {

	/** The Constant TEMPLATE_ENCODING. */
	public static final String DEFAULT_ENCODING = "UTF-8";

	/** The logger to use. */
	private static final Logger logger  = LoggerFactory.getLogger(ResourceReader.class);

	/** The cached. */
	private Map<String, String> cached;

	/**
	 * Instantiates a new resources dr.
	 */
	private ResourceReader() {
		cached = new HashMap<String, String>();
	}

	/**
	 * Lazy-loaded Singleton, by Bill Pugh *.
	 */
	private static class Holder {

		/** The Constant INSTANCE. */
		private final static ResourceReader INSTANCE = new ResourceReader();
	}

	/**
	 * Gets the single instance of ResourcesDR.
	 *
	 * @return single instance of ResourcesDR
	 */
	public static ResourceReader instance() {
		return Holder.INSTANCE;
	}


	   /**
	    * Convert stream to string.
	    *
	    * @param is the is
	    * @param encoding the encoding
	    * @return the string
	    * @throws IOException Signals that an I/O exception has occurred.
	    */
	   public static String convertStreamToString(InputStream is, String encoding) throws IOException {
	     /*
	      * To convert the InputStream to String we use the
	      * Reader.read(char[] buffer) method. We iterate until the
	      * Reader return -1 which means there's no more data to
	      * read. We use the StringWriter class to produce the string.
	      */
	     if (is != null) {
	         Writer writer = new StringWriter();

	         char[] buffer = new char[512];
	         Reader reader = null;
	         try {
	             InputStreamReader isReader =  encoding != null ? new InputStreamReader(is, encoding) : new InputStreamReader(is);
	             reader = new BufferedReader(isReader);
	             int n;
	             while ((n = reader.read(buffer)) > 0) {
	                 writer.write(buffer, 0, n);
	             }
	         } finally {
	             reader.close();
	         }
	         return writer.toString();
	     } else {
	         return "";
	     }
	 }

	   /**
	 * Gets the resource.
	 *
	 * @param name the name
	 * @return the resource
	 */
	private String getResource(Class<?> clz, String name) {

		  String ret = cached.get(name);

		  if (null == ret) {
			  try {
				  InputStream stream = clz.getResourceAsStream(name);
				  ret = convertStreamToString(stream, DEFAULT_ENCODING);
			  } catch (IOException e) {
				  logger.error("",e);
				  ret = "";
			  }
			  cached.put(name, ret);
		  }

	      return ret;
	   }


	   /**
	 * Gets the template.
	 *
	 * @param name the name
	 * @return the template
	 */
	public static String resource(Class<?> clz, String name) {
		   return ResourceReader.instance().getResource(clz, name);
	 }





}
