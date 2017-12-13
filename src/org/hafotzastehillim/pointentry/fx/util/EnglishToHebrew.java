package org.hafotzastehillim.pointentry.fx.util;

import java.util.HashMap;
import java.util.Map;

public class EnglishToHebrew {

	private static final Map<Character, Character> map = new HashMap<>();

	static {
		map.put('q', '/');
		map.put('w', '\'');
		map.put('e', '\u05e7');
		map.put('r', '\u05e8');
		map.put('t', '\u05d0');
		map.put('y', '\u05d8');
		map.put('u', '\u05d5');
		map.put('i', '\u05df');
		map.put('o', '\u05dd');
		map.put('p', '\u05e4');
		map.put('a', '\u05e9');
		map.put('s', '\u05d3');
		map.put('d', '\u05d2');
		map.put('f', '\u05db');
		map.put('g', '\u05e2');
		map.put('h', '\u05d9');
		map.put('j', '\u05d7');
		map.put('k', '\u05dc');
		map.put('l', '\u05da');
		map.put(';', '\u05e3');
		map.put('\'', ',');
		map.put('z', '\u05d6');
		map.put('x', '\u05e1');
		map.put('c', '\u05d1');
		map.put('v', '\u05d4');
		map.put('b', '\u05e0');
		map.put('n', '\u05de');
		map.put('m', '\u05e6');
		map.put(',', '\u05ea');
		map.put('.', '\u05e5');
		map.put('/', '.');
	}


	public static char convert(char src) {
		if(map.containsKey(src))
			return map.get(src);

		return src;
	}

	public static String convert(String src) {
		StringBuilder b = new StringBuilder(src.length());
		for(char c : src.toCharArray()) {
			b.append(convert(c));
		}

		return b.toString();
	}
}
