package net.j7.commons.reflection;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.j7.commons.streams.Streams;

import org.eclipse.osgi.storage.url.BundleURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for loading resources automatically.
 * @author tavrovsa
 */
@SuppressWarnings("unused")
public abstract class ResourceLoader {

   /** The technical logger to use. */
	   /** The technical logger to use. */
	   private static final Logger logger = LoggerFactory.getLogger(ReflectionHelper.class);

   /** The Constant STR_SP1. */
   private static final transient String SLASH = "/";
   private static final transient String BACKSLASH = "\\";

   /** The Constant BaseLine DOT */
   private static final transient String BL_DOT = ".";

   /** The Constant WARCLS_PATH. */
   private static final String WARCLS_PATH = "WEB-INF/classes/";

   private enum Protocol {
	   EclipseBundle("bundleresource"), /** URL protocol for an entry from a bundle Resource eclipse. */
	   JAR("jar"), /** URL protocol for an entry from a jar file: "jar". */
	   ZIP("zip"), /** URL protocol for an entry from a zip file: "zip". */
	   FILE("file"), /** URL protocol for a file in the file system: "file". */
	   VFSFILE("vfsfile"), /** URL protocol for a file in the file system (JBOSS): "file". */
	   VFSZIP("vfszip"), /** URL protocol for an entry from a JBoss jar file: "vfszip". */
	   WSJAR("wsjar"),  /** URL protocol for an entry from a WebSphere jar file: "wsjar". */
	   CODE_SOURCE("code-source");    /** URL protocol for an entry from an OC4J jar file: "code-source". */

   String id; Protocol(String id) { this.id = id;  }

   boolean is(String protocol) {
	   return id.equals(protocol);
   }
   };

   public enum Method {EclipseBundle, FILE, JAR, WSJAR, ZIP, CODE_SOURCE;

   boolean is(String protocol) {return is(protocol, null);}

   boolean is(String protocol, String url) {
	   switch (this) {
	   case EclipseBundle:
		   return Protocol.EclipseBundle.is(protocol);
	   case FILE:
		   return Protocol.FILE.is(protocol) || Protocol.VFSFILE.is(protocol);
	   case JAR:
		   if (Protocol.JAR.is(protocol) || Protocol.VFSZIP.is(protocol)) return true;
		   if (Protocol.ZIP.is(protocol) && (url != null && url.contains("jar!"))) return true;
		   return false;
	   case WSJAR:
		   return Protocol.WSJAR.is(protocol);
	   case ZIP:
		   if (Protocol.ZIP.is(protocol) && (url == null || !url.contains("jar!"))) return true;
		   return false;
	   case CODE_SOURCE:
		   return Protocol.CODE_SOURCE.is(protocol);
	   default:
		return false;
	}
   }
   };


   /** Separator between JAR URL and file path within the JAR. */
   private static final String JAR_URL_SEPARATOR = "!/";

   /** URL prefix for loading from the file system: "file:". */
   private static final String FILE_URL_PREFIX = "file:";

   /** The Constant CHARSET. */
   private static final String CHARSET = "UTF-8";

   private static final String EXT_CLASS = ".class";

   /**
    * Gets the name.
    *
    * @param fileName the file name
    * @return the name
    */
   protected static String getName(String fileName) {

      char ch;
      int num = 0, start = 0 ,  end = 0;
      for (int i = fileName.length() - 1; i >= 0; i--) {
         ch = fileName.charAt(i);
         if (ch == '.') {
            num++;
            if (1 == num) {
               end = i;
            } else if (2 == num) {
               start = i + 1;
               break;
            }
         }
      }
      return fileName.substring(start, end);
   }

   /**
    * This method finds all classes that are located in the package identified by the given <code>packageName</code>.<br>
    * <b>ATTENTION:</b><br>
    * This is a relative expensive operation. Depending on your classpath multiple directories,JAR-, and WAR-files may
    * need to be scanned. <br>
    * Code written by Jorg Hohwiller for the m-m-m project ({@link http://m-m-m.sf.net})
    *
    * @param path is the name of the {@link Package} to scan.
    * @param type the type
    * @param src the src
    * @throws ExporterException the exporter exception
    */
   public final void findExt(String path) throws IOException {
         try {
            find0(path);
         } catch (IOException e) {
            throw new IOException(e);
         }

   }

   /**
    * Process.
    *
    * @param is the is
    * @param fileName the file name
    * @throws Exception the exception
    */
   public abstract void process(InputStream is, String fileName) throws Exception;

