/*
 * @File: TextFileUtils.java
 *
 *
 *
 * All rights reserved.
 *
 * @Author:  tavrovsa
 *
 * @Version $Revision: #3 $Date: $
 *
 *
 */
package net.j7.commons.io;

import java.io.*;
import java.nio.CharBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class TextFileUtils - to read and write text files in easy way.
 *
 * @author  tavrovsa
 * @version $Revision: #3 $
 */
public class TextFileUtils {


	private static final Logger log = LoggerFactory.getLogger(TextFileUtils.class);

   /**
    * Creates the text reader.
    *
    * @param fileName the file name
    * @return the text reader
    * @throws IOException Signals that an I/O exception has occurred.
    * @see AbrstractFileParser for easy reading of text files line by line
    */
   public static TextReader createTextReader(String fileName) throws IOException {
      return createTextReader(fileName, null);
   }


   /**
    * Instantiates a new text reader.
    *
    * @param fileName the file name
    * @param encoding the encoding
    * @return the text reader
    * @throws IOException the text file exception
    * @see AbrstractFileParser for easy reading of text files line by line
    */
   public static TextReader createTextReader(String fileName, String encoding) throws IOException {
      TextReader reader = new TextReader(fileName, encoding);
      return reader;
   }


   /**
    * Instantiates a new text reader.
    *
    * @param fileStream the file stream
    * @param encoding the encoding
    * @return the text reader
    * @throws IOException the text file exception
    * @see AbrstractFileParser for easy reading of text files line by line
    */
   public static TextReader createTextReader(InputStream fileStream, String encoding) throws IOException {
      TextReader reader = new TextReader(fileStream, encoding);
      return reader;
   }


   /**
    * Creates the text reader.
    *
    * @param fileStream the file stream
    * @return the text reader
    * @throws IOException Signals that an I/O exception has occurred.
    * @see AbrstractFileParser for easy reading of text files line by line
    */
   public static TextReader createTextReader(InputStream fileStream) throws IOException {
      return createTextReader(fileStream, null);
   }


   /**
    * Creates the text reader.
    *
    * @param fileToRead the file to read
    * @return the text reader
    * @throws IOException Signals that an I/O exception has occurred.
    * @see AbrstractFileParser for easy reading of text files line by line
    */
   public static TextReader createTextReader(File fileToRead) throws IOException {
      return createTextReader(fileToRead, null);
   }


   /**
    * Instantiates a new text reader.
    *
    * @param fileToRead the file to read
    * @param encoding the encoding
    * @return the text reader
    * @throws IOException the text file exception
    * @see AbrstractFileParser for easy reading of text files line by line
    */
   public static TextReader createTextReader(File fileToRead, String encoding) throws IOException {
      TextReader reader = new TextReader(fileToRead, encoding);
      return reader;
   }


   /**
    * Creates the text writer.
    *
    * @param fileName the file name
    * @param encoding the encoding
    * @return the text writer
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static TextWriter createTextWriter(String fileName, String encoding, boolean append) throws IOException {
      TextWriter writer = new TextWriter(fileName, encoding, append);
      return writer;
   }


   /**
    * Creates the text writer.
    *
    * @param fileName the file name
    * @return the text writer
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static TextWriter createTextWriter(String fileName, boolean append) throws IOException {
      TextWriter writer = new TextWriter(fileName, append);
      return writer;
   }


   /**
    * Creates the text writer.
    *
    * @param outStream the out stream
    * @param encoding the encoding
    * @return the text writer
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static TextWriter createTextWriter(OutputStream outStream, String encoding) throws IOException {
      TextWriter writer = new TextWriter(outStream, encoding);
      return writer;
   }


   /**
    * Creates the text writer.
    *
    * @param outStream the out stream
    * @return the text writer
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static TextWriter createTextWriter(OutputStream outStream) throws IOException {
      return createTextWriter(outStream, null);
   }

   /**
    * Creates the text writer.
    *
    * @param fileToWrite the file to write
    * @param encoding the encoding
    * @return the text writer
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static TextWriter createTextWriter(File fileToWrite, String encoding) throws IOException {
      TextWriter writer = new TextWriter(fileToWrite, encoding);
      return writer;
   }


   /**
    * Creates the text writer.
    *
    * @param fileToWrite the file to write
    * @return the text writer
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static TextWriter createTextWriter(File fileToWrite) throws IOException {
      return createTextWriter(fileToWrite, null);
   }

   /**
    * The Class TextReader.
    */
   public static class TextReader implements Readable, Closeable{

