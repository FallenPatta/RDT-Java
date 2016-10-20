package distances;

import containers.TreeNode;
import containers.Vector2D;

public interface Distance {
	abstract public double dist(TreeNode a, Vector2D b);
	
	abstract public double dist(int x, int y, Vector2D b);
}
