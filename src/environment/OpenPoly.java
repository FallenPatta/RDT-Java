package environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import containers.Vector2D;
import distances.Distance;

public class OpenPoly implements Obstacle {
	
	List<Obstacle> obs = new ArrayList<Obstacle>();
	
	public OpenPoly(List<Vector2D> points){
		Random rnd = new Random();
		if(points == null) throw new IllegalArgumentException("List of Vector2D for OpenPoly can not be NULL");
		if(points.size() <= 1) throw new ArrayIndexOutOfBoundsException("Polygon MUST have more than one point");
		for(Vector2D p : points) obs.add(
				new Circle(p.add(new Vector2D(sign(rnd.nextInt(2)*2-1)*2*ConstantsHelper.epsilon
						, sign(rnd.nextInt(2)*2-1)*2*ConstantsHelper.epsilon))
				, 0.1));
		for(int i = 1; i<points.size(); i++){
			try{
				Line l = new Line(points.get(i-1), points.get(i));
				obs.add(l);
			}catch(IllegalArgumentException e){
				
			}
		}
	}
	
	private int sign(double d) {
		return d >= 0 ? 1 : -1;
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