      /** The Constant ERROR_CANT_FIND_FILE. */
      private static final String ERROR_CANT_FIND_FILE = "Can't find file: ";

      /** The Constant ERROR_UNSUPPORTED_ENCODING. */
      private static final String ERROR_UNSUPPORTED_ENCODING = "Unsupported Encoding: ";
      /** The reader. */
      private final BufferedReader reader;


      /**
       * Instantiates a new text reader.
       *
       * @param stream the stream
       * @throws IOException the text file exception
       */
      public TextReader(InputStream stream) throws IOException {
         this(stream, null);
      }

      /**
       * Instantiates a new text reader.
       *
       * @param stream the stream
       * @param charset the charset
       * @throws IOException the text file exception
       */
      public TextReader(InputStream stream, String charset) throws IOException {
         try {
            final InputStreamReader isReader =  charset == null ? new InputStreamReader(stream) : new InputStreamReader(stream, charset) ;
            reader = new BufferedReader( isReader );

         } catch (Exception e) {
            throw new IOException(ERROR_UNSUPPORTED_ENCODING+charset, e);
         }
      }

      /**
       * Instantiates a new text reader.
       *
       * @param fileName the file name
       * @throws IOException the text file exception
       */
      public TextReader(String fileName) throws IOException {
         this(fileName, null);
      }

      /**
       * Instantiates a new text reader.
       *
       * @param fileName the file name
       * @param encoding the encoding
       * @throws IOException the text file exception
       */
      public TextReader(String fileName, String encoding) throws IOException {
         this(fileAsStream(fileName), encoding);
      }

      /**
       * Instantiates a new text reader.
       *
       * @param fileName the file name
       * @throws IOException the text file exception
       */
      public TextReader(File fileName) throws IOException {
         this(fileAsStream(fileName), null);
      }

      /**
       * Instantiates a new text reader.
       *
       * @param fileName the file name
       * @param encoding the encoding
       * @throws IOException the text file exception
       */
      public TextReader(File fileName, String encoding) throws IOException {
         this(fileAsStream(fileName), encoding);
      }


      /**
       * Reads next char from file if available or returns -1.
       *
       * @return the int
       * @throws IOException Signals that an I/O exception has occurred.
       */
      public int read() throws IOException {
         return reader.read();
      }

      /**
       * Reads characters into a portion of an array.
       *
       * @param cbuf the cbuf
       * @param off the off
       * @param len the len
       * @return the int
       * @throws IOException Signals that an I/O exception has occurred.
       */
      public int read(char cbuf[], int off, int len) throws IOException {
         return reader.read(cbuf, off, len);
      }

      /**
       * Read line.
       *
       * @return the string
       * @throws IOException Signals that an I/O exception has occurred.
       */
      public String readLine() throws IOException {
         return reader.readLine();
      }


      /**
       * Read chars.
       *
       * @param length the length
       * @return the char sequence
       * @throws IOException Signals that an I/O exception has occurred.
       */
      public CharSequence readChars(int length) throws IOException {
         StringBuilder sb = new StringBuilder(length);
         int read;
         while ((read = reader.read())!=-1 && sb.length() < length)
            sb.append((char)read);
         return sb;
      }


      public String readAll() {
    	 final char[] buf = new char[512];
    	 StringBuilder sb = new StringBuilder();
    	 try {
			int r;
    		 while ((r = reader.read(buf))>0) {
    			 sb.append(buf, 0, r);
    		 }
		} catch (IOException e) {//ignore//
		}
    	 return sb.toString();
      }

      /**
       * Mark.
       *
       * @param readAheadLimit the read ahead limit
       * @throws IOException Signals that an I/O exception has occurred.
       */
      public void mark(int readAheadLimit) throws IOException {
         reader.mark(readAheadLimit);
      }

      /**
       * Close.
       */
      @Override
      public void close()  {
         if (reader != null)
            try {
               reader.close();
            } catch (IOException e) {/*ignore*/}
      }



