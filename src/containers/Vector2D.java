package containers;

import environment.ConstantsHelper;

public class Vector2D {
	private double x;
	private double y;

	public Vector2D(double x, double y) {
		this.setX(x);
		this.setY(y);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double length() {
		return Math.sqrt(this.x * this.x + this.y * this.y);
	}

	public Vector2D add(Vector2D v) {
		this.x += v.getX();
		this.y += v.getY();
		return this;
	}

	public Vector2D sum(Vector2D v) {
		Vector2D r = new Vector2D(x, y);
		r.x += v.getX();
		r.y += v.getY();
		return r;
	}
	
	/**
	 * Bildet die Differenz zwischen dem Vektor und einem anderen Vektor. <br>
	 * Dabei fungiert das aufrufende Objekt als Fuß und das Argument als Spitze.
	 * 
	 * @param other Spitze
	 * @return Vector spitze - Vector Fuß
	 */
	public Vector2D vdiff(Vector2D other){
		return new Vector2D(other.x-x, other.y-y);
	}
	
	public Vector2D diff(Vector2D other){
		return new Vector2D(x-other.x, y-other.y);
	}
	
	public Vector2D div(double val){
		return new Vector2D(x/val, y/val);
	}
	
	public Vector2D mult(double val){
		return new Vector2D(x*val, y*val);
	}
	
	@Override
	public String toString(){
		return "("+x+", "+y+")";
	}
	
	@Override
	public boolean equals(Object other){
		if(!other.getClass().equals(Vector2D.class))
			return false;
		Vector2D o = (Vector2D) other;
		return (Math.abs(x - o.x) < ConstantsHelper.epsilon & Math.abs(y - o.y) < ConstantsHelper.epsilon);
	}

}
