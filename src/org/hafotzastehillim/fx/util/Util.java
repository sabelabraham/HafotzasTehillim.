package org.hafotzastehillim.fx.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.jfoenix.controls.JFXButton;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
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
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Util {

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

	public static void showErrorDialog(Throwable t) {
		StringWriter out = new StringWriter();
		t.printStackTrace(new PrintWriter(out));

		TextArea area = new TextArea();
		area.setText(out.toString());
		area.setFont(Font.font("Monospaced"));
		area.setEditable(false);
		area.setPrefColumnCount(150);
		area.setPrefRowCount(45);
		area.setStyle("-fx-text-fill: red;");

		if (Platform.isFxApplicationThread()) {
			createDialog(area, "Error", ButtonType.OK);
		} else {
			Platform.runLater(() -> createDialog(area, "Error", ButtonType.OK));
		}
	}

	public static Optional<ButtonType> createDialog(Node content, String title, ButtonType... buttonTypes) {
		return createDialog(content, title, new SimpleBooleanProperty(false), buttonTypes);
	}

	public static Optional<ButtonType> createDialog(Node content, String title, ObservableValue<Boolean> disableButtons,
			ButtonType... buttonTypes) {

		Stage dialog = new Stage();
		dialog.getIcons().add(new Image(Util.class.getResourceAsStream("/resources/images/logo.png")));
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setResizable(false);
		dialog.setTitle(title);

		List<Button> buttons = new ArrayList<>(buttonTypes.length);
		ButtonType[] response = new ButtonType[1];

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
		vbox.getChildren().addAll(content, new Separator(), buttonBox);

		Scene scene = new Scene(new StackPane(vbox));
		dialog.setScene(scene);

		if (buttons.size() > 0) {
			dialog.setOnShown(evt -> buttons.get(0).requestFocus());
		}

		dialog.showAndWait();

		return Optional.ofNullable(response[0]);
	}

	/*
	 * https://stackoverflow.com/a/36949596
	 */
	public static Optional<ButtonType> createAlertWithOptOut(AlertType type, String title, String headerText,
			String message,

			String optOutMessage, Consumer<Boolean> optOutAction, ButtonType... buttonTypes) {
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

		return alert.showAndWait();
	}
}