      /**
       * File as stream.
       *
       * @param fileName the file name
       * @return the input stream
       * @throws IOException the text file exception
       */
      private final static InputStream fileAsStream(String fileName)  throws IOException {
         try {
            return new FileInputStream(fileName);
         } catch (FileNotFoundException e) {
            throw new IOException(ERROR_CANT_FIND_FILE+fileName, e);
         }
      }

      /**
       * File as stream.
       *
       * @param textFile the text file
       * @return the input stream
       * @throws IOException the text file exception
       */
      private final static InputStream fileAsStream(File textFile)  throws IOException {
         try {
            return new FileInputStream(textFile);
         } catch (FileNotFoundException e) {
            throw new IOException(ERROR_CANT_FIND_FILE+textFile, e);
         }
      }

      /* (non-Javadoc)
       * @see java.lang.Readable#read(java.nio.CharBuffer)
       */
      @Override
      public int read(CharBuffer cb) throws IOException {
         return this.reader.read(cb);
      }

   }



   /**
    * The Class TextReader.
    */
   public static class TextWriter implements Appendable, Closeable, Flushable {

      /** The Constant ERROR_UNSUPPORTED_ENCODING. */
      private static final String ERROR_UNSUPPORTED_ENCODING = "Unsupported Encoding: ";

      /** The Constant ERROR_CREATE_FILE. */
      private static final String ERROR_CREATE_FILE = "Can't create file: ";
      /** The reader. */
      private final BufferedWriter writer;


      /**
       * Instantiates a new text reader.
       *
       * @param stream the stream
       * @throws IOException the text file exception
       */
      public TextWriter(OutputStream stream) throws IOException {
         this(stream, null);
      }

      /**
       * Instantiates a new text reader.
       *
       * @param stream the stream
       * @param charset the charset
       * @throws IOException the text file exception
       */
      public TextWriter(OutputStream stream, String charset) throws IOException {
         try {
            final OutputStreamWriter isReader =  charset == null ? new OutputStreamWriter(stream) : new OutputStreamWriter(stream, charset) ;
            writer = new BufferedWriter( isReader );

         } catch (Exception e) {
            throw new IOException(ERROR_UNSUPPORTED_ENCODING+charset, e);
         }
      }

      /**
       * Instantiates a new text reader.
       *
       * @param fileName the file name
       * @throws IOException the text file exception
       */
      public TextWriter(String fileName, boolean append) throws IOException {
         this(fileName, null, append);
      }

      /**
       * Instantiates a new text reader.
       *
       * @param fileName the file name
       * @param encoding the encoding
       * @throws IOException the text file exception
       */
      public TextWriter(String fileName, String encoding, boolean append) throws IOException {
         this(fileAsStream(fileName, append), encoding);
      }

      /**
       * Instantiates a new text reader.
       *
       * @param fileName the file name
       * @throws IOException the text file exception
       */
      public TextWriter(File fileName) throws IOException {
         this(fileAsStream(fileName), null);
      }

      /**
       * Instantiates a new text reader.
       *
       * @param fileName the file name
       * @param encoding the encoding
       * @throws IOException the text file exception
       */
      public TextWriter(File fileName, String encoding) throws IOException {
         this(fileAsStream(fileName), encoding);
      }


      /**
       * Write.
       *
       * @param c the c
       * @throws IOException Signals that an I/O exception has occurred.
       */
      public void write(int c) throws IOException {
         writer.write(c);

      }


      /**
       * New line.
       *
       * @throws IOException Signals that an I/O exception has occurred.
       */
      public void newLine() throws IOException {
         writer.newLine();
      }


      /**
       * Write.
       *
       * @param str the str
       * @throws IOException Signals that an I/O exception has occurred.
       */
      public void write(String str) throws IOException {
         writer.write(str);
      }

      /**
       * Write ln.
       *
       * @param str the str
       * @throws IOException Signals that an I/O exception has occurred.
       */
      public void writeLn(String str) throws IOException {
         writer.write(str);
         writer.newLine();
      }


      /**
       * Append.
       *
       * @param csq the csq
       * @return the writer
       * @throws IOException Signals that an I/O exception has occurred.
       */
      @Override
      public Writer append(CharSequence csq) throws IOException {
         return writer.append(csq);
      }


