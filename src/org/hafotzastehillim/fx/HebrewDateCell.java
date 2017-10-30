package org.hafotzastehillim.fx;

import java.time.LocalDate;
import javafx.geometry.Pos;
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
		text.setFontSmoothingType(FontSmoothingType.GRAY);
		Text hebText = new Text(hebFmt[0] + " " + hebFmt[1]);

		text.setFont(Font.font("System", FontWeight.BOLD, 12));
		hebText.setFont(Font.font(9));

		if (item.equals(picker.getValue())) {
			text.setFill(Color.WHITE);
			hebText.setFill(Color.WHITE);
		}

		VBox box = new VBox(text, hebText);
		box.setAlignment(Pos.CENTER);
		setGraphic(box);
		setText("");
	}
}
