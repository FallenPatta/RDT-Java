package containers;

import environment.ConstantsHelper;

public class Vector2D {
	private double x;
	private double y;

	public Vector2D(double x, double y) {
		this.setX(x==-0?0:x);
		this.setY(y==-0?0:y);
	}

	public double getX(){
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

	/**
	 * Ändert den Vektorwert
	 * @param v Vector2D
	 * @return this
	 */
	public Vector2D add(Vector2D v) {
		this.x += v.getX();
		this.y += v.getY();
		return this;
	}

	/**
	 * Ändert den Vektorwert NICHT
	 * @param v
	 * @return new Vector(x+v.x, y+v.y)
	 */
	public Vector2D sum(Vector2D v) {
		Vector2D r = new Vector2D(x, y);
		r.x += v.getX();
		r.y += v.getY();
		return r;
	}
	
	/**
	 * Ermittelt die Differenz zwischen den Vektoren.<br>
	 * a.vdiff(b) => b - a<br>
	 * <b>Wichtig:</b> Der Wert der Variable ändert sich nicht.
	 * 
	 * @param other Spitze
	 * @return neuer Vektor
	 */
	public Vector2D vdiff(Vector2D other){
		return new Vector2D(other.x-x, other.y-y);
	}
	
	/**
	 * Ermittelt die Differenz zwischen den Vektoren.<br>
	 * a.diff(b) => a - b<br>
	 * <b>Wichtig:</b> Der Wert der Variable ändert sich nicht.
	 * @param val zweiter Vektor
	 * @return neuer Vektor
	 */
	public Vector2D diff(Vector2D other){
		return new Vector2D(x-other.x, y-other.y);
	}
	
	/**
	 * Erzeugt die Division des Vektors durch den übergebenen Dividenden<br>
	 * <b>Wichtig:</b> Der Wert der Variable ändert sich nicht.
	 * @param val Dividend
	 * @return neuer, geteilter Vektor
	 */
	public Vector2D div(double val){
		return new Vector2D(x/val, y/val);
	}
	
	/**
	 * Erzeugt das Skalare Produkt des Vektors mit den übergebenen Faktor<br>
	 * <b>Wichtig:</b> Der Wert der Variable ändert sich nicht.
	 * @param val Faktor
	 * @return gestreckter Vektor
	 */
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
