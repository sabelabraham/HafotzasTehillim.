import com.jfoenix.controls.JFXCheckBox;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JFXCheckBoxTest extends Application {

	@Override
	public void start(Stage primaryStage) {
		
		JFXCheckBox check = new JFXCheckBox("Check me!");
		check.setFocusTraversable(false);
		System.out.println("Checked Color: " + check.getCheckedColor());
		System.out.println("Unchecked Color: " + check.getUnCheckedColor());
		
		check.setCheckedColor(Color.RED);
		check.setUnCheckedColor(Color.RED);
		
		System.out.println("Checked Color: " + check.getCheckedColor());
		System.out.println("Unchecked Color: " + check.getUnCheckedColor());
		
		final BorderPane root = new BorderPane();
		root.setCenter(check);
		primaryStage.setScene(new Scene(root, 600, 400));
		primaryStage.show();
	}


	public static void main(String[] args) {
		launch(args);
	}
}