   /**
    * Right file.
    *
    * @param fileName the file name
    * @return true, if successful
    */
   public abstract boolean rightFile(String fileName);

   /**
    * Check found file.
    *
    * @param className the class name
    * @return true, if successful
    */
   private final boolean checkFoundFile(String className) {

      return !className.endsWith(ResourceLoader.BL_DOT) ? rightFile(className) : false;
   }

   /**
    * Find.
    *
    * @param packageName the package name
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private final void find0(String packageName) throws IOException {

      // HashSet<Class> classSet = new HashSet<Class>();
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      String path = packageName.replace('.', '/');
      String pathWithPrefix = path + '/';
      Enumeration<URL> urls = classLoader.getResources(path);

      StringBuilder qnb = new StringBuilder(packageName);
      qnb.append('.');
      int qualifiedNamePrefixLength = qnb.length();
      while (urls.hasMoreElements()) {
         URL packageUrl = urls.nextElement();
         String urlString = URLDecoder.decode(packageUrl.getFile(), ResourceLoader.CHARSET);
         String protocol = packageUrl.getProtocol().toLowerCase();

         if (Method.EclipseBundle.is(protocol)) {
        	 try {
        		 BundleURLConnection connection = (BundleURLConnection) packageUrl.openConnection();
        		 packageUrl = connection.getFileURL();
        		 urlString = packageUrl.getPath();
        		 protocol = packageUrl.getProtocol();
        	 } catch (Exception e) {
        		 logger.info("This module is not intended for use under Eclipse");
        	 }
         } else
         if (Method.FILE.is(protocol)) {
            File packageDirectory = new File(urlString);
            if (packageDirectory.isDirectory()) {
            	findFileNamesRecursive(packageDirectory, qnb, qualifiedNamePrefixLength);
            }
         } else if (Method.JAR.is(protocol, urlString)) {

            JarFile jarFile = null;
            if (Protocol.ZIP.is(protocol)) {
               // we have forced jar handling but protocol starts with zip. So try to open jar directly.
               String file = packageUrl.getFile();
               int indexOf = file.indexOf(".jar!");
               jarFile = new JarFile(file.substring(0, indexOf) + ".jar");
            } else {
                // somehow the connection has no close
                // method and can NOT be disposed
                JarURLConnection connection = (JarURLConnection) packageUrl.openConnection();
                connection.setUseCaches(false);
                jarFile = connection.getJarFile();
            }
            processArchive(pathWithPrefix, jarFile);

         } else if (Method.WSJAR.is(protocol)) {
            JarFile jarFile = new JarFile(new File(urlString));
            processArchive(pathWithPrefix, jarFile);
         } else if (Method.ZIP.is(protocol, urlString)) {
            ZipFile zipFile = new ZipFile(new File(urlString));
            processArchive(pathWithPrefix, zipFile);
         } else if (Method.CODE_SOURCE.is(protocol)) {
     		URL jarURL = extractJarFileURL(packageUrl);
         	File jarPath = getFile(jarURL);
         	JarFile jarFile = new JarFile(jarPath);
         	processArchive(pathWithPrefix, jarFile);
         }
         else {
        	 logger.warn("Unknown protocol: "+protocol);
         }
      }

   }

   private static File getFile(URL resourceUrl) throws FileNotFoundException {
       if (!Protocol.FILE.is(resourceUrl.getProtocol())) {
           throw new FileNotFoundException("url cannot be resolved to absolute file path because it does not reside in the file system: " + resourceUrl);
       }
       try {
       	URI uri = new URI(resourceUrl.toString().replaceAll(" ", "%20"));
           return new File(uri.getSchemeSpecificPart());
       } catch (URISyntaxException ex) {
           // Fallback for URLs that are not valid URIs (should hardly ever happen).
           return new File(resourceUrl.getFile());
       }
   }

   /**
    * Extract the URL for the actual jar file from the given URL (which may
    * point to a resource in a jar file or to a jar file itself).
    *
    * @param jarUrl
    *            the original URL
    * @return the URL for the actual jar file
    * @throws MalformedURLException
    *             if no valid jar file URL could be extracted
    */
   private static URL extractJarFileURL(URL jarUrl) throws MalformedURLException {
       String urlFile = jarUrl.getFile();
       int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
       if (separatorIndex != -1) {
           String jarFile = urlFile.substring(0, separatorIndex);
           try {
               return new URL(jarFile);
           } catch (MalformedURLException ex) {
               // Probably no protocol in original jar URL, like
               // "jar:C:/mypath/myjar.jar".
               // This usually indicates that the jar file resides in the file
               // system.
               if (!jarFile.startsWith("/")) {
                   jarFile = "/" + jarFile;
               }
               return new URL(FILE_URL_PREFIX + jarFile);
           }
       } else {
           return jarUrl;
       }
   }


