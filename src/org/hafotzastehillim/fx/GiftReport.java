package org.hafotzastehillim.fx;

import org.hafotzastehillim.fx.spreadsheet.Entry;

import com.jfoenix.controls.JFXButton;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;

public class GiftReport extends VBox {

	private JFXButton findEligible;

	public GiftReport() {
		findEligible = new JFXButton("Find Eligible");
		findEligible.setId("find-eligible");
		findEligible.setOnAction(evt -> {
			ObservableList<Entry> all = FXCollections.observableArrayList();
			Model.getInstance().getSpreadsheet().searchEntries("ALL", all, (q, v, col) -> true, 0);
			Model.getInstance().getSpreadsheet().searchService().setOnSucceeded(event -> {
				all.removeIf(entry -> {
					for (int i = 0; i < entry.getTotal() / 100; i++)
						if (!entry.isGiftRecieved(i))
							return false;

					return true;
				});
			});
		});

		getChildren().add(findEligible);
	}

}
