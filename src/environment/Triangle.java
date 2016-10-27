package environment;

import java.util.ArrayList;
import java.util.List;

import containers.Vector2D;
import distances.Distance;

public class Triangle implements Obstacle {

	List<Obstacle> obs = new ArrayList<Obstacle>();
	
	public Triangle(Vector2D a, Vector2D b, Vector2D c){
		obs.add(new Line(a,b));
		obs.add(new Line(a,c));
		obs.add(new Line(b,c));
		obs.add(new Circle(a,0.1));
		obs.add(new Circle(b,0.1));
		obs.add(new Circle(c,0.1));
	}
	
	public List<Obstacle> getObstacles(){
		return this.obs;
	}

	@Override
	public boolean blocks(Line p, Distance d) {
		for(Obstacle o : obs){
			if(o.blocks(p, d)) return true;
		}
		return false;
	}

	@Override
	public Vector2D intersection(Line p, Distance d) {
		List<Vector2D> inters = new ArrayList<Vector2D>();
		for(Obstacle o : obs){
			Vector2D i = o.intersection(p, d);
			if( i!= null) inters.add(i);
		}
		
		if(inters.size() == 0) return null;
		
		double min = Double.MAX_VALUE;
		double temp = 0;
		Vector2D minV = inters.get(0);
		for (Vector2D v : inters) {
			Vector2D check = v.vdiff(p.getStart());
			if ((temp = d.dist(0,0,check)) < min) {
				min = temp;
				minV = v;
			}
		}
		
		return minV;
	}

}