      /**
       * Append.
       *
       * @param aChar the a char
       * @return the writer
       * @throws IOException Signals that an I/O exception has occurred.
       */
      @Override
      public Writer append(char aChar) throws IOException {
         return writer.append(aChar);
      }


      /**
       * Flush.
       * @throws IOException Signals that an I/O exception has occurred.
       */
      @Override
      public void flush() throws IOException {
         writer.flush();
      }


      /**
       * Close.
       */
      @Override
      public void close()  {
         if (writer != null)
            try {
               writer.close();
            } catch (IOException e) {/*ignore*/}
      }



      /**
       * File as stream.
       *
       * @param fileName the file name
       * @return the input stream
       * @throws IOException the text file exception
       */
      private final static OutputStream fileAsStream(String fileName, boolean append)  throws IOException {
         try {
            return new FileOutputStream(fileName, append);
         } catch (FileNotFoundException e) {
            throw new IOException(ERROR_CREATE_FILE+fileName, e);
         }
      }

      /**
       * File as stream.
       *
       * @param textFile the text file
       * @return the input stream
       * @throws IOException the text file exception
       */
      private final static OutputStream fileAsStream(File textFile)  throws IOException {
         try {
            return new FileOutputStream(textFile);
         } catch (FileNotFoundException e) {
            throw new IOException(ERROR_CREATE_FILE+textFile, e);
         }
      }

      /* (non-Javadoc)
       * @see java.lang.Appendable#append(java.lang.CharSequence, int, int)
       */
      @Override
      public Appendable append(CharSequence csq, int start, int end) throws IOException {
         return this.writer.append(csq, start, end);
      }

   }


   /**
    * The Class AbrstractFileParser.
    */
   public abstract static class AbrstractFileParser {

      /** The reader. */
      private final TextReader reader;

      /**
       * Instantiates a new abrstract file parser.
       *
       * @param reader the reader
       */
      public AbrstractFileParser(TextReader reader) {
         this.reader = reader;
      }


      /**
       * Instantiates a new abrstract file parser.
       *
       * @param fileName the file name
       * @throws IOException Signals that an I/O exception has occurred.
       */
      public AbrstractFileParser(String fileName, String encoding) throws IOException {
         this.reader = TextFileUtils.createTextReader(fileName, encoding);
      }

      /**
       * Instantiates a new abrstract file parser.
       *
       * @param fileName the file name
       * @throws IOException Signals that an I/O exception has occurred.
       */
      public AbrstractFileParser(String fileName) throws IOException {
         this.reader = TextFileUtils.createTextReader(fileName);
      }

      /**
       * Instantiates a new abrstract file parser.
       *
       * @param fileName the file name
       * @throws IOException Signals that an I/O exception has occurred.
       */
      public AbrstractFileParser(File fileName) throws IOException {
         this.reader = TextFileUtils.createTextReader(fileName);
      }

      /**
       * Instantiates a new abrstract file parser.
       *
       * @param stream the stream
       * @throws IOException Signals that an I/O exception has occurred.
       */
      public AbrstractFileParser(InputStream stream) throws IOException {
         this.reader = TextFileUtils.createTextReader(stream);
      }

      /**
       * Read.
     * @throws Exception
       */
      public void read() throws Exception {
         try {
            String line;
            int num = 0;
            while ((line = this.reader.readLine()) != null) {
               processLine(line, ++num);
            }
         } catch (IOException e) {
        	 log.error("IOException: ", e);
         }

      }

      /**
       * Process line.
       *
       * @param line the line
       * @param lineNumber the line number
       */
      public abstract void processLine(String line, int lineNumber) throws Exception;

      /**
       * Close.
       */
      public void close() {
         if (null != reader) reader.close();
      }
   }


   public static boolean empty(String value) {
	   return value == null || value.trim().isEmpty();
   }

   /**
    * Fill string with given char to given length.
    * Example:
    * a = fillString("123",6,'0');
    * result> a = 000123
    *
    * @param str the string
    * @param length the length
    * @param toFill the char to fill
    *
    * @return the string
    */
   public final static String fillString(final String str, final int length, final char toFill) {
       if (null == str) return str;
       StringBuilder sb	 = new StringBuilder(length);

       for (int	diff	= length - str.length(), i = diff; i > 0; i--) sb.append(toFill);
       sb.append(str);
       return sb.toString();
   }


}