   /**
    * This method finds the recursively scans the given <code>packageDirectory</code> for {@link Class} files and adds
    * their according Java names to the given <code>classSet</code>. Code written by Jorg Hohwiller for the m-m-m
    * project ({@link http://m-m-m.sf.net})
    *
    * @param packageDirectory is the directory representing the {@link Package}.
    * @param qnb is a {@link StringBuilder} containing the qualified prefix (the {@link Package} with a trailing dot).
    * @param qualifiedNamePrefixLength the length of the prefix used to rest the string-builder after reuse.
    */
   private final void findFileNamesRecursive(File packageDirectory, StringBuilder qnb, int qualifiedNamePrefixLength) {

      for (File childFile : packageDirectory.listFiles()) {
         String fileName = childFile.getName();
         if (childFile.isDirectory()) {
            qnb.setLength(qualifiedNamePrefixLength);
            StringBuilder subBuilder = new StringBuilder(qnb);
            subBuilder.append(fileName);
            subBuilder.append('.');
            findFileNamesRecursive(childFile, subBuilder, subBuilder.length());
         } else {

            qnb.setLength(qualifiedNamePrefixLength);
            qnb.append(fileName);
            String dateiName = qnb.toString();
            if (checkFoundFile(dateiName)) {
               try {
                  processData(new FileInputStream(childFile), dateiName);
               } catch (FileNotFoundException e) {
                  e.printStackTrace();
               }
            }
         }
      }
   }

   /**
    * Process archive.
    *
    * @param pathWithPrefix the path with prefix
    * @param jarFile the jar file
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private void processArchive(String pathWithPrefix, ZipFile jarFile) throws IOException {

      Enumeration<? extends ZipEntry> jarEntryEnumeration = jarFile.entries();
      while (jarEntryEnumeration.hasMoreElements()) {
         ZipEntry zipEntry = jarEntryEnumeration.nextElement();
         String absoluteFileName = zipEntry.getName();
         if (absoluteFileName.startsWith(ResourceLoader.SLASH)) {
            absoluteFileName = absoluteFileName.substring(1);
         }

         // special treatment for WAR files... "WEB-INF/lib/" entries
         // should be opened directly in contained jar

         if (absoluteFileName.startsWith(ResourceLoader.WARCLS_PATH)) {
            absoluteFileName = absoluteFileName.substring(16); // "WEB-INF/classes/".length() == 16
         }

         if (absoluteFileName.startsWith(pathWithPrefix)) {

            String qualifiedName = absoluteFileName.replace('/', '.');
            if (checkFoundFile(qualifiedName)) {
               processData(jarFile.getInputStream(zipEntry), qualifiedName);
            }
         }
      }
   }

   /**
    * internal helper method: processData.
    *
    * @param is - InputStream
    * @param fileName - a file name
    */
   private void processData(InputStream is, String fileName) {

      try {
         if (is != null) {
            process(is, fileName);
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
    	  Streams.close(is);
      }
   }


   /**
    * This method checks and transforms the filename of a potential {@link Class} given by
    * <code>fileName</code>.
    *
    * @param fileName is the filename.
    * @return the according Java {@link Class#getName() class-name} for the given
    * <code>fileName</code> if it is a class-file that is no anonymous {@link Class},
    * else <code>null</code>.
    */
   private static String fixClassName(String fileName) {

	if (fileName.endsWith(EXT_CLASS)) {
		// remove extension (".class".length() == 6)
		String nameWithoutExtension = fileName.substring(0, fileName.length() - 6);
		// handle inner classes...

		int lastDollar = nameWithoutExtension.lastIndexOf('$');
		if (lastDollar > 0) {
			char innerClassStart = nameWithoutExtension.charAt(lastDollar + 1);
			if ((innerClassStart >= '0') && (innerClassStart <= '9')) {
				// ignore anonymous class
				return null;
			} else {
				//return nameWithoutExtension.replace('$', '.');

			}
		}

		return nameWithoutExtension;
	}
	return null;
}
}
