package org.hafotzastehillim.fx;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.skins.JFXDatePickerSkin;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

public class DateTimeBarController {

	@FXML
	private Label dateTime;
	@FXML
	private Label hebrewDate;
	@FXML
	private JFXDatePicker calendar;

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter
			.ofPattern("EEEE\t\t MMMM dd, yyyy \t\t h:mm");

	private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, ''yy");

	private static final HebrewDateFormatter HEBREW_FORMATTER = new HebrewDateFormatter();
	static {
		HEBREW_FORMATTER.setHebrewFormat(true);
	}

	private static final LocalDate NOW = LocalDate.now();

	@FXML
	private void initialize() {
		bindToTime(dateTime);

		JewishCalendar cal = new JewishCalendar();
		hebrewDate.setText(HEBREW_FORMATTER.format(cal) + " \t\t  יום " + HEBREW_FORMATTER.formatDayOfWeek(cal));

		calendar.setDayCellFactory(dp -> new HebrewDateCell(dp));
		calendar.setValue(NOW);
		calendar.valueProperty().addListener((obs, ov, nv) -> {
			if (nv != NOW)
				calendar.setValue(NOW);
		});

		Platform.runLater(() -> {
			JFXDatePickerSkin skin = (JFXDatePickerSkin) calendar.getSkin();
			VBox monthYear = (VBox) (((Parent) skin.getPopupContent()).getChildrenUnmodifiable().get(0));
			Label label = (Label) (((Parent) monthYear.getChildren().get(1)).getChildrenUnmodifiable().get(0));

			String date = NOW.format(SHORT_DATE_FORMATTER);
			String hebDate = HEBREW_FORMATTER.format(new JewishDate());

			String both = date + "\n" + hebDate;

			label.textProperty().addListener((obs, ov, nv) -> {
				if (nv.equals(both))
					return;

				label.setText(both);
			});
			label.setText(both);

			label.setFont(Font.font("System", FontWeight.BOLD, 25));
		});
	}

	private void bindToTime(Label l) {
		Timeline first = new Timeline();
		Timeline second = new Timeline();

		first.getKeyFrames().addAll(new KeyFrame(Duration.seconds(0), evt -> {
			LocalDateTime time = LocalDateTime.now();
			l.setText(time.format(TIME_FORMATTER));

			if (time.getSecond() == 0) {
				first.stop();
				second.play();
			}
		}), new KeyFrame(Duration.seconds(1)));
		
		second.getKeyFrames().addAll(new KeyFrame(Duration.seconds(0), evt -> {
			LocalDateTime time = LocalDateTime.now();
			l.setText(time.format(TIME_FORMATTER));

		}), new KeyFrame(Duration.minutes(1)));
		
		first.setCycleCount(Animation.INDEFINITE);
		second.setCycleCount(Animation.INDEFINITE);
		
		first.play();
	}
}
