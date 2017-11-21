package org.hafotzastehillim.fx.util;

import java.time.LocalDate;

import javafx.beans.property.SimpleStringProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

public class HebrewDateCell extends DateCell {

	private static final PseudoClass today = PseudoClass.getPseudoClass("today");
	private static final PseudoClass selected = PseudoClass.getPseudoClass("selected");

	private static final HebrewDateFormatter HEBREW_DAY_FORMATTER = new HebrewDateFormatter();
	static {
		HEBREW_DAY_FORMATTER.setHebrewFormat(true);
		HEBREW_DAY_FORMATTER.setUseGershGershayim(false);
	}

	private DatePicker picker;

	public HebrewDateCell(DatePicker picker) {
		this.picker = picker;
	}

	@Override
	public void updateItem(LocalDate item, boolean empty) {
		super.updateItem(item, empty);

		JewishDate jewish = new JewishDate(java.sql.Date.valueOf(item));

		String[] hebFmt = HEBREW_DAY_FORMATTER.format(jewish).split("\\s");

		Text text = new Text(getText());
		text.getStyleClass().add("text");
		text.setFontSmoothingType(FontSmoothingType.GRAY);
		Text hebText = new Text(hebFmt[0] + " " + hebFmt[1]);
		hebText.getStyleClass().add("hebrew-text");
		
		pseudoClassStateChanged(selected, item.isEqual(picker.getValue()));
		pseudoClassStateChanged(today, item.isEqual(LocalDate.now()));

		VBox box = new VBox(text, hebText);
		box.setAlignment(Pos.CENTER);
		setGraphic(box);

		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

		getStyleClass().add("hebrew-date-cell");
		getStylesheets().add(getClass().getResource("/resources/css/hebrew-date-cell.css").toExternalForm());
	}
}
