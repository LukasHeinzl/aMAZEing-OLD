package cf.lukasheinzl.amazeing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import cf.lukasheinzl.graph.CoordinateNode;
import cf.lukasheinzl.graph.algorithm.GraphAlgorithm;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * This class is a subclass of {@link java.lang.Thread Thread} and is responsible for solving mazes independently from
 * the gui-thread.
 * 
 * @author Lukas Heinzl
 *
 */
public class SolveThread extends Thread{

	private FXController	cont;
	private ImageView		imgView;

	private BufferedImage	bi;
	private String			imgPath;
	private String			algorithm;

	/**
	 * Constructs a solver thread which is used to solve mazes in image form
	 * 
	 * @param cont
	 *            The gui-controller this thread is associated with
	 * @param imgView
	 *            The image-view the result should be display in
	 * @param bi
	 *            The image from which the maze should be solved
	 * @param imgPath
	 *            The path to the file where the solved maze should be saved to
	 * @param algorithm
	 *            The algorithm to used by the solver
	 */
	public SolveThread(FXController cont, ImageView imgView, BufferedImage bi, String imgPath, String algorithm){
		setDaemon(true);
		this.cont = cont;
		this.imgView = imgView;
		this.bi = bi;
		this.imgPath = imgPath;
		this.algorithm = algorithm;
	}

	public void run(){
		// disable everything so the user can not interfere
		cont.setStatus(false, false, false);
		cont.appendOutput("Solving maze with " + algorithm + "...\n");

		try{
			// get timestamp, nodes and copy picture because of a java bug
			long startT = System.currentTimeMillis();
			List<CoordinateNode> nodes = Solver.nodeify(bi);
			bi = copy(bi);

			// get the algorithm, solve the maze and print it onto the image
			GraphAlgorithm<CoordinateNode> a = GraphAlgorithm.getAlgorithm(algorithm);

			List<CoordinateNode> path = a.findPath(nodes);

			if(path == null){
				Platform.runLater(() -> new Alert(AlertType.INFORMATION, "Sorry, no path has been found!").show());
				return;
			}

			Solver.printPath(bi, path);

			// write the image to disk and update it in the gui
			ImageIO.write(bi, "png", new File(imgPath + "-solved.png"));
			imgView.setImage(new Image("file:" + imgPath + "-solved.png"));

			// get timestamp and write all debug information to the screen
			long endT = System.currentTimeMillis();
			double time = (endT - startT) / 1000.0;
			String savedPath = imgPath + "-solved.png";
			savedPath = savedPath.substring(savedPath.lastIndexOf(File.separatorChar) + 1);

			cont.appendOutput("\nSolved, saved as: " + savedPath + "\n");
			cont.appendOutput("Node count: " + path.size() + "\n");
			cont.appendOutput("Path length: " + getTotalLength(path) + "\n");
			cont.appendOutput("Time took: " + time + " seconds");
		} catch(Exception e){
			Platform.runLater(
					() -> new Alert(AlertType.ERROR, "An error occurred while solving maze: (" + e.getClass().getName() + ") " + e.getMessage())
							.show());
		}

		// re-enable the loading of images
		cont.setStatus(true, false, false);
	}

	/**
	 * This method creates a new {@link java.awt.image.BufferedImage BufferedImage} object and copys the pixel data from
	 * the old one to the new one. This method is needed in order to draw the found path into the image later on
	 * otherwise the path will be black and not red because of a java bug.
	 * 
	 * @param bi
	 *            The image to copy
	 * @return The copy
	 */
	private BufferedImage copy(BufferedImage bi){
		BufferedImage bi2 = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for(int x = 0; x < bi.getWidth(); x++){
			for(int y = 0; y < bi.getHeight(); y++){
				bi2.setRGB(x, y, bi.getRGB(x, y));
			}
		}

		return bi2;
	}

	/**
	 * This method calculated the length of the found path
	 * 
	 * @param path
	 *            The list of nodes that contain the path from start to finish
	 * @return The length
	 */
	private int getTotalLength(List<CoordinateNode> path){
		int len = 0;

		for(int i = 0; i < path.size() - 1; i++){
			CoordinateNode n = path.get(i);
			CoordinateNode next = path.get(i + 1);
			len += n.getDistance(next);
		}

		return len + 1;
	}

}
