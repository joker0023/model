package com.jokerstation.common.util;

//import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class JsonUtils {

	private static Gson gson = new Gson();
	
	public static String toJson(Object obj) {
		return gson.toJson(obj);
	}
	
	public static <T> T toBean(String json, Class<T> cls) {
		return gson.fromJson(json, cls);
	}
	
//	public static <T> List<T> toList(String json, Class<T> cls) {
//		return gson.fromJson(json, new ListTypeToken<T>().getType());
//	}
//	
//	private static class ListTypeToken<T> extends TypeToken<List<T>> {
//		private static final long serialVersionUID = -4776565271219350992L;
//	}
}
