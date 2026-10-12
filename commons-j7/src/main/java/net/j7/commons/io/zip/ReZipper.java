package net.j7.commons.io.zip;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.*;

import net.j7.commons.io.FileUtils;
import net.j7.commons.io.helpers.WildcardMatcher;
import net.j7.commons.strings.Args;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @File: AbstractFileProcessor.java
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




/**
 * The Class AbstractFileProcessor.
 * @author  tavrovsa
 */
public abstract class ReZipper {

   /** The technical logger to use. */
   private static final Logger logger = LoggerFactory.getLogger(ReZipper.class);

   /** The Constant ALL_FILES. */
   private final static String ALL_FILES = "*.*";

   /** The base path. */
   public File rootPath;

   /** The processed file count. */
   private int processedFiles;

   /** The processed dirs. */
   private int processedDirs;

   /** The wildcard. */
   private WildcardMask wildcard;

   /** The flag stopped. */
   private boolean flagStopped;


   /** The stop on exception. */
   private boolean stopOnException = false;

   private ZipFile zipFile;
   private boolean zipFileOpen;
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


   /** The Constant BUFFER. */
   public static final int BUFFER = 4096;

   /** The zip. */
   private ZipOutputStream zip;

   /** The data. */
   private byte[] data;


   /** The target. */
   private File target;

   /** The compression level. */
   private int compressionLevel;

   /** The file filter. */
   private EntriesFilter fileFilter;


   /** The Constant dateTime. */
   private final static SimpleDateFormat dateTime    = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

   /** The Constant time. */
   private final static SimpleDateFormat time    = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

   /**
    * Instantiates a new abstract file processor.
    *
    * @param basePath the base path
    */
   public ReZipper(String sourcePath, String targetPath) {
      this(sourcePath, targetPath,  ALL_FILES);
   }

   /**
    * Instantiates a new abstract file processor.
    *
    * @param sourcePath the base path
    * @param wildcardMask the wildcard mask
    */
   public ReZipper(String sourcePath, String targetPath, String wildcardMask) {

      this.rootPath = new File(sourcePath);
      this.target = new File(targetPath);

      if (!rootPath.exists()) {
         rootPath = new File(FileUtils.correctPath(sourcePath));
      }

      this.wildcard = new WildcardMask(wildcardMask);
      this.flagStopped = false;

      this.data = new byte[BUFFER];
      this.compressionLevel = Deflater.DEFAULT_COMPRESSION;
   }

   /**
    * Gets the files processed.
    *
    * @return Returns the processed.
    */
   public int getCountFilesProcessed() {
      return processedFiles;
   }

   /**
    * you could override this method in child classes
    * if this method returns false -> this file will be skipped / omited.
    *
    * @param entry the entry
    * @param fileData the file data
    * @return true, if successful
    */
   public boolean accept(ZipEntry entry, EntryInfo fileData) {
      return true;
   }


   /**
    * you could override this method in child classes
    * if this method returns true -> this file will be skipped / omited.
    *
    * @param entry the entry
    * @param fileData the file data
    * @return true, if successful
    */
   public boolean skip(ZipEntry entry, EntryInfo fileData) {
      return false;
   }

   /**
    * Stop processing.
    */
   public void stopProcessing() {
      this.flagStopped = true;
   }


   /**
    * Process a file.
    *
    * @param file the file
    * @param EntryInfo the file info
    * @throws Exception the exception
    * @see stopProcessing()
    */
   public void processEntry(ZipEntry entry, EntryInfo entryInfo) throws Exception {

      if (fileFilter != null && fileFilter.isFiltered(entryInfo)) {
         logger.info(MSG_FILTERED_ENTRY,entry.getName());
         return ;
      }

      BufferedInputStream inputStream = new BufferedInputStream(entryInfo.getDataInputStream(), BUFFER);

      String name  = entryInfo.getNameWithPath();

      // Add ZIP entry to output stream.
      logger.info(MSG_COMPR_FILE,name);
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

   /**
    * Start.
    * @throws IOException
    */
   public void process() throws IOException {
      checkIfOpen();
      zipFile = new ZipFile(rootPath, ZipFile.OPEN_READ);
      zipFileOpen = true;





      FileOutputStream destStream = new FileOutputStream(target);
      zip = new ZipOutputStream(new BufferedOutputStream(destStream));
      zip.setLevel(compressionLevel);
      zip.setComment(Args.fill(DEFAULT_COMMENT,dateTime.format(new Date()), CLASS_AUTHOR, "\r\n"));

      logger.info(Args.fill("Creating archive: %1 Start time: %2", target.getName(), time.format(new Date())));
      long start = System.currentTimeMillis();

      listContents(zipFile);

      zip.close();
      destStream.close();

      long end = System.currentTimeMillis();
      logger.info(Args.fill("Compression done. Finish time: %1. Compression time: %2 second(s)", time.format(new Date()),((end-start)/1000)));



   }

   public void close() {
      checkIfOpen();
   }

   private final void checkIfOpen() {
      if (zipFileOpen && zipFile!=null) {
         zipFileOpen = false;
         zipFile = null;
      }
   }

   /**
    * List contents.
    *
    * @param entry the entry
    * @param recurse the recurse
    * @throws IOException
    */
   private void listContents(ZipFile zipFile) throws IOException {

      if (flagStopped || zipFile==null) return ;

      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
         ZipEntry entry = entries.nextElement();
         if (entry.isDirectory()) {
            addDirectory(entry);
            this.processedDirs++;
            continue;
         }

         doFileIntern(entry);

      }

   }

