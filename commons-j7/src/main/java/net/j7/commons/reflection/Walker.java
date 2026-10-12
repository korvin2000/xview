package net.j7.commons.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import net.j7.commons.base.NotNull.NullObject;
import net.j7.commons.collections.Maps;
import net.j7.commons.strings.StringUtils;

/**
 * The Class<?> Walker.
 *
 * @author tavrovsa
 *
 */
@SuppressWarnings("unchecked")
public final class Walker extends AbstractWalker {

	private static final BaseModel EMPTY_BASE_MODEL = new BaseModel();

	private Map<String, Integer> fieldsSet;

	private void addPossibleFields(String str) {
		char ch;
		StringBuilder sb = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); i++) {
			ch = str.charAt(i);
			if (ch==DOT) fieldsSet.put(sb.toString(), FIELD_BRIDGE);
			sb.append(ch);
		}
		fieldsSet.put(sb.toString(), FIELD_LAST);
	}

	private Integer getState(String path) {
		if (null == fieldsSet) return FIELD_BRIDGE;
		Integer state = fieldsSet.get(path);
		return state != null ? state : path.length() == 0 ? FIELD_BRIDGE : FIELD_WRONG;
	}



	private Object obj;

	public Walker(Object obj, String[] fields) {
		this.obj = obj;

		if (fields!=null) {
		fieldsSet = Maps.newMap(fields.length << 1); //multiply by 2
		for (int i = 0; i < fields.length; i++)
			addPossibleFields(fields[i]);
		}

	}

	public BaseModel toBaseModel() {
		BaseModel res = new BaseModel();
		HashMap processed=null;
		try {
			processed = new HashMap(128);
			iterate(obj, res, BASE_MODEL, MAX_RECURSION, processed, StringUtils.EMPTY, FIELD_BRIDGE);
			return NotNullBaseModel((BaseModel)res.get(BASE_MODEL));
		} catch (Exception e) {
			e.printStackTrace();
			return EMPTY_BASE_MODEL;
		} finally {
			processed.clear();
		}

	}

	private static BaseModel NotNullBaseModel(BaseModel base) {
		return base != null ? base : new BaseModel();
	}

	public Object iterate(Object obj, BaseModel m, String fieldName, int depth, Map<Object, Object> processed, String path, Integer state) throws Exception {

		if (0 >= depth-- || NullObject.isNull(obj)) return NullObject.instance;
		if (null==obj) return obj;

 		if (!FIELD_LAST.equals(state)) state = getState(path);

		if (FIELD_WRONG.equals(state)) return NullObject.instance;


		Object pro = processed.get(obj);
		if (pro!=null) return pro;

		Class<?> cls = obj.getClass();

		if (cls.isPrimitive() || IGNR.contains(cls)) {
			return toReturn(obj, obj, processed);
		} else
		if (obj instanceof Collection) {
			Collection cl = (Collection)obj;

			Collection list = obj instanceof Set ? new HashSet(cl.size()) : new ArrayList(cl.size());

			//int cc = 0;
			for (Object element : cl) {
				//cc++;
				Object result = iterate(element, null, null, depth, processed, path, state);
				if (NullObject.notNull(result) && null!=result)
					list.add(result);
			}

			return toReturn(obj, list, processed);

		} else if (cls.isArray()) {

			final Object[] array = (Object[]) obj;

			Object[] newArr = new Object[array.length];
			//model.set(fieldName, newArr);

			for (int i = 0; i < array.length; i++) {
				Object result = iterate(array[i], null, null, depth, processed, path, state);
				if (NullObject.notNull(result) && null!=result) newArr[i] = result;
			}
			return toReturn(obj, newArr, processed);
		} else if (obj instanceof Map) {
			final Map<Object, Object> map = (Map) obj;

			Map newMap = new HashMap(map.size());

			for (Map.Entry<Object, Object> element : map.entrySet()) {
				Object key = iterate(element.getKey(), null, null, depth, processed, path, state);
				Object value = iterate(element.getValue(), null, null, depth, processed, path, state);
				if (null!=key && null !=value && NullObject.notNull(key) && NullObject.notNull(value))
				newMap.put(key, value);

			}

			return toReturn(obj, newMap, processed);
	} else
		{
			final Field[] fields = getCachedFields(cls);

			m = newBaseModel(m, fieldName); //returns always not null value
			m.set(CLASS_NAME, cls.getName());

			for (Field field : fields)
				if (checkField(field))
			{
				String nameOfField = field.getName();
				Object value = field.get(obj);

				Object result = iterate(value, m, nameOfField, depth, processed, fillPath(path, nameOfField), state);
				if (null!=m && NullObject.notNull(result))
				m.set(nameOfField, result);
			}



		}

		return toReturn(obj, m, processed);
	}

	private final static Object toReturn(Object obj, Object ret, Map<Object, Object> processed) {
		processed.put(obj, ret);
		return ret;
	}

	private final static BaseModel newBaseModel(BaseModel oldM, String name) {
		BaseModel newM = new BaseModel();
		if (oldM!=null) oldM.set(name, newM);
		return newM;
	}

//	private BaseModel newBaseModel2(BaseModel oldM, String name) {
//		return oldM!=null ? oldM : new BaseModel();
//	}

	/**
	 * @param field
	 * @return
	 */
	private final static boolean checkField(Field field) {
		final int modifiers = field.getModifiers();
		boolean ok =
		!field.isSynthetic() &&
		!Modifier.isStatic(modifiers) &&
		!Modifier.isFinal(modifiers) &&
		!Modifier.isTransient(modifiers);

		if (!field.isAccessible())
			field.setAccessible(true);

		return ok && field.isAccessible();
	}

	private String fillPath(String path, String field) {
		StringBuilder sb = new StringBuilder(path.length() + field.length() + 16);

		if (path.length()>0) {
			sb.append(path).append(DOT);
		}

		return sb.append(field).toString();
	}

	private static final Field[] getAllFields(Class<?> clazz) {
		Set<Field> allFields = new HashSet<Field>();
		while (clazz!=null) {
			Field[] fields = clazz.getDeclaredFields();
			for (int i = 0; i < fields.length; i++)
				allFields.add(fields[i]);

;			clazz = clazz.getSuperclass();
		}
		Field[] all = new Field[allFields.size()];
		allFields.toArray(all);
		return all;
	}

	private final static transient Map<Class<?>, Field[]> cachedField= Maps.newSynchronizedMap();

	private static final Field[] getCachedFields(Class<?> clazz) {
		Field[] fields = cachedField.get(clazz);
		if (null==fields) {
			fields = getAllFields(clazz);
			cachedField.put(clazz, fields);
		}
		return fields;
	}


	public static class BaseModel<K,E> extends HashMap<K,E> {

		private static final long serialVersionUID = 5031053964604422916L;

		public E set(K key, E value) {
			return this.put(key, value);
		}

	};


}
