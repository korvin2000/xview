package net.j7.commons.io;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Stack;

import net.j7.commons.io.helpers.WildcardMatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class FileUtils. This version is generally based on apache IO FileName class. It's a reduced and optimized version containing only most commonly used methods
 *
 * @version $Revision: #1 $
 */
@SuppressWarnings({"unused"})
public final class FileUtils {

   /**
    * The technical logger to use.
    */
   private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

   /**
    * The extension separator character.
    */
   public static final char EXT_SEPARATOR = '.';
   /**
    * The extension separator String.
    */
   public static final String EXT_SEPARATOR_STR = Character.toString(EXT_SEPARATOR);

   /**
    * The system separator character.
    */
   public static final char SYSTEM_SEPARATOR = File.separatorChar;

   /**
    * The Unix separator character.
    */
   public static final char UNIX_SEPARATOR = '/';

   /**
    * The Windows separator character.
    */
   public static final char WINDOWS_SEPARATOR = '\\';

   /**
    * Converts all separators to the system separator.
    *
    * @param path the path to be changed, null ignored
    * @return the updated path
    */
   public static String convertToSystemPath(String path) {
      if (path == null) {
         return null;
      }
      if (isWinFileSystem()) {
         return convertToWindowsPath(path);
      } else {
         return convertToUnixPath(path);
      }
   }

   /**
    * Converts all separators to the Unix separator of forward slash.
    *
    * @param path the path to be changed, null ignored
    * @return the updated path
    */
   public static String convertToUnixPath(String path) {
      if (path == null || path.indexOf(WINDOWS_SEPARATOR) == -1) {
         return path;
      }
      return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
   }

   /**
    * Converts all separators to the Windows separator of backslash.
    *
    * @param path the path to be changed, null ignored
    * @return the updated path
    */
   public static String convertToWindowsPath(String path) {
      if (path == null || path.indexOf(UNIX_SEPARATOR) == -1) {
         return path;
      }
      return path.replace(UNIX_SEPARATOR, WINDOWS_SEPARATOR);
   }

   /**
    * Gets the base name, minus the full path and extension, from a full filename.
    * <p>
    * This method will handle a file in either Unix or Windows format. The text after the last forward or backslash and before the last dot is returned.
    * <pre>
    * a/b/c.txt --> c
    * a.txt     --> a
    * a/b/c     --> c
    * a/b/c/    --> ""
    * </pre>
    * <p>
    * The output will be the same irrespective of the machine that the code is running on.
    *
    * @param filename the filename to query, null returns null
    * @return the name of the file without the path, or an empty string if none exists
    */
   public static String getBaseName(String filename) {
      return removeExtension(getFileName(filename));
   }

   /**
    * Gets the extension of a filename.
    * <p>
    * This method returns the textual part of the filename after the last dot. There must be no directory separator after the dot.
    * <pre>
    * foo.txt      --> "txt"
    * a/b/c.jpg    --> "jpg"
    * a/b.txt/c    --> ""
    * a/b/c        --> ""
    * </pre>
    * <p>
    * The output will be the same irrespective of the machine that the code is running on.
    *
    * @param filename the filename to retrieve the extension of.
    * @return the extension of the file or an empty string if none exists or {@code null} if the filename is {@code null}.
    */
   public static String getExtension(String filename) {
      if (filename == null) {
         return null;
      }
      int index = indexOfExtension(filename);
      if (index == -1) {
         return "";
      } else {
         return filename.substring(index + 1);
      }
   }

   /**
    * Gets the name minus the path from a full filename.
    * <p>
    * This method will handle a file in either Unix or Windows format. The text after the last forward or backslash is returned.
    * <pre>
    * a/b/c.txt --> c.txt
    * a.txt     --> a.txt
    * a/b/c     --> c
    * a/b/c/    --> ""
    * </pre>
    * <p>
    * The output will be the same irrespective of the machine that the code is running on.
    *
    * @param filename the filename to query, null returns null
    * @return the name of the file without the path, or an empty string if none exists
    */
   public static String getFileName(String filename) {
      if (filename == null) {
         return null;
      }
      int index = indexOfLastSeparator(filename);
      return filename.substring(index + 1);
   }

