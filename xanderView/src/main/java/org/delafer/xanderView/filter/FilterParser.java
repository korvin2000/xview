package org.delafer.xanderView.filter;

import net.j7.commons.strings.StringUtils;

@Deprecated
public class FilterParser {

	public static IFilter parse(String filterArg) {

		if (filterArg == null || StringUtils.isEmpty(filterArg)) {
			return null;
		}
		String[] filters = filterArg.split(";");
		IFilter[] iFilter = new IFilter[filters.length];
		for (int i = 0; i < filters.length; i++) {
			iFilter[i] = FilterParser.parse(filters[i]);
		}
		return iFilter.length == 1 ? iFilter[0] : new MultiFilter(iFilter);
	}

	private static IFilter parseChunk(String val) {
		int start = val.indexOf('(');
		int end = start >= 0 ? val.indexOf(')', start+1) : start;
		String name, args = null;

		if (end > 0) {
			args = val.substring(start+1, end);
			name = val.substring(0, start).trim().toLowerCase();
		} else {
			name = val.toLowerCase();
		}
		IFilter filter = FilterFactory.getFilter(name);
		if (filter != null && StringUtils.isNotEmpty(args)) {
			String[] argArr = args.split(",");
			filter.setArgs(argArr);
		}
		return filter;
	}

//	public static void main(String[] args) {
//		parseChunk("Abcd");
//		parseChunk(" abcd()");
//		parseChunk("abCd (1,2) ");
//	}
}
