package org.hafotzastehillim.pointentry.fx;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.hafotzastehillim.pointentry.fx.util.DialogUtils;
import org.hafotzastehillim.pointentry.fx.util.Util;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.skins.JFXDatePickerSkin;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

public class DateTimeBar extends HBox {

	public DateTimeBar() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/DateTimeBar.fxml"));
			loader.setController(this);
			loader.setRoot(this);
			loader.load();
		} catch (IOException e) {
			DialogUtils.showErrorDialog(e);
			return;
		}

		getStyleClass().add("date-time-bar");
		getStylesheets().add(getClass().getResource("/resources/css/date-time-bar.css").toExternalForm());
	}

	@FXML
	private Label day;
	@FXML
	private Label date;
	@FXML
	private Label time;
	@FXML
	private Label hebrewDay;
	@FXML
	private Label hebrewDate;
	@FXML
	private JFXDatePicker calendar;

	private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("EEEE");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm");

	private static final HebrewDateFormatter HEBREW_FORMATTER = new HebrewDateFormatter();
	static {
		HEBREW_FORMATTER.setHebrewFormat(true);
	}

	private static final LocalDate NOW = LocalDate.now();

	@FXML
	private void initialize() {
		bindToTime(time);

		day.setText(DAY_FORMATTER.format(NOW));
		date.setText(DATE_FORMATTER.format(NOW));

		JewishCalendar cal = new JewishCalendar();
		hebrewDay.setText("יום " + HEBREW_FORMATTER.formatDayOfWeek(cal));
		hebrewDate.setText(HEBREW_FORMATTER.format(cal));

		calendar.valueProperty().addListener((obs, ov, nv) -> calendar.setValue(null));

		Util.fixDatePicker(calendar);
	}

	private void bindToTime(Label l) {
		Timeline first = new Timeline();
		Timeline second = new Timeline();

		first.getKeyFrames().addAll(new KeyFrame(Duration.seconds(0), evt -> {
			LocalTime time = LocalTime.now();
			l.setText(time.format(TIME_FORMATTER));

			if (time.getSecond() == 0) {
				first.stop();
				second.play();
			}
		}), new KeyFrame(Duration.seconds(1)));

		second.getKeyFrames().addAll(new KeyFrame(Duration.seconds(0), evt -> {
			LocalTime time = LocalTime.now();
			l.setText(time.format(TIME_FORMATTER));

		}), new KeyFrame(Duration.minutes(1)));

		first.setCycleCount(Animation.INDEFINITE);
		second.setCycleCount(Animation.INDEFINITE);

		first.play();
	}
}
