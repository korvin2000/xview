/*
 * @File: ZipPacker.java
 *
 * 
 * 
 * All rights reserved.
 *
 * @Author:  tavrovsa
 *
 * @Version $Revision: #2 $Date: $
 *
 *
 */
package net.j7.commons.io.zip;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.j7.commons.strings.Args;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class ZipPacker.
 *
 * Used to create a ZIP format archive, with ability to<br>
 * compress a single file or a whole directory with all sub directories<br>
 * and ability to filter some unnecessary entries. See EntriesFilter interface<br>
 *
 * @author  tavrovsa
 * @version $Revision: #2 $
 */
public final class ZipPacker {


   /** The Constant MSG_FILTERED_ENTRY. */
   private static final String MSG_FILTERED_ENTRY = "Filtered entry: ";

   /** The Constant CLASS_AUTHOR. */
   private static final String CLASS_AUTHOR = "TavrovsA";

   /** The Constant DEFAULT_COMMENT. */
   private static final String DEFAULT_COMMENT = "Created by Korvin ZipPacker.%3Creation date: %1.%3Author: %2";

   /** The Constant MSG_COMPR_FILE. */
   private static final String MSG_COMPR_FILE = "Compression file: ";

   /** The Constant MSG_COMPR_DIR. */
   private static final String MSG_COMPR_DIR = "Compressing directory: ";

   /** The technical logger to use. */
   private static final Logger logger = LoggerFactory.getLogger(ZipPacker.class);

   /** The Constant ERROR_TARGET_ACHIVET_NAME_CAN_T_BE_EMPTY_OR_NULL. */
   private static final String ERROR_TARGET_ACHIVET_NAME_CAN_T_BE_EMPTY_OR_NULL = "Error: Target achivet name can't be empty or null";

   /** The Constant BUFFER. */
   public static final int BUFFER = 4096;

   /** The zip. */
   private ZipOutputStream zip;

   /** The data. */
   private byte[] data;


   /** The source. */
   private File source;

   /** The target. */
   private File target;

   /** The compression level. */
   private int compressionLevel;

   /** The verbose output. */
   private boolean verboseOutput;

   /** The file filter. */
   private EntriesFilter fileFilter;


   /** The Constant dateTime. */
   private final static SimpleDateFormat dateTime    = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

   /** The Constant time. */
   private final static SimpleDateFormat time    = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

   /**
    * Instantiates a new zip packer.
    *
    * @param targetArchive the target archive
    * @param sourceFileOrDir the source file or dir
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public ZipPacker( String targetArchive, String sourceFileOrDir) throws IOException{
      this(getTarget(targetArchive), new File(sourceFileOrDir));
   }

   /**
    * To log.
    *
    * @param v the v
    */
   private final void toLog(final String... v) {
      if (verboseOutput) {
         if (v.length==1) {
            logger.info(v[0]);
            //System.out.println(v[0]);
         } else {
           StringBuilder sb = new StringBuilder();

           for (String str : v) {
           if (sb.length()>0) sb.append(' ');
              sb.append(str);
           }
           logger.info(sb.toString());
           //System.out.println(sb.toString());
         }
      }
   }

   /**
    * Gets the target.
    *
    * @param targetArchive the target archive
    * @return the target
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private static File getTarget(String targetArchive) throws IOException{
     if (targetArchive==null || targetArchive.isEmpty()) throw new IOException(ERROR_TARGET_ACHIVET_NAME_CAN_T_BE_EMPTY_OR_NULL);

     targetArchive = targetArchive.trim();
     if (targetArchive.endsWith("\\") || targetArchive.endsWith("/") || targetArchive.endsWith("."))
        targetArchive = targetArchive.substring(0, targetArchive.length()-1);

     int dotPos = targetArchive.lastIndexOf('.');
     int fs = max(targetArchive.lastIndexOf('\\'),targetArchive.lastIndexOf('/'));

     boolean hasExt = dotPos > fs;

     String zipName =  hasExt ? targetArchive : targetArchive + ".zip";
     return new File(zipName);
   }

   /**
    * Max.
    *
    * @param v1 the v1
    * @param v2 the v2
    * @return the int
    */
   private static int max(int v1, int v2) {
      return v1 > v2 ? v1 : v2;
   }


