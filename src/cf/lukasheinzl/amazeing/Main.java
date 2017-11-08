package cf.lukasheinzl.amazeing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;;

public class Main extends Application{

	private static Stage stage = null;

	public static void main(String[] args){
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception{
		Parent root = FXMLLoader.load(getClass().getResource("FXML.fxml"));
		Scene scene = new Scene(root);

		stage.setTitle("aMAZEing");
		stage.setScene(scene);
		stage.show();

		Main.stage = stage;
	}

	/**
	 * This returns the stage of this javafx application. The stage is used in the
	 * {@link cf.lukasheinzl.amazeing.FXController} class for the FileChooser dialog.
	 * 
	 * @return The stage
	 */
	public static Stage getStage(){
		return Main.stage;
	}

}