   public static String extractFullPathName(File file) {
       if (file == null) {
           return "";
        }
        try {
           return file.getCanonicalPath();
        } catch (IOException e) {
        }
        return file.getAbsolutePath();
   }

   /**
    * Does the work of getting the path.
    *
    * @param filename the filename
    * @param includeSeparator 0 to omit the end separator, 1 to return it
    * @return the path
    */
   public static String getFilePath(String filename, boolean includeSeparator) {
      if (filename == null) {
         return null;
      }
      int prefix = prefixLength(filename);
      if (prefix < 0) {
         return null;
      }
      if (prefix >= filename.length()) {
         if (includeSeparator) {
            return getPrefix(filename);  // add end slash if necessary
         } else {
            return filename;
         }
      }
      int index = indexOfLastSeparator(filename);
      if (index < 0) {
         return filename.substring(0, prefix);
      }
      int end = index + (includeSeparator ? 1 : 0);
      if (end == 0) {
         end++;
      }
      return filename.substring(0, end);
   }

   /**
    * Checks whether the extension of the filename is that specified.
    * <p>
    * This method obtains the extension as the textual part of the filename after the last dot. There must be no directory separator after the dot. The
    * extension check is case-sensitive on all platforms.
    *
    * @param filename the filename to query, null returns false
    * @param extension the extension to check for, null or empty checks for no extension
    * @return true if the filename has the specified extension
    */
   public static boolean hasExtension(String filename, String extension) {
      if (filename == null) {
         return false;
      }
      if (extension == null) {
         return true;
      }
      if (extension.length() == 0) {
         return indexOfExtension(filename) == -1;
      }
      extension = extension.toLowerCase();
      if (!extension.startsWith(".")) {
         extension = "." + extension;
      }
      return filename.toLowerCase().endsWith(extension);
   }

   /**
    * Removes the extension from a filename.
    * <p>
    * This method returns the textual part of the filename before the last dot. There must be no directory separator after the dot.
    * <pre>
    * foo.txt    --> foo
    * a\b\c.jpg  --> a\b\c
    * a\b\c      --> a\b\c
    * a.b\c      --> a.b\c
    * </pre>
    * <p>
    * The output will be the same irrespective of the machine that the code is running on.
    *
    * @param filename the filename to query, null returns null
    * @return the filename minus the extension
    */
   public static String removeExtension(String filename) {
      if (filename == null) {
         return null;
      }
      int index = indexOfExtension(filename);
      if (index == -1) {
         return filename;
      } else {
         return filename.substring(0, index);
      }
   }



   public static boolean wildcardMatchOnSystem(String filename, String wildcardMatcher) {
      return WildcardMatcher.wildcardMatch(filename, wildcardMatcher, isWinFileSystem());
   }

   public static String correctPath(String path) {
      if (path == null || path.isEmpty()) {
         return path;
      }
      try {
         String ret = new File(path).toURI().toURL().getFile();
         if (ret.startsWith("/")) {
            ret = ret.substring(1);
         }
         return FileUtils.convertToSystemPath(ret);
      } catch (MalformedURLException e) {
         // ignore exception //
      }
      return FileUtils.convertToSystemPath(path);
   }

   /**
    * Determines if Windows file system is in use.
    *
    * @return true if the system is Windows
    */
   static boolean isWinFileSystem() {
      return SYSTEM_SEPARATOR == WINDOWS_SEPARATOR;
   }


   /**
    * Gets the prefix from a full filename,
    */
   private static String getPrefix(String filename) {
      if (filename == null) {
         return null;
      }
      int len = prefixLength(filename);
      if (len < 0) {
         return null;
      }
      if (len > filename.length()) {
         return filename + UNIX_SEPARATOR;  // we know this only happens for unix
      }
      return filename.substring(0, len);
   }

   /**
    * Returns the index of the last extension separator character, which is a dot.
    * <p>
    * This method also checks that there is no directory separator after the last dot. To do this it uses {@link #indexOfLastSeparator(String)} which will
    * handle a file in either Unix or Windows format.
    * <p>
    * The output will be the same irrespective of the machine that the code is running on.
    *
    * @param filename the filename to find the last path separator in, null returns -1
    * @return the index of the last separator character, or -1 if there is no such character
    */
   private static int indexOfExtension(String filename) {
      if (filename == null) {
         return -1;
      }
      int extensionPos = filename.lastIndexOf(EXT_SEPARATOR);
      int lastSeparator = indexOfLastSeparator(filename);
      return lastSeparator > extensionPos ? -1 : extensionPos;
   }

