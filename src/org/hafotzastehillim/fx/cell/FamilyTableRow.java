package org.hafotzastehillim.fx.cell;

import java.util.function.Function;

import org.hafotzastehillim.fx.spreadsheet.Entry;

import javafx.css.PseudoClass;
import javafx.scene.control.TableRow;

public class FamilyTableRow extends TableRow<Entry> {

	public FamilyTableRow(Function<Entry, Integer> familyIndexer) {

		itemProperty().addListener((obs, ov, nv) -> {
			if (nv == null)
				return;

			int familyIndex = familyIndexer.apply(nv);

			pseudoClassStateChanged(FIRST, familyIndex % 2 == 0);
			pseudoClassStateChanged(SECOND, familyIndex % 2 == 1);
		});

	}

	private static final PseudoClass FIRST = PseudoClass.getPseudoClass("first");
	private static final PseudoClass SECOND = PseudoClass.getPseudoClass("second");

}
