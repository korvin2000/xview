/*
 * @File: ReflectionHelper.java
 *
 *
 *
 * All rights reserved.
 *
 * @Author:  tavrovsa
 *
 * @Version $Revision: #4 $Date: $
 *
 *
 */
package net.j7.commons.reflection;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class ReflectionHelper.
 * @author  tavrovsa
 */
@SuppressWarnings({"unused"})
public final class ReflectionHelper {


   /** The technical logger to use. */
   private static final Logger logger = LoggerFactory.getLogger(ReflectionHelper.class);

   /** The Constant EMPTY_CLASS_ARRAY. */
   private static final Class<?> [] EMPTY_CLASS_ARRAY = new Class<?> [0];    // Used to lookup Constructor

    /** The Constant EMPTY_OBJECT_ARRAY. */
    private static final Object [] EMPTY_OBJECT_ARRAY = new Object [0]; // Used to call constructor

   /** The Constant SETPR. */
   private transient final static String SETPR = "set";

   /** The Constant GETPR. */
   private transient final static String GETPR = "get";

   private static final String QUOTES = "()";
   private static final String S_DOT = ".";

   /**
    * Checks if is annotation present.
    * first checks annotation if annotation assigned to field,
    * if no, checks if annotation assigned for corresponding
    * "get" method, if such exists
    *
    * @param cls the class to work with
    * @param field the field
    * @param annotation the annotation
    * @return true, if is annotation present
    */
   public static boolean isAnnotationPresent(Class<? extends Object> cls, String field, Class<? extends Annotation> annotation) {
      if (cls==null) return false;

      Field fld = getFieldByName(cls, field);
      if (fld==null) return false;

      if (fld.isAnnotationPresent(annotation)) return true;

      Method m = assignedGetter(cls, field);
      if (m==null) return false;

      return m.isAnnotationPresent(annotation);
   }


    /**
     * Return assigned method with given preffix for a given field if such exists.
     *
     * @param cls the class to work with
     * @param name the name
     * @param prefix the prefix
     * @return the method
     */
    public final static Method assignedMethod(Class<? extends Object> cls, String name, String prefix) {
      if (null == name || null == cls) return null;

      StringBuilder sb = new StringBuilder(name.length()+prefix.length());
      sb.append(prefix).append(Character.toUpperCase(name.charAt(0)));
      if (name.length()>1) sb.append(name.substring(1));

      return assignedMethod(cls, sb.toString());
    }


    /**
     * Assigned method.
     *
     * @param cls the class to work with
     * @param name the name
     * @return the method
     */
    public final static Method assignedMethod(Class<? extends Object> cls, String name) {
      if (cls ==  null) return null;
      name = name.intern();

      final Method[] methods = cls.getDeclaredMethods();
      for (int j =  methods.length-1, i = j; i >= 0; i--)
         if (methods[i].getName().equals(name))   return checkAccess(methods[i]);

      return assignedMethod(cls.getSuperclass(), name);
    }


    /**
     * Gets the method value.
     *
     * @param obj the object
     * @param name the name
     * @return the method value
     */
    public final static Object getMethodValue(Object obj, String name) {
      final Method method = assignedMethod(obj.getClass(), name);

      if (method!=null)
      try {
         return method.invoke(obj, EMPTY_OBJECT_ARRAY);
      } catch (Exception e) {
         logger.error("",e);
      }
      return null;
    }

    /**
     * Return assigned get method for a given field if such exists.
     *
     * @param cls the class to work with
     * @param name the name
     * @return the method
     */
    public final static Method assignedGetter(Class<? extends Object> cls, String name) {
      return assignedMethod(cls, name, GETPR);
    }

    /**
     * Return assigned set method for a given field if such exists.
     *
     * @param cls the class to work with
     * @param name the name
     * @return the method
     */
    public final static Method assignedSetter(Class<? extends Object> cls, String name) {
      return assignedMethod(cls, name, SETPR);
    }

   /**
    * Gets the field by name.
    * @param cls the class to work with
    * @param name the name
    * @return the field by name
    */
    public static Field getFieldByName(Class<?> cls, String name)  {

       if (cls == null || cls.isPrimitive()) return null;

       Field field = null;
       try {
          field = cls.getDeclaredField(name);
       } catch (Exception e) {}

       return (field!=null) ? field : getFieldByName(cls.getSuperclass(), name);
    }


    /**
     * Gets the field type.
     * @param cls the class to work with
     * @param name the name
     * @return the field type
     */
    public final static Class<?> getFieldType(Class<? extends Object> cls, String name) {
      final Field field = getFieldByName(cls, name);
      return field != null ?  field.getType() : null ;
    }

    /**
     * Gets the field value.
     * @param obj the object
     * @param name the name
     * @return the field value
     */
    public final static Object getFieldValue(Object obj, String name) {
      final Field field = getFieldByName(obj.getClass(), name);
      checkAccess(field);
      Object value=null;
      if (field!=null)
      try {
         value = field.get(obj);
      } catch (Exception e) { logger.error("",e); }
      return value;

    }

   /**
    * Get the value of the <tt>accessible</tt> flag for given field
    * and tries to set it to true, in oder to obtain access for such field.
    *
    * @param field the field
    * @throws SecurityException the security exception
    */
   private static Field checkAccess(final Field field) throws SecurityException {
      if (field!=null && !field.isAccessible()) field.setAccessible(true);
      return field;
   }