   /**
    * Returns the index of the last directory separator character.
    * <p>
    * This method will handle a file in either Unix or Windows format. The position of the last forward or backslash is returned.
    * <p>
    * The output will be the same irrespective of the machine that the code is running on.
    *
    * @param filename the filename to find the last path separator in, null returns -1
    * @return the index of the last separator character, or -1 if there is no such character
    */
   private static int indexOfLastSeparator(String filename) {
      if (filename == null) {
         return -1;
      }
      int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
      int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
      return Math.max(lastUnixPos, lastWindowsPos);
   }

   /**
    * Checks if the character is a separator.
    *
    * @param ch the character to check
    * @return true if it is a separator character
    */
   private static boolean isSeparator(char ch) {
      return ch == UNIX_SEPARATOR || ch == WINDOWS_SEPARATOR;
   }

   private static int prefixLength(String filename) {
      if (filename == null) {
         return -1;
      }
      int len = filename.length();
      if (len == 0) {
         return 0;
      }
      char ch0 = filename.charAt(0);
      if (ch0 == ':') {
         return -1;
      }
      if (len == 1) {
         if (ch0 == '~') {
            return 2;  // return a length greater than the input
         }
         return isSeparator(ch0) ? 1 : 0;
      } else {
         if (ch0 == '~') {
            int posUnix = filename.indexOf(UNIX_SEPARATOR, 1);
            int posWin = filename.indexOf(WINDOWS_SEPARATOR, 1);
            if (posUnix == -1 && posWin == -1) {
               return len + 1;  // return a length greater than the input
            }
            posUnix = posUnix == -1 ? posWin : posUnix;
            posWin = posWin == -1 ? posUnix : posWin;
            return Math.min(posUnix, posWin) + 1;
         }
         char ch1 = filename.charAt(1);
         if (ch1 == ':') {
            ch0 = Character.toUpperCase(ch0);
            if (ch0 >= 'A' && ch0 <= 'Z') {
               if (len == 2 || isSeparator(filename.charAt(2)) == false) {
                  return 2;
               }
               return 3;
            }
            return -1;
         } else if (isSeparator(ch0) && isSeparator(ch1)) {
            int posUnix = filename.indexOf(UNIX_SEPARATOR, 2);
            int posWin = filename.indexOf(WINDOWS_SEPARATOR, 2);
            if (posUnix == -1 && posWin == -1 || posUnix == 2 || posWin == 2) {
               return -1;
            }
            posUnix = posUnix == -1 ? posWin : posUnix;
            posWin = posWin == -1 ? posUnix : posWin;
            return Math.min(posUnix, posWin) + 1;
         } else {
            return isSeparator(ch0) ? 1 : 0;
         }
      }
   }

   public static String asFileName(String name) {
	   if (null == name || name.isEmpty()) return name;
	   char ch;
	   StringBuilder sb = new StringBuilder(name.length());

	   name = name.toLowerCase();

	   for (int i = 0; i < name.length(); i++) {
		ch = name.charAt(i);

		if (ch < ' ') sb.append('_');
		else
		switch (ch) {
		case ',': case '/': case '\\': case '?':
		case ';': case ':': case '>': case '<':
		case '|': case '*': case '.': case '=':
		case '[': case ']': case '+': case '\u007F':
		case ' ':
			sb.append('_'); break;
		case '"':
			break;
		case '\u00e4':
			sb.append("ae"); break;
		case '\u00f6':
			sb.append("oe"); break;
		case '\u00fc':
			sb.append("ue"); break;
		case '\u00df':
			sb.append("ss"); break;
		default:
			sb.append(ch); break;
		}
	}
	   StringBuilder res = new StringBuilder(sb.length());
	   char lCh = (char)0;
	   for (int i = 0; i < sb.length(); i++) {
			ch = sb.charAt(i);
			if (ch=='_' && ch == lCh) continue;
			res.append(ch);
			lCh = ch;
	   }


	   return res.toString();
   }


}
