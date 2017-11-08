package cf.lukasheinzl.amazeing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * This class is the gui-controller associated with this app's gui.
 * 
 * @author Lukas Heinzl
 *
 */
public class FXController implements Initializable{

	@FXML
	private Button				loadBtn;

	@FXML
	private Button				solveBtn;

	@FXML
	private ChoiceBox<String>	drpDwn;

	@FXML
	private Text				outputTxt;

	@FXML
	private ScrollPane			scrlPane;

	@FXML
	private ImageView			imgView;

	private double				zoom	= 200;
	private BufferedImage		bi;
	private String				imgPath;
	private File				lastLoadPath;

	@Override
	public void initialize(URL url, ResourceBundle rb){
		// set the possible solve-algorithms and select the first one
		drpDwn.setItems(FXCollections.observableArrayList("DepthFirst", "BreadthFirst", "Dijkstra", "AStar"));
		drpDwn.getSelectionModel().select(0);

		// add event handler to enable zooming in the image-view
		scrlPane.addEventFilter(ScrollEvent.ANY, this::zoom);

		setStatus(true, false, false);
		outputTxt.setText("");
	}

	/**
	 * This method loads an image from disk and displays it in the image-view
	 * 
	 * @param e
	 *            The corresponding ActionEvent
	 */
	public void load(ActionEvent e){
		outputTxt.setText("Loading image...\n");
		setStatus(true, false, false);

		try{
			// display file chooser to handle image-selection
			FileChooser fc = new FileChooser();

			if(lastLoadPath != null){
				fc.setInitialDirectory(lastLoadPath);
			}

			fc.setTitle("Open maze as .png file");
			fc.getExtensionFilters().add(new ExtensionFilter("PNG", "*.png"));

			File f = fc.showOpenDialog(Main.getStage());

			if(f == null){
				appendOutput("Canceled\n");
				return;
			}

			// load and display image
			imgPath = f.getAbsolutePath();
			lastLoadPath = new File(imgPath).getParentFile();

			bi = ImageIO.read(f);
			imgView.setImage(new Image("file:" + imgPath));

			appendOutput("Loaded image: ");
			appendOutput(imgPath.substring(imgPath.lastIndexOf(File.separatorChar) + 1) + "\n");
			setStatus(true, true, true);
		} catch(Exception ex){
			new Alert(AlertType.ERROR, "An error occurred while loading maze: (" + ex.getClass().getName() + ") " + ex.getMessage()).show();
			appendOutput("Error loading image\n");
		}
	}

	/**
	 * This method creates a new {@link cf.lukasheinzl.amazeing.SolveThread SolveThread} that does the maze solving
	 * 
	 * @param e
	 *            The corresponding ActionEvent
	 */
	public void solve(ActionEvent e){
		new SolveThread(this, imgView, bi, imgPath, drpDwn.getSelectionModel().getSelectedItem()).start();
	}

	/**
	 * This method handles the mouse-wheel scrolling of the image-view
	 * 
	 * @param e
	 *            The corresponding ScrollEvent
	 */
	private void zoom(ScrollEvent e){
		if(imgView.getImage() == null){
			return;
		}

		// update zoom factor
		if(e.getDeltaY() > 0){
			zoom *= 1.1;
		} else if(e.getDeltaY() < 0){
			zoom /= 1.1;
		}

		// update image-view
		imgView.setFitWidth(zoom * 4);
		imgView.setFitHeight(zoom * 4);
	}

	/**
	 * This method sets the status of the 3 user-interactable gui-element.
	 * 
	 * @param load
	 *            Boolean if the load button should be enabled
	 * @param solve
	 *            Boolean if the solve button should be enabled
	 * @param drop
	 *            Boolean if the dropdown-selection should be enabled
	 */
	public void setStatus(boolean load, boolean solve, boolean drop){
		loadBtn.setDisable(!load);
		solveBtn.setDisable(!solve);
		drpDwn.setDisable(!drop);
	}

	/**
	 * This method appends output to the debug-text-field
	 * 
	 * @param txt
	 *            The text to append
	 */
	public void appendOutput(String txt){
		outputTxt.setText(outputTxt.getText() + txt);
	}

}
