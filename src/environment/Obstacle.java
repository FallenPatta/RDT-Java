package environment;

import containers.Vector2D;

public interface Obstacle {
	/**
	 * Returns true if point p is inside or behind the obstacle
	 * 
	 * @param p The Point
	 * @return true if blocket, false if not blocked
	 */
	abstract public boolean blocks(Line p);
	
	abstract public Vector2D intersection(Line p);
}
