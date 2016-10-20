package distances;

import containers.TreeNode;
import containers.Vector2D;

public class Manhattan implements Distance {

	public Manhattan(){
		
	}
	
	@Override
	public double dist(TreeNode a, Vector2D b) {
		return (a.getDistance() + Math.abs(a.getPoint().getX() - b.getX()) + Math.abs(a.getPoint().getY() - b.getY()));
	}

	@Override
	public double dist(int x, int y, Vector2D b) {
		return (Math.abs(x - b.getX()) + Math.abs(y - b.getY()));
	}

}