   private void addDirectory(ZipEntry entry) throws IOException {
      String name = entry.getName();
      //    add empty directory
      if (!name.isEmpty()) {
      logger.info(MSG_COMPR_DIR,name);
      zip.putNextEntry(new ZipEntry(name + "/"));
      zip.closeEntry();
      }

   }

   /**
    * process file internally.
    *
    * @param entry the entry
    */
   private void doFileIntern(ZipEntry entry) {

      final String fullName = entry.getName();

      if (!wildcard.accept(fullName)) return ;

      EntryInfo fileData = new EntryInfo(entry);
      if (skip(entry, fileData) || !accept(entry, fileData)) return ;

      try {
         processEntry(entry, fileData);
         processedFiles++;
      } catch (Exception e) {
         logger.error("Error processing file", e);
         if (stopOnException) stopProcessing();
      }


   }

   /**
    * The Class EntryInfo.
    */
   public class EntryInfo {

      /** The file. */
      private ZipEntry file;

      /** The full name. */
      private String fullName;

      /**
       * Instantiates a new file info.
       *
       * @param file the file
       */
      public EntryInfo(ZipEntry file) {
         this.file = file;
         this.fullName = file.getName();
      }


      public InputStream getDataInputStream() throws IOException {
         InputStream is = zipFile.getInputStream(file);
         return is;
      }

      public String getFileData() throws IOException {

         InputStream is = zipFile.getInputStream(file);
         int size = is.available();

         char[] cbuf = new char[size];
         InputStreamReader reader = new InputStreamReader(is, "UTF-8");
         reader.read(cbuf, 0, size);

         String str = new String(cbuf);
         return str;



//         InputStream is = zipFile.getInputStream(file);
//         int size = is.available();
//         byte[] buf = new byte[size];
//         is.read(buf, 0, size);
//         String str = new String(buf);
//         return str;

//         BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(file));
//
//         StringBuilder sb = new StringBuilder(4096);
//
//         int size;
//         byte[] buffer = new byte[512];
//         BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fullName), buffer.length);
//
//         while ((size = bis.read(buffer, 0, buffer.length)) != -1)
//         {
//            sb.append((char[])buffer, 0, size);
//         }
//         bos.flush();
//         bos.close();
      }

      /**
       * Gets the name minus the path from a full filename.
       *
       * @return the file name
       */
      public String getFileName() {
         return FileUtils.getFileName(fullName);
      }

      /**
       * Gets the base name, minus the full path and extension, from a full filename.
       *
       * @return the file name base
       */
      public String getFileNameBase() {
         return FileUtils.getBaseName(fullName);
      }

      /**
       * Gets the extension of a filename.
       * @return extension of a filename.
       */
      public String getExtension() {
         return FileUtils.getExtension(fullName);
      }


      /**
       * Gets the file.
       *
       * @return Returns the file.
       */
      public ZipEntry getEntry() {
         return file;
      }

      /**
       * Gets the file size.
       * @return the file size
       */
      public long getFileSize() {
         return file.getSize();
      }

      /**
       *  Gets the name with extension (if exists) <br>
       *  plus the path from a full filename.
       *
       * @return Returns the fullName with path
       */
      public String getNameWithPath() {
         return fullName;
      }

      /**
       * Does the work of getting the path.
       *
       * @return the path
       */
      public String getPath() {
        return FileUtils.getFilePath(fullName, false);
      }


      /**
       * Gets the relative path (without base Path).
       *
       * @return the path relative
       */
      public String getPathRelative() {
         String fullPath = getPath();


         String relativePath = fullPath.startsWith(rootPath.getPath())
               ? fullPath.substring(rootPath.getPath().length() + 1)
               : fullPath;

         return relativePath;
      }

   }


   /**
    * The Class WildcardMask.
    */
   private static class WildcardMask {

      /** The wildcard. */
      private String wildcard;

      /** The no filter. */
      private boolean noFilter;

      /**
       * Instantiates a new wildcard mask.
       *
       * @param mask the mask
       */
      public WildcardMask(String mask) {
         if (mask==null || mask.isEmpty()) mask = ALL_FILES;
         this.wildcard = mask;
         this.noFilter = ALL_FILES.equals(mask) || "*".equals(mask);
      }

      /**
       * Accept.
       *
       * @param name the name
       * @return true, if successful
       */
      public boolean accept(String name) {
         return noFilter ? true : WildcardMatcher.wildcardMatch(name, wildcard);
      }

   }



   /**
    * Checks if is stop on exception.
    *
    * @return Returns the stopOnException.
    */
   public boolean isStopOnException() {

      return stopOnException;
   }


   /**
    * Sets the stop on exception.
    *
    * @param stopOnException The stopOnException to set.
    */
   public void setStopOnException(boolean stopOnException) {

      this.stopOnException = stopOnException;
   }


   /**
    * @return Returns the processedDirs.
    */
   public int getProcessedDirsCount() {

      return processedDirs;
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
     public boolean isFiltered(EntryInfo entry);

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
