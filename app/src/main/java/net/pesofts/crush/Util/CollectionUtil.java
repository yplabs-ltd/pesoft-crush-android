package net.pesofts.crush.Util;

import java.util.Collection;
import java.util.Map;

public class CollectionUtil {
	public static final int INVALID_INDEX = -1;

	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Collection collection) {
		return (collection == null || collection.isEmpty());
	}

	@SuppressWarnings("rawtypes")
	public static boolean isNotEmpty(Collection collection) {
		return !isEmpty(collection);
	}

	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Map map) {
		return (map == null || map.isEmpty());
	}

	@SuppressWarnings("rawtypes")
	public static boolean isNotEmpty(Map map) {
		return !isEmpty(map);
	}

	public static int getSize(Collection collection) {
		if (isEmpty(collection)) {
			return 0;
		} else {
			return collection.size();
		}
	}

	public static boolean isEmpty(Object[] array) {
		return array == null || array.length < 1;
	}

	public static boolean isNotEmpty(Object[] array) {
		return !isEmpty(array);
	}

	public static boolean isSafeIndex(Collection collection, int index) {
		if (isEmpty(collection)) {
			return false;
		}

		return index >= 0 && getSize(collection) > index;
	}

	public static boolean isOutOfIndex(Collection collection, int index) {
		return !isSafeIndex(collection, index);
	}

}
