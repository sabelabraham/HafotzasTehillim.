package org.hafotzastehillim.pointentry.fx.print;

import org.hafotzastehillim.pointentry.spreadsheet.Entry;

public class ChildName {

	public String id;
	public String name;
	
	public ChildName(Entry e) {
		id = e.getId();
		name = e.getFirstNameYiddish();
	}
}
