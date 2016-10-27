package environment;

import java.util.ArrayList;
import java.util.List;

import containers.Vector2D;
import distances.Distance;

public class Environment {

	private List<Obstacle> obstacles = new ArrayList<Obstacle>();
	private boolean output = false;
	private Distance distance;

	public Environment(Distance d) {
		this.distance = d;
	}

	/**
	 * Gets the closest Collision with the Environment, or null
	 * 
	 * @param l Line to check
	 * @return First Collision Point with Environment, or null if there is no collision
	 */
	public Vector2D collision(Line l, double security) {
		List<Vector2D> collisions = new ArrayList<Vector2D>();

		Vector2D tmp = null;
		for (Obstacle o : obstacles) {
			tmp = o.intersection(l, distance);
			if (tmp != null)
				collisions.add(tmp);
		}
		if (collisions.size() == 0)
			return null;
		double min = Double.MAX_VALUE;
		double temp = 0;
		Vector2D minV = collisions.get(0);
		for (Vector2D v : collisions) {
			if (output) {
				System.out.println(v);
			}
			tmp = v.vdiff(l.getStart());
			tmp = tmp.div(tmp.length());
			tmp = tmp.mult(security);
			if(tmp.length() > v.length()){
				tmp = tmp.mult(v.length()/tmp.length());
			}
			v.add(tmp);
			Vector2D check = v.vdiff(l.getStart());
			if ((temp = distance.dist(0,0,check)) < min) {
				min = temp;
				minV = v;
			}
		}

		if (output) {
			System.out.println("RESULT: " + minV);
		}
		return minV;
	}

	public boolean blocks(Line l) {
		for (Obstacle o : obstacles) {
			if (o.blocks(l, distance))
				return true;
		}
		return false;
	}

	public void add(Obstacle o) {
		obstacles.add(o);
	}

	public void setOutput(boolean val) {
		this.output = val;
	}
	
	public List<Obstacle> getObstacles(){
		return this.obstacles;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Obstacle o : obstacles) {
			sb.append(o.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

}
