package environment;

import containers.Vector2D;
import distances.Distance;

public interface Obstacle {
	/**
	 * Returns true if point p is inside or behind the obstacle
	 * 
	 * @param p The Point
	 * @return true if blocked, false if not blocked
	 */
	abstract public boolean blocks(Line p, Distance d);
	
	/**
	 * Returns the closest intersection to the Start of Line p
	 * @param p Line to check
	 * @param d	Metric to use
	 * @return Intersecton, or null if no intersection was found
	 */
	abstract public Vector2D intersection(Line p, Distance d);
}
