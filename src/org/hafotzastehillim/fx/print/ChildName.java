package org.hafotzastehillim.fx.print;

import org.hafotzastehillim.fx.spreadsheet.Entry;

public class ChildName {

	public String id;
	public String name;
	
	public ChildName(Entry e) {
		id = e.getId();
		name = e.getFirstNameYiddish();
	}
}