   /**
    * Instantiates a new zip packer.
    *
    * @param targetArchive the target archive
    * @param sourceFileOrDir the source file or dir
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public ZipPacker(File targetArchive, File sourceFileOrDir) throws IOException{
      this.source = sourceFileOrDir;
      this.target = targetArchive;

      this.data = new byte[BUFFER];
      this.compressionLevel = Deflater.DEFAULT_COMPRESSION;
      this.verboseOutput = true;
   }


   /**
    * Compress.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void compress() throws IOException {
      FileOutputStream destStream = new FileOutputStream(target);
      zip = new ZipOutputStream(new BufferedOutputStream(destStream));
      zip.setLevel(compressionLevel);
      zip.setComment(Args.fill(DEFAULT_COMMENT,dateTime.format(new Date()), CLASS_AUTHOR, "\r\n"));

      toLog(Args.fill("Creating archive: %1 Start time: %2", target.getName(), time.format(new Date())));
      long start = System.currentTimeMillis();

      compressDirectory(source, "");
      zip.close();
      destStream.close();

      long end = System.currentTimeMillis();
      toLog(Args.fill("Compression done. Finish time: %1. Compression time: %2 second(s)", time.format(new Date()),((end-start)/1000)));

   }



   /**
    * Compress directory.
    *
    * @param entry the entry
    * @param name the name
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private final void compressDirectory(File entry,  String name)throws IOException  {

      if (entry == null || !entry.exists()) return ;

      if (entry.isDirectory()) {
         //    add empty directory
         if (!name.isEmpty()) {
         toLog(MSG_COMPR_DIR,name);
         zip.putNextEntry(new ZipEntry(name + "/"));
         zip.closeEntry();
         }
         final String[] children = entry.list();

         if (null != children)
         for (String childEntry : children) {

            String childName = !name.isEmpty() ? name + "/" + childEntry : childEntry;
            // initiate recursive call
            compressDirectory(new File(entry, childEntry), childName);
         }


      } else {

         if (fileFilter != null && fileFilter.isFiltered(entry)) {
            toLog(MSG_FILTERED_ENTRY,entry.getName());
            return ;
         }

         BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(entry), BUFFER);

         // Add ZIP entry to output stream.
         toLog(MSG_COMPR_FILE,name);
         ZipEntry zipEntry = new ZipEntry(name);
         zip.putNextEntry(zipEntry);
         int count;
        // Transfer bytes from the file to the ZIP file
         while((count = inputStream.read(data,0,BUFFER)) > 0)
         {
           zip.write(data,0,count);
         }

         // Complete the entry
         zip.closeEntry();
         inputStream.close();
      }

   }



   /**
    * Gets the compression level.
    *
    * @return Returns the compressionLevel.
    */
   public int getCompressionLevel() {

      return compressionLevel;
   }



   /**
    * Sets the compression level.
    *
    * @param compressionLevel - the compression level (0 (store) - 9 (maximal))<br>
    * default value - Deflater.DEFAULT_COMPRESSION
    */
   public void setCompressionLevel(int compressionLevel) {

      this.compressionLevel = compressionLevel;
   }



   /**
    * Checks if is verbose output.
    *
    * @return Returns the verboseOutput.
    */
   public boolean isVerboseOutput() {

      return verboseOutput;
   }



   /**
    * Sets the verbose output.
    *
    * @param verboseOutput The verboseOutput to set.
    * Default is on (enabled)
    */
   public void setVerboseOutput(boolean verboseOutput) {

      this.verboseOutput = verboseOutput;
   }

   /**
    * The Interface EntriesFilter.
    */
   public static interface EntriesFilter {

     /**
      * Checks if is filtered.
      *
      * @param entry the entry
      * @return true, if is filtered
      */
     public boolean isFiltered(File entry);

   }


   /**
    * Gets the file filter.
    *
    * @return Returns the fileFilter.
    */
   public EntriesFilter getFileFilter() {

      return fileFilter;
   }


   /**
    * Filter is used to filter ( don't add to archive ) some file types<br>
    * which doesn't make any sense to add in dest. archive,<br>
    * e.G. temporary files, empty files, other archives, etc.
    *
    * Sets the file filter.
    *
    * @param fileFilter The fileFilter to set.
    */
   public void setFileFilter(EntriesFilter fileFilter) {

      this.fileFilter = fileFilter;
   }


}
