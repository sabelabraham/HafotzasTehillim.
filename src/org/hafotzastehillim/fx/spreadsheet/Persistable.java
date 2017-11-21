package org.hafotzastehillim.fx.spreadsheet;

import java.util.List;

public interface Persistable {

	List<String> getData();
	
	void setData(List<String> data);
	
	int getRow();
	
	Spreadsheet getSheet();
	
	void reload();
	
	void save();
	
	void delete();
}
