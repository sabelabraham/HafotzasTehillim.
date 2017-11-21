package org.hafotzastehillim.fx.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.hafotzastehillim.fx.DetailsPane;
import org.hafotzastehillim.fx.Main;
import org.hafotzastehillim.fx.Model;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.skins.JFXDatePickerSkin;
import com.sun.javafx.application.PlatformImpl;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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

			String date = LocalDate.now().format(SHORT_DATE_FORMATTER);
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

			// https://uwesander.de/?p=208
			int diff = newText.length() - originalText.length();
			if (diff != 0) {
				change.setAnchor(Math.max(0, change.getAnchor() + diff));
				change.setCaretPosition(Math.max(0, change.getCaretPosition() + diff));
			}

			return change;
		});
	}

	public static <T> TextFormatter<T> asYouTypeCurrencyFormatter() {
		return new TextFormatter<T>(change -> {
			if (!change.isContentChange())
				return change;

			String originalText = change.getControlNewText();
			char[] chars = originalText.replaceAll("[^\\d]", "").toCharArray();

			StringBuilder tempText = new StringBuilder();

			for (int i = chars.length - 1; i >= 0; i--) {
				if (chars.length - i == 3)
					tempText.append('.');

				if ((chars.length - i) > 3 && (chars.length - i) % 3 == 0)
					tempText.append(',');
				
				tempText.append(chars[i]);
			}

			StringBuilder newText = new StringBuilder(tempText.length());
			chars = tempText.toString().toCharArray();

			for (int i = chars.length - 1; i >= 0; i--) {
				newText.append(chars[i]);
			}

			change.setRange(0, change.getControlText().length());
			change.setText(newText.toString());

			// https://uwesander.de/?p=208
			int diff = newText.length() - originalText.length();
			if (diff != 0) {
				change.setAnchor(Math.max(0, change.getAnchor() + diff));
				change.setCaretPosition(Math.max(0, change.getCaretPosition() + diff));
			}

			return change;
		});
	}

	public static void showErrorDialog(Throwable t) {
		t.printStackTrace();

		StringWriter out = new StringWriter();
		t.printStackTrace(new PrintWriter(out));

		TextArea area = new TextArea();
		area.setText(out.toString());
		area.setFont(Font.font("Monospaced"));
		area.setEditable(false);
		area.setPrefColumnCount(150);
		area.setPrefRowCount(45);
		area.setStyle("-fx-text-fill: red;");

		createDialog(area, "Error", ButtonType.OK);
	}

	public static Optional<ButtonType> createDialog(Node content, String title, ButtonType... buttonTypes) {
		return createDialog(content, title, new SimpleBooleanProperty(false), buttonTypes);
	}

	@SuppressWarnings("restriction")
	public static Optional<ButtonType> createDialog(Node content, String title, ObservableValue<Boolean> disableButtons,
			ButtonType... buttonTypes) {

		ButtonType[] response = new ButtonType[1];

		PlatformImpl.runAndWait(() -> {
			Stage dialog = new Stage();
			Model.getInstance().registerStage(dialog);
			dialog.getIcons().add(Main.ICON);
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setResizable(false);
			dialog.setTitle(title);

			List<Button> buttons = new ArrayList<>(buttonTypes.length);

			for (ButtonType type : buttonTypes) {
				Button b = new JFXButton(type.getText());
				b.setDefaultButton(type.getButtonData().isDefaultButton());
				b.setCancelButton(type.getButtonData().isCancelButton());
				b.disableProperty().bind(disableButtons);

				b.setOnAction(evt -> {
					response[0] = type;
					dialog.close();
				});

				buttons.add(b);
			}

			HBox buttonBox = new HBox();
			buttonBox.getChildren().addAll(buttons);
			buttonBox.setAlignment(Pos.CENTER_RIGHT);
			buttonBox.setSpacing(10);
			buttonBox.setPadding(new Insets(10));

			VBox vbox = new VBox();
			vbox.getChildren().add(content);

			if (buttonTypes.length > 0)
				vbox.getChildren().addAll(new Separator(), buttonBox);

			Scene scene = new Scene(new StackPane(vbox));
			dialog.setScene(scene);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
				if (evt.getCode() == KeyCode.ESCAPE)
					dialog.close();
			});

			if (buttons.size() > 0) {
				dialog.setOnShown(evt -> buttons.get(0).requestFocus());
			}

			dialog.showAndWait();
		});

		return Optional.ofNullable(response[0]);
	}

	/*
	 * https://stackoverflow.com/a/36949596
	 */
	@SuppressWarnings("restriction")
	public static Optional<ButtonType> createAlertWithOptOut(AlertType type, String title, String headerText,
			String message, String optOutMessage, Consumer<Boolean> optOutAction, ButtonType... buttonTypes) {

		List<Optional<ButtonType>> response = new ArrayList<Optional<ButtonType>>();

		PlatformImpl.runAndWait(() -> {
			Alert alert = new Alert(type);

			alert.getDialogPane().applyCss();
			Node graphic = alert.getDialogPane().getGraphic();

			alert.setDialogPane(new DialogPane() {
				@Override
				protected Node createDetailsButton() {
					CheckBox optOut = new CheckBox();
					optOut.setText(optOutMessage);
					optOut.setOnAction(e -> optOutAction.accept(optOut.isSelected()));
					return optOut;
				}
			});
			alert.getDialogPane().getButtonTypes().addAll(buttonTypes);
			alert.getDialogPane().setContentText(message);
			// Fool the dialog into thinking there is some expandable content
			// a Group won't take up any space if it has no children
			alert.getDialogPane().setExpandableContent(new Group());
			alert.getDialogPane().setExpanded(true);
			// Reset the dialog graphic using the default style
			alert.getDialogPane().setGraphic(graphic);
			alert.setTitle(title);
			alert.setHeaderText(headerText);
			alert.setResizable(false);
			alert.initOwner(Model.getInstance().getCurrentStage());
			response.add(alert.showAndWait());
		});

		return response.get(0);
	}

	@SuppressWarnings("restriction")
	public static Optional<ButtonType> createAlert(AlertType type, String title, String headerText, String message,
			ButtonType... buttons) {
		List<Optional<ButtonType>> response = new ArrayList<Optional<ButtonType>>();

		PlatformImpl.runAndWait(() -> {
			Alert alert = new Alert(type, message, buttons);
			alert.setTitle(title);
			alert.setHeaderText(headerText);
			alert.initOwner(Model.getInstance().getCurrentStage());

			response.add(alert.showAndWait());
		});

		return response.get(0);
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
}
