package net.j7.commons.reflection;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import net.j7.commons.base.NotNull.NullObject;
import net.j7.commons.collections.Maps;
import net.j7.commons.reflection.Walker.BaseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"unchecked","unused"})
public class UnWalker extends AbstractWalker {

//	private static final Logger log = LoggerFactory.getLogger(UnWalker.class);
//	private static final String SET_ID = "setId";
//
//	private static final String FIELD_ID = "id";
//
//	private ModelData obj;
//	private IBaseMethods dispatcher;
//
//	private final static BOAbstract EMPTY_BO_ABSTRACT = null;
//
//	protected final static transient Set<String> IGNORE_FIELDS;
//	static {
//		IGNORE_FIELDS = Collections.synchronizedSet(new HashSet(2));
//		//IGNORE_FIELDS.add(FIELD_ID);
//		IGNORE_FIELDS.add(CLASS_NAME);
//	}
//	private transient Set<String> whiteList;
//	private BOAbstract defaultBO;
//
//	public UnWalker(ModelData obj, IBaseMethods dispatcher, String[] whiteList, BOAbstract emptyBoInstance) {
//		this.obj = obj;
//		this.dispatcher = dispatcher;
//
//		this.defaultBO = emptyBoInstance;
//
//		if (whiteList != null) {
//		this.whiteList = new HashSet<String>();
//
//		for (int i = 0; i < whiteList.length; i++)
//			this.whiteList.add(whiteList[i]);
//		}
//	}
//
//	public BOAbstract toBusinessObject() {
//		StubBO res = new StubBO();
//		HashMap processed=null;
//		try {
//			processed = new HashMap(128);
//			Object iterRes = iterate(obj, res, BASE_MODEL, MAX_RECURSION, processed);
//
//			BOAbstract output = res.getBase();
//
//			if (output!=null) return output;
//			if (iterRes instanceof Serializable) {
//				BaseModel md = new BaseModel();
//				md.set(Constants.ID, iterRes);
//				return newBusinessObject(null, null, md);
//			}
//
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return EMPTY_BO_ABSTRACT;
//		} finally {
//			processed.clear();
//		}
//		return null;
//	}
//
//	public Object iterate(Object obj, BOAbstract m, String fieldName, int depth, Map<Object, Object> processed) {
//
//		if (0 >= depth-- || NullObject.isNull(obj)) return NullObject.instance;
//		if (null == obj) return obj;
//
//		Object pro = processed.get(obj);
//		if (pro != null) return pro;
//
//		final Class cls = obj.getClass();
//
//		if (cls.isPrimitive() || IGNR.contains(cls)) {
//			return toReturn(obj, obj, processed);
//		}
//
//		if (obj instanceof Collection) {
//			Collection cl = (Collection) obj;
//			Collection list = obj instanceof Set ? new HashSet(cl.size()) : new ArrayList(cl.size());
//
//			for (Object element : cl) {
//				Object result = iterate(element, null, null, depth, processed);
//				if (null != result && NullObject.notNull(result))	list.add(result);
//			}
//			return toReturn(obj, list, processed);
//		}
//
//		if (cls.isArray()) {
//			final Object[] array = (Object[]) obj;
//			Object[] newArr = new Object[array.length];
//
//			for (int i = 0; i < array.length; i++) {
//				Object result = iterate(array[i], null, null, depth, processed);
//				if (null != result && NullObject.notNull(result))	newArr[i] = result;
//			}
//			return toReturn(obj, newArr, processed);
//		}
//		if (obj instanceof Map) {
//			final Map<Object, Object> map = (Map) obj;
//
//			Map newMap = new HashMap(map.size());
//
//			for (Map.Entry<Object, Object> element : map.entrySet()) {
//				Object key	 = iterate(element.getKey(), null, null, depth, processed);
//				Object value = iterate(element.getValue(), null, null, depth, processed);
//				if (null != key && null != value && NullObject.notNull(key) && NullObject.notNull(value))
//					newMap.put(key, value);
//			}
//			return toReturn(obj, newMap, processed);
//		}
//
//		if (obj instanceof ModelData) {
//
//			if (obj instanceof ITypeModel) {
//				return toReturn(obj, ((ITypeModel)obj).getId(), processed);
//			}
//
//			ModelData md = (ModelData) obj;
//			Map<String, Object> fields = md.getProperties();
//			m = newBusinessObject(m, fieldName, md);
//
//
//			if (null != fields)
//				for (Map.Entry<String, Object> field : fields.entrySet()) {
//					String nameOfField = field.getKey();
//
//
//					if (IGNORE_FIELDS.contains(nameOfField) ||
//						(whiteList!=null && !whiteList.contains(nameOfField))) continue;
//
//					Object value = field.getValue();
//
//					Object result = iterate(value, m, nameOfField, depth, processed);
//					if (null != m && NullObject.notNull(result))
//						setField(m, nameOfField, result);
//				}
//
//		}
//
//		return toReturn(obj, m, processed);
//	}
//
//	public static Class getClassByNameUncached(String name) {
//
//		Class result = null;
//		try {
//			result = Class.forName(name);
//		} catch (ClassNotFoundException e) {
//			ClassLoader cloader = Thread.currentThread().getContextClassLoader();
//			try {
//				result = cloader.loadClass(name);
//			} catch (ClassNotFoundException e1) {
//				e1.printStackTrace();
//			}
//		}
//
//		return result;
//	}
//
//	private final static transient Map<String, Class> cachedCBN= Maps.newSynchronizedMap();
//
//	public static final Class getClassByName(String className) {
//		Class clazz = cachedCBN.get(className);
//		if (null==clazz) {
//			clazz = getClassByNameUncached(className);
//			cachedCBN.put(className, clazz);
//		}
//		return clazz;
//	}
//
//	public static Field getField(Class clazz, String fieldName){
//		try {
//			return clazz.getDeclaredField(fieldName);
//		} catch (NoSuchFieldException e) {
//			return getFieldBySuperclass(clazz, fieldName);
//		}
//	}
//
//	private static Field getFieldBySuperclass(Class clazz, String fieldName){
//		Class superclass = clazz.getSuperclass();
//		while (!superclass.equals(Object.class)) {
//			try {
//				return superclass.getDeclaredField(fieldName);
//			} catch (NoSuchFieldException e) {
//				return getFieldBySuperclass(superclass, fieldName);
//			}
//		}
//		return null;
//	}
//
//
//
//	private void setField(Object obj, String fieldName, Object value) {
//		try {
//			if (NullObject.isNull(value)) return ;
////			if ("active".equals(fieldName)) {
////				System.out.println("AGA!");
////			}
//			Field field = getField(obj.getClass(),fieldName);
//			if(field == null) {
////				System.out.println("field not found: " + fieldName);
//				return;
//			}
//			if (instanceOf(field.getType(), IBOAbstract.class)
//					&& !(value instanceof IBOAbstract)) {
//				value = value != null ? (BOAbstract) dispatcher.readFirstBO(EmptyBO.get(field.getType()), value) : null;
//			}
//
//
//
//
//			if(field.getName().equals(Constants.ID)) {
//				Method setter = obj.getClass().getMethod(SET_ID, Serializable.class);
//				if (!setter.isAccessible())	setter.setAccessible(true);
//				setter.invoke(obj, value);
//			} else {
//
//				boolean ok = false;
//				if (value!=null)
//				try {
//					Method setter = obj.getClass().getMethod(fieldToSetterName(fieldName), value.getClass());
//					if (!setter.isAccessible())	setter.setAccessible(true);
//					setter.invoke(obj, value);
//					ok = true;
//				} catch (Exception e) {
//					//log.error("Can't set value via setter");
//
//				}
//
//				if (!ok) {
//				//initialize field with value directly
//				if (!field.isAccessible())	field.setAccessible(true);
//				try {
//					field.set(obj, value);
//				} catch (Exception e2) {
//					log.error("Can't set "+obj.getClass().getSimpleName()+"."+field.getName()+" to "+value);
//				}
//				}
//
//
//
//
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			}
//	}
//
//	private static final String PRF = "set";
//	private static String fieldToSetterName(String field) {
//		if (field==null || field.length()==0) return field;
//		StringBuilder sb = new StringBuilder();
//		sb.append(PRF);
//		sb.append(Character.toUpperCase(field.charAt(0)));
//		if (field.length()>1)
//			sb.append(field.substring(1));
//
//		return sb.toString();
//	}
//
////	public static void main(String[] args) {
////		System.out.println(fieldToSetterName("personTypeId"));
////		System.out.println(fieldToSetterName(""));
////		System.out.println(fieldToSetterName(null));
////		System.out.println(fieldToSetterName("account"));
////		System.out.println(fieldToSetterName("Id"));
////		System.out.println(fieldToSetterName("i"));
////	}
//
//	private BOAbstract newBusinessObject(BOAbstract m,  String fieldName, ModelData model) {
//		final Class clazz = getClassForBO(model);
//		ClassMetadata cm = new ClassMetadata(clazz);
//		try {
//			Serializable id = model.get(Constants.ID);
//			BOAbstract obj;
//			if (null==id)
//			obj = (BOAbstract) cm.newInstance();
//			else {
//			obj = (BOAbstract) dispatcher.readFirstBO(EmptyBO.get(clazz), id);
//			if (obj == null) {
//				log.info("[UnWalker]Can't read non existing or deleted BO ["+clazz+"] with id="+id+", returning new bo with the same id");
//				obj = (BOAbstract) cm.newInstance();
//				obj.setId(id);
//			}
//			}
//
//			if (null!=m) {
//				setField(m, fieldName, obj);
//			}
//			return (BOAbstract)obj;
//		} catch (Exception e) {e.printStackTrace();}
//		return null;
//	}
//
//	private Class getClassForBO(ModelData model) {
//		String className = (String) model.get(CLASS_NAME);
//		final Class clazz = className != null ? getClassByName((String) model.get(CLASS_NAME)) : defaultBO.getClass();
//		return clazz;
//	}
//
//	private final static Object toReturn(Object obj, Object ret, Map<Object, Object> processed) {
//		processed.put(obj, ret);
//		return ret;
//	}
//
//

}
