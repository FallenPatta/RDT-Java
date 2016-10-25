package distances;

import containers.TreeNode;
import containers.Vector2D;

public class Cartesian implements Distance {
	
	public Cartesian(){
		
	}

	@Override
	public double dist(TreeNode a, Vector2D b){
		return (a.getDistance() + Math.sqrt(Math.pow(a.getPoint().getX() - b.getX(),2) + Math.pow(a.getPoint().getY() - b.getY(),2)));
	}

	@Override
	public double dist(int x, int y, Vector2D b) {
		return Math.sqrt(Math.pow(x - b.getX(), 2) + Math.pow(y - b.getY(), 2));
	}

	@Override
	public double dist(Vector2D a, Vector2D b) {
		return (Math.sqrt(Math.pow(a.getX() - b.getX(),2) + Math.pow(a.getY() - b.getY(),2)));
	}

}