   /**
    * Get the value of the <tt>accessible</tt> flag for given method
    * and tries to set it to true, in oder to obtain access for such field.
    *
    * @param method the field
    * @throws SecurityException the security exception
    */
   private static Method checkAccess(final Method method) throws SecurityException {
      if (method!=null && !method.isAccessible()) method.setAccessible(true);
      return method;
   }


   /**
    * Checks whether an object implements a specific interface.
    *
    * @param classVerifiable the class verifiable
    * @param interfaceClass - class of interface
    * @return true, if <classVerifiable> inmplements <interfaceClass>
    */
   public static final boolean implementsInterface(final Class<?> classVerifiable,final Class<?> interfaceClass) {
      return interfaceClass.isInterface() && interfaceClass.isAssignableFrom(classVerifiable);
   }

   /**
    * Checks whether an object implements a specific interface.
    *
    * @param classVerifiable the class verifiable
    * @param interfaceName - name of interface
    * @return true, if <classVerifiable> inmplements <interfaceName>
    */
   public static final boolean implementsInterface(final Class<?> classVerifiable, final String interfaceName) {
      return implementsInterface(classVerifiable, interfaceName, 0);
   }


   /**
    * auxiliary private helper method for.
    *
    * @param cls the class to work with
    * @param iface the iface
    * @param recursions the recursions
    * @return true, if successful
    * @implementsInterface(classVerifiable, interfaceName) function
    * checks interface recursively, max recursion invocations up to 255 times
    */
   private static final boolean implementsInterface(final Class<?> cls, String iface, int recursions) {
      if (recursions<255 && cls != null)  {
         iface = iface.intern();
         for (Class<?> clzz : cls.getInterfaces())
            return !clzz.getSimpleName().equals(iface) ? implementsInterface(clzz, iface, ++recursions) : true;
         }
      return false;
   }

   /**
    * The instanceof command is used to determine if a Java class is of a
    * given type. The classVerifiable argument specifies an class handle.
    * The classAssignable specifies an interface or class. If the type
    * argument is a class name, instanceof will return true if the
    * classVerifiable is an instance of classAssignable class. If the type
    * argument is an interface, method returns true if the classVerifiable
    * argument implements the interface. Otherwise, returns false.
    *
    * @param classVerifiable the class verifiable
    * @param classAssignable the class assignable
    * @return true, if <classVerifiable> instanceof <classAssignable>
    */
    public final static boolean instanceOf(final Class<?> classVerifiable, final Class<?> classAssignable) {
      return classAssignable.isAssignableFrom(classVerifiable);
    }

   /**
    * Gets the class by a given name.
    *
    * @param name the name
    * @return the class by name
    */
   public static Class<?> getClassByName(String name) {
      Class<?> result = null;
      try {
         result = Class.forName(name);
      } catch (ClassNotFoundException e) {
         ClassLoader cloader = Thread.currentThread().getContextClassLoader();
         try {
            result = cloader.loadClass(name);
         } catch (ClassNotFoundException e1) {
            logger.error("",e1);
         }
      }

      return result;
   }


   /**
    * gets a value from some object
    * could be used to omit null pointer errors
    *
    * Person myObject = getPerson(...
    * eG. get(myObject, "address.getPhone()")
    * where address is a field in person class and getPhone() is a method in address class
    *
    * @param obj
    * @param str
    * @return
    */
   public static Object get(Object obj, String str) {
      return interateIntern(obj, new StringBuilder(str));
   }

   /**
    * Internal helper method for public method - GET
    * @param obj
    * @param str
    * @return
    */
   private static Object interateIntern(Object obj, StringBuilder str) {
      int idx = str.indexOf(S_DOT);
      String subS;
      if (idx<0) {
         subS = str.toString();
         str.delete(0, str.length());
      } else {
         subS = str.substring(0, idx);
         str.delete(0, idx + 1);
      }

      boolean isMethod = subS.endsWith(QUOTES);
      Object res = !isMethod ? getFieldValue(obj, subS) : getMethodValue(obj, subS.substring(0, subS.length()-2));
      if (res != null && str.length()>0) return interateIntern(res, str);

      return res;
   }

   /**
    * Sets the field value.
    *
    * @param objectInstance the object instance
    * @param fieldName the field name
    * @param valueToSet the value to set
    * @throws NoSuchFieldException the no such field exception
    * @throws IllegalArgumentException the illegal argument exception
    * @throws IllegalAccessException the illegal access exception
    */
   public static void setFieldValue(Object objectInstance, String fieldName, Object valueToSet) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
	   if (null == objectInstance) return ;
	   Field fld  = getFieldByName(objectInstance.getClass(), fieldName);
	   if (fld == null) throw new NoSuchFieldException("Field not found: "+fieldName);
	   if (!fld.isAccessible()) fld.setAccessible(true);
	   fld.set(objectInstance, valueToSet);

   }

   /**
    * Sets the method value.
    *
    * @param objectInstance the object instance
    * @param methodName the method name
    * @param valueToSet the value to set
    * @throws NoSuchMethodException the no such method exception
    * @throws IllegalAccessException the illegal access exception
    * @throws IllegalArgumentException the illegal argument exception
    * @throws InvocationTargetException the invocation target exception
    */
   public static void setMethodValue(Object objectInstance, String methodName, Object valueToSet) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	   if (null == objectInstance) return ;
	   Method method  = assignedMethod(objectInstance.getClass(), methodName);
	   if (method == null) throw new NoSuchMethodException("Method not found: "+methodName);
	   if (!method.isAccessible()) method.setAccessible(true);
	   method.invoke(objectInstance, valueToSet);
   }


}
