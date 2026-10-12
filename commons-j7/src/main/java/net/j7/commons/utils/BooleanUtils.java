package net.j7.commons.utils;

import net.j7.commons.strings.StringUtils;

/**
 *
 * Class to convert a String boolean representation to an Boolean
 * Operations on boolean primitives and Boolean objects.
 * This class tries to handle null input gracefully. An exception will not be thrown for a null input. Each method documents its behaviour in more detail.
 * @author  tavrovsa
 * @version $Revision: #3 $
 */
public class BooleanUtils  {


   public static boolean toBoolean(String b) {

	   if (null == b || StringUtils.isEmpty(b)) return false;
	   if ("1".equals(b)) return true;
	   b = b.toLowerCase();
	   return "true".equals(b) || "yes".equals(b) || "y".equals(b) || "on".equals(b) || "j".equals(b) || "ja".equals(b);

   }
}
