package org.hafotzastehillim.pointentry.fx.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hafotzastehillim.pointentry.fx.DetailsPane;
import org.hafotzastehillim.pointentry.fx.Main;
import org.hafotzastehillim.pointentry.fx.Model;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.skins.JFXDatePickerSkin;
import com.sun.javafx.application.PlatformImpl;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

public class Util {

	public static <T> void commitOnFocusLose(Spinner<T> spinner) {
		// hack for committing on focus lose
		TextFormatter<T> formatter = new TextFormatter<>(spinner.getValueFactory().getConverter(), spinner.getValue());
		spinner.getEditor().setTextFormatter(formatter);
		spinner.getValueFactory().valueProperty().bindBidirectional(formatter.valueProperty());
	}

	public static void selectOnFocus(TextInputControl text) {
		text.focusedProperty().addListener((obs, ov, nv) -> {
			if (nv)
				Platform.runLater(() -> text.selectAll());
		});
	}

	public static Circle circleClip(Region r) {
		Circle clip = new Circle();
		clip.radiusProperty().bind(Bindings.when(r.widthProperty().lessThan(r.heightProperty()))
				.then(r.widthProperty().divide(2).subtract(1)).otherwise(r.heightProperty().divide(2).subtract(1)));

		clip.centerXProperty().bind(r.widthProperty().divide(2));
		clip.centerYProperty().bind(r.heightProperty().divide(2));
		r.setClip(clip);

		return clip;
	}

	public static Pane niceList(ListView<?> list, DoubleProperty cellHeight) {

		IntegerBinding size = Bindings.createIntegerBinding(() -> list.getItems().size(), list.getItems());
		DoubleBinding niceHeight = cellHeight.multiply(size);

		Rectangle innerClip = new Rectangle();
		innerClip.setX(2);
		innerClip.setY(2);
		innerClip.widthProperty().bind(list.widthProperty().subtract(4));
		innerClip.heightProperty().bind(niceHeight.subtract(4));
		innerClip.setArcWidth(10);
		innerClip.setArcHeight(10);
		// list.setClip(innerClip);

		Pane listPane = new StackPane();

		NumberBinding min = new When(niceHeight.lessThan(list.heightProperty().add(1))).then(niceHeight)
				.otherwise(list.heightProperty().add(1));

		Rectangle outerClip = new Rectangle();
		outerClip.xProperty().bind(list.layoutXProperty());
		outerClip.yProperty().bind(list.layoutYProperty());
		outerClip.widthProperty().bind(list.widthProperty());
		outerClip.heightProperty().bind(min);
		outerClip.arcWidthProperty().bind(innerClip.arcWidthProperty());
		outerClip.arcHeightProperty().bind(innerClip.arcHeightProperty());

		listPane.getChildren().add(list);
		listPane.getStyleClass().add("nice-list-pane");
		// listPane.setClip(outerClip);

		return listPane;
	}

	private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM. d, ''yy");

	private static final HebrewDateFormatter HEBREW_FORMATTER = new HebrewDateFormatter();
	static {
		HEBREW_FORMATTER.setHebrewFormat(true);
	}

	public static void fixDatePicker(JFXDatePicker picker) {
		picker.setDayCellFactory(dp -> new HebrewDateCell(dp));

		Platform.runLater(() -> {
			JFXDatePickerSkin skin = (JFXDatePickerSkin) picker.getSkin();
			VBox monthYear = (VBox) (((Parent) skin.getPopupContent()).getChildrenUnmodifiable().get(0));
			Label label = (Label) (((Parent) monthYear.getChildren().get(1)).getChildrenUnmodifiable().get(0));

			label.textProperty().addListener((obs, ov, nv) -> {

				LocalDate value = picker.getValue();
				if (value == null)
					value = LocalDate.now();
				java.util.Date date = java.sql.Date.valueOf(value);

				String dateString = SHORT_DATE_FORMATTER.format(value);
				String hebDateString = HEBREW_FORMATTER.format(new JewishDate(date));

				String both = dateString + "\n" + hebDateString;

				if (nv != null && nv.equals(both))
					return;

				label.setText(both);
			});

			label.setText("");

			label.setFont(Font.font("System", FontWeight.BOLD, 25));
		});
	}

	public static <T> TextFormatter<T> asYouTypePhoneFormatter() {
		AsYouTypeFormatter phoneFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter("US");

		return new TextFormatter<T>(change -> {
			if (!change.isContentChange())
				return change;

			String originalText = change.getControlNewText();
			String newText = originalText.replaceAll("[^\\d]", "");

			phoneFormatter.clear();
			for (char c : newText.toCharArray())
				newText = phoneFormatter.inputDigit(c);

			change.setRange(0, change.getControlText().length());
			change.setText(newText);

			resetCaret(change, originalText);

			return change;
		});
	}

	public static <T> TextFormatter<T> asYouTypeCurrencyFormatter() {
		return new TextFormatter<>(change -> {
			if (!change.isContentChange())
				return change;

			String originalText = change.getControlNewText();
			String strippedText = originalText.replaceAll("[^\\d]", "");
			
			if (strippedText.isEmpty()) {
				change.setText("");
				return change;
			}

			int number = Integer.parseInt(strippedText);
			char[] chars = String.valueOf(number).toCharArray();

			StringBuilder newText = new StringBuilder();

			for (int i = chars.length - 1; i >= 0; i--) {
				if (chars.length - i == 3)
					newText.append('.');

				if ((chars.length - i) > 3 && (chars.length - i) % 3 == 0)
					newText.append(',');

				newText.append(chars[i]);
			}

			if (chars.length == 1) {
				newText.append("0.0");
			} else if (chars.length == 2) {
				newText.append(".0");
			}

			change.setRange(0, change.getControlText().length());
			change.setText(newText.reverse().toString());

			resetCaret(change, originalText);

			return change;
		});
	}

	private static final PseudoClass error = PseudoClass.getPseudoClass("error");

	public static void validateField(TextInputControl txt, Function<String, Boolean> valid) {
		txt.textProperty().addListener((obs, ov, nv) -> txt.pseudoClassStateChanged(error, !valid.apply(nv)));

		txt.getStyleClass().add("text-input-control");
		txt.getStylesheets().add(Util.class.getResource("/resources/css/util.css").toExternalForm());
	}

	public static void resetCaret(TextFormatter.Change change, String originalText) {

		// https://uwesander.de/?p=208
		int diff = change.getText().length() - originalText.length();
		if (diff != 0) {
			change.setAnchor(Math.max(0, change.getAnchor() + diff));
			change.setCaretPosition(Math.max(0, change.getCaretPosition() + diff));
		}
	}

	public static void onceOnSucceeded(ObservableValue<? extends State> prop, Runnable run) {

		ChangeListener<State> listener = new ChangeListener<State>() {
			private boolean hasScheduled = false;

			@Override
			public void changed(ObservableValue<? extends State> obs, State ov, State nv) {
				if (nv == State.SCHEDULED)
					hasScheduled = true;

				if (hasScheduled && (nv == State.CANCELLED || nv == State.FAILED || nv == State.SUCCEEDED))
					prop.removeListener(this);

				if (nv == State.SUCCEEDED) {
					run.run();
				}
			};
		};

		prop.addListener(listener);
	}

	public static <T> ObservableList<T> withExtractor(ObservableList<T> list, Callback<T, Observable[]> extractor) {
		ListProperty<T> newList = new SimpleListProperty<>(FXCollections.observableArrayList(extractor));
		newList.bindContentBidirectional(list);

		return newList;
	}

}
