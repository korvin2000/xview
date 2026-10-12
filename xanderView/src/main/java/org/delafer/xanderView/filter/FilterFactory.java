package org.delafer.xanderView.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FilterFactory {

	static Map<String, Supplier<IFilter>> filters = new HashMap<>() {{
		put("size", FilterSize::new);
	}};

	public static IFilter getFilter(String name) {
		Supplier<IFilter> res = filters.get(name);
		return res == null ? null : res.get();
	}

}
