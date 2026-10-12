package net.j7.commons.streams;

import java.io.*;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.j7.commons.base.CommonUtils;
import net.j7.commons.base.NotNull;

public class Streams {

	   /** The technical logger to use. */
	   private static final Logger logger = LoggerFactory.getLogger(Streams.class);

	private static final String DEFAULT_CHARSET = "UTF-8";
	/**
	 * The default size of the buffer.
	 */
	public static final int	DEFAULT_BUFFER_SIZE	= 256;
	/**
	 * @param os
	 * @param inquiry
	 * @throws IOException
	 */
	public static void stringToStream(OutputStream os, String text, String encoding) throws IOException {
			os.write(text.getBytes(Charset.forName(CommonUtils.nvl(encoding, DEFAULT_CHARSET))));
			os.flush();
	}

	   /**
	    * Convert stream to string.
	    *
	    * @param is the is
	    * @param encoding the encoding
	    * @return the string
	    * @throws IOException Signals that an I/O exception has occurred.
	    */
	public static  String convertStreamToString(InputStream is, String encoding) throws IOException {
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
	    * Copy bytes from an <code>InputStream</code> to an
	    * <code>OutputStream</code>.
	    * @param input the <code>InputStream</code> to read from
	    * @param output the <code>OutputStream</code> to write to
	    * @return the number of bytes copied
	    * @throws IOException In case of an I/O problem
	    */
	   public static final int copy(InputStream input, OutputStream output) throws IOException {
	       byte[]	buffer	= new byte[DEFAULT_BUFFER_SIZE];
	       int	count	= 0;
	       int	n;

	       while (-1 != (n = input.read(buffer))) {
	           output.write(buffer, 0, n);
	           count	+= n;
	       }

	       return count;
	   }

	   public static void close(Closeable closeable) {

		      if (closeable == null) {
		         return;
		      }
		      try {
		         closeable.close();
		      } catch (IOException e) {
		         logger.error("Error closing closeable", e);
		      }
		   }

		   public static void closeSilently(Closeable closeable) {

		      if (closeable == null) {
		         return;
		      }
		      try {
		         closeable.close();
		      } catch (IOException e) {
		         // silently
		      }
		   }


}
