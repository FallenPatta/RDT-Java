package environment;

import containers.Vector2D;

public class Line implements Obstacle {
	
	private Vector2D a;
	private Vector2D b;
	
	private Vector2D dir;
	private double length;
	private double m;
	private double n;
	
	public Line(Vector2D start, Vector2D end){
		this.a = start;
		this.b = end;
		this.dir = start.vdiff(end);
		this.m = dir.getY()/dir.getX();
		this.n = a.getY()-m*a.getX();
		this.length = dir.length();
	}
	
	/**
	 * Gibt Schnittpunkt der übergeordneten Geraden zurück
	 * @param other
	 * @return Schnittpunkt
	 */
	public Vector2D intersect(Line other){
		if(m == other.m){
			if(n == other.n) return other.b;
			else return null;
		}
		if(Double.isInfinite(m)){
			return new Vector2D(a.getX(), other.m*a.getX()+other.n);
		} else if(Double.isInfinite(other.m)){
			return new Vector2D(other.a.getX(), m*other.a.getX()+n);
		}
		double x = (n-other.n)/(other.m-m);
		double y = m*x+n;
		
		return new Vector2D(x,y);
	}

	@Override
	public boolean blocks(Line l) {
		Vector2D inter = intersect(l);
		if(inter != null){
			Vector2D d1 = a.vdiff(inter);
			Vector2D d2 = l.a.vdiff(inter);
			if(((dir.getX() >= 0) == (d1.getX() >= 0)) && ((l.dir.getX() >= 0) == (d2.getX() >= 0))){
				return true;
			}
		}
		return false;
	}

	@Override
	public Vector2D intersection(Line l) {
		Vector2D inter = intersect(l);
		if(inter != null){
			Vector2D d1 = a.vdiff(inter);
			Vector2D d2 = l.a.vdiff(inter);
			if(((dir.getX() >= 0) == (d1.getX() >= 0)) && ((l.dir.getX() >= 0) == (d2.getX() >= 0))){
				return inter;
			}
		}	
		return null;
	}
	
	public Vector2D getStart(){
		return a;
	}
	
	public Vector2D getEnd(){
		return b;
	}
	
	public Vector2D getDir(){
		return dir;
	}
	
	@Override
	public String toString(){
		return this.a + " -> " + this.b;
	}

}
