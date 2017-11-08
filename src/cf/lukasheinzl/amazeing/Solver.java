package cf.lukasheinzl.amazeing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import cf.lukasheinzl.graph.CoordinateNode;

/**
 * This class is used to turn images into node-graphs and to draw the found path into images.
 * 
 * @author Lukas Heinzl
 *
 */
public class Solver{

	/**
	 * This method turns an image into a list of nodes
	 * 
	 * @param bi
	 *            The image to parse
	 * @return The list of nodes representing the image
	 */
	public static List<CoordinateNode> nodeify(BufferedImage bi){
		int width = bi.getWidth();
		int height = bi.getHeight();

		// Stores the current top-most CoordinateNode
		// Needed for connecting top and bottom
		CoordinateNode[] topCoordinateNodes = new CoordinateNode[width];
		List<CoordinateNode> CoordinateNodes = new ArrayList<>();

		outer: for(int row = 0; row < height; row++){
			// Stores the current left-most CoordinateNode
			// Needed for connecting left and right
			CoordinateNode leftCoordinateNode = null;

			for(int col = 0; col < width; col++){

				// Only consider white pixels
				// 0xFFFFFFFF - RGB + alpha-channel
				if(bi.getRGB(col, row) == 0xFFFFFFFF){

					// Top-most row only has one tile, the entrance
					if(row == 0){
						CoordinateNode n = new CoordinateNode(col, row);
						topCoordinateNodes[col] = n;
						CoordinateNodes.add(n);
						continue outer;
					}

					// same for bottom-most row
					if(row == height - 1){
						CoordinateNode n = new CoordinateNode(col, row);

						if(topCoordinateNodes[col] != null){
							n.link(topCoordinateNodes[col]);
							topCoordinateNodes[col].link(n);
						}

						topCoordinateNodes[col] = n;

						CoordinateNodes.add(n);
						continue outer;
					}

					CoordinateNode n = null;
					boolean linkLeft = false;
					boolean linkTop = false;

					// tile to the left is non-white
					// all CoordinateNodes created are the new leftCoordinateNode
					// because we started a new path
					if(bi.getRGB(col - 1, row) != 0xFFFFFFFF){

						// tile to the right is white
						if(bi.getRGB(col + 1, row) == 0xFFFFFFFF){
							n = new CoordinateNode(col, row);

							// only link top when there is no wall
							if(bi.getRGB(col, row - 1) == 0xFFFFFFFF){
								linkTop = true;
							}
						} else{
							// tile to the right is non-white
							// and top or bottom tile is also non-white
							// this checks for bottom and top dead-ends
							if(bi.getRGB(col, row + 1) != 0xFFFFFFFF){
								n = new CoordinateNode(col, row);
								linkTop = true;
							} else if(bi.getRGB(col, row - 1) != 0xFFFFFFFF){
								n = new CoordinateNode(col, row);
							}
						}
					}

					// tile to the right is non-white and to the left is white
					// either end-of-path or junction
					else if(bi.getRGB(col + 1, row) != 0xFFFFFFFF){
						if(bi.getRGB(col - 1, row) == 0xFFFFFFFF){
							n = new CoordinateNode(col, row);
							linkLeft = true;

							if(bi.getRGB(col, row - 1) == 0xFFFFFFFF){
								linkTop = true;
							}
						}
					}

					// tile to the top is white and to the bottom non-white
					// t-junction going up, left and right
					// or corner going up or 4-way-junction
					else if(bi.getRGB(col, row - 1) == 0xFFFFFFFF){
						// bottom is non-white
						if(bi.getRGB(col, row + 1) != 0xFFFFFFFF){
							n = new CoordinateNode(col, row);
							linkLeft = true;
							linkTop = true;
						}

						// all directions are white
						else if(bi.getRGB(col, row + 1) == 0xFFFFFFFF){
							if(bi.getRGB(col - 1, row) == 0xFFFFFFFF){
								if(bi.getRGB(col + 1, row) == 0xFFFFFFFF){
									n = new CoordinateNode(col, row);
									linkLeft = true;
									linkTop = true;
								}
							}
						}
					}

					// tile to the bottom is white and to the top is non-white
					// t-junction going down, left and right
					// or corner going down
					else if(bi.getRGB(col, row + 1) == 0xFFFFFFFF){
						if(bi.getRGB(col, row - 1) != 0xFFFFFFFF){
							n = new CoordinateNode(col, row);
							linkLeft = true;
						}
					}

					if(linkLeft){
						n.link(leftCoordinateNode);
						leftCoordinateNode.link(n);
					}

					if(linkTop && topCoordinateNodes[col] != null){
						n.link(topCoordinateNodes[col]);
						topCoordinateNodes[col].link(n);
					}

					// if a CoordinateNode got created update left and top positions
					// and add CoordinateNode to the list
					if(n != null){
						leftCoordinateNode = n;
						topCoordinateNodes[col] = n;
						CoordinateNodes.add(n);
					}
				}
			}
		}

		return CoordinateNodes;
	}

	/**
	 * This draws the found path in red into the image.
	 * 
	 * @param bi
	 *            The image to draw the path in (should be the same as used by {@link #nodeify(BufferedImage)}
	 * @param path
	 *            The list of nodes that contain the path from start to finish
	 */
	public static void printPath(BufferedImage bi, List<CoordinateNode> path){
		for(int i = 0; i < path.size() - 1; i++){
			CoordinateNode n = path.get(i);
			CoordinateNode next = path.get(i + 1);

			// draw the start coordinate
			bi.setRGB(n.getX(), n.getY(), 0xFFFF0000);

			// draw the path between two nodes
			if(n.getX() == next.getX()){
				if(n.getY() <= next.getY()){
					for(int y = n.getY() + 1; y <= next.getY(); y++){
						bi.setRGB(n.getX(), y, 0xFFFF0000);
					}
				} else{
					for(int y = next.getY() + 1; y <= n.getY(); y++){
						bi.setRGB(n.getX(), y, 0xFFFF0000);
					}
				}
			} else{
				if(n.getX() <= next.getX()){
					for(int x = n.getX() + 1; x <= next.getX(); x++){
						bi.setRGB(x, n.getY(), 0xFFFF0000);
					}
				} else{
					for(int x = next.getX() + 1; x <= n.getX(); x++){
						bi.setRGB(x, n.getY(), 0xFFFF0000);
					}
				}
			}
		}
	}
}
