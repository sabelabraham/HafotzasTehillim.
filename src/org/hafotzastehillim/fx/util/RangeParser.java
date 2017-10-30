package org.hafotzastehillim.fx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RangeParser {

	private RangeParser() {

	}

	public static Set<Integer> parse(String str) {
		str = str.replaceAll("\\s", ",");

		Pattern illegal = Pattern.compile("[^\\s\\d,-]");
		Matcher matcher = illegal.matcher(str);
		if (matcher.find())
			throw new IllegalArgumentException("Contains illegal characters.");

		String[] tokens = str.split(",");
		Set<Integer> list = new TreeSet<>();

		for (String token : tokens) {
			if(token.isEmpty())
				continue;

			if (token.contains("-")) {
				String[] range = token.split("-");
				try {
					int start = Integer.parseInt(range[0]);
					int end = Integer.parseInt(range[1]);

					if (end < start) {
						throw new IllegalArgumentException("Range end less than range start.");
					}

					for (int i = start; i <= end; i++) {
						list.add(i);
					}
				} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
					throw new IllegalArgumentException("Invalid range: " + token);
				}
			} else {
				try {
					list.add(Integer.parseInt(token));
				} catch(NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}

		return list;
	}
}
