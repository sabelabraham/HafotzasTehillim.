package org.hafotzastehillim.pointentry.fx.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.hafotzastehillim.pointentry.fx.Main;
import org.hafotzastehillim.pointentry.fx.Model;

import com.jfoenix.controls.JFXButton;
import com.sun.javafx.application.PlatformImpl;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogUtils {

	private static final Model model = Model.getInstance();

	public static void showErrorDialog(Throwable e) {
		e.printStackTrace();

		StringWriter out = new StringWriter();
		e.printStackTrace(new PrintWriter(out));

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
			Stage dialog = newStage();
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setResizable(false);
			dialog.setTitle(title);

			List<Button> buttons = new ArrayList<>(buttonTypes.length);

			for (ButtonType type : buttonTypes) {
				JFXButton b = new JFXButton(type.getText());
				b.setDefaultButton(type.getButtonData().isDefaultButton());
				b.setCancelButton(type.getButtonData().isCancelButton());
				dialogButtonStyling(b);

				if (b.isDefaultButton()) {
					b.disableProperty().bind(disableButtons);
				}

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

			dialog.showAndWait();
		});

		return Optional.ofNullable(response[0]);
	}

	public static void dialogButtonStyling(JFXButton button) {
		button.setText(button.getText().toUpperCase());
		button.textProperty().addListener((ons, ov, nv) -> button.setText(nv.toUpperCase()));
		button.styleProperty()
				.bind(Bindings.when(button.defaultButtonProperty())
						.then("-fx-background-color: #4285f4; -fx-text-fill: white; -jfx-button-type: RAISED;")
						.otherwise("-fx-text-fill: #4285f4;"));

		button.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
		button.setPadding(new Insets(5, 17, 5, 17));
		button.setFocusTraversable(false);
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
			alert.initOwner(model.getCurrentStage());
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
			alert.initOwner(model.getCurrentStage());

			response.add(alert.showAndWait());
		});

		return response.get(0);
	}

	public static final Image ICON;

	static {
		ICON = new Image(Main.class.getResource("/resources/images/logo.png").toExternalForm());
	}

	public static void setupStage(Stage stage) {
		model.registerStage(stage);
		stage.getIcons().add(ICON);

		if (stage.getScene() != null) {
			stage.getScene().getStylesheets().add(Util.class.getResource("/resources/css/root.css").toExternalForm());
		}

		stage.sceneProperty().addListener((obs, ov, nv) -> nv.getStylesheets()
				.add(Util.class.getResource("/resources/css/root.css").toExternalForm()));

		if (model.getOpenStages().size() > 0)
			stage.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
				if (evt.getCode() == KeyCode.ESCAPE) {
					stage.close();
				}
			});
	}

	public static Stage newStage() {
		Stage s = new Stage();
		setupStage(s);

		return s;
	}
}
