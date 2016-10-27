package environment;

import containers.DoubleImg;
import containers.Vector2D;
import distances.Distance;

public class Line implements Obstacle {

	private Vector2D a;
	private Vector2D b;

	private Vector2D dir;
	private Vector2D ortho;
	private double length;
	private double m;
	private double n;

	private DoubleImg projMat;

	public Line(Vector2D start, Vector2D end) throws IllegalArgumentException {
		if (start.equals(end))
			throw new IllegalArgumentException("Start- and Endpoint of a Line cannot be identical");
		this.a = start;
		this.b = end;
		this.dir = start.vdiff(end);
		ortho = new Vector2D(-dir.getY(), dir.getX());
		this.m = Math.abs(dir.getX()) < ConstantsHelper.epsilon
				? (dir.getY() > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY) : dir.getY() / dir.getX();
		this.n = Double.isInfinite(m) ? 0 : a.getY() - m * a.getX();
		this.length = dir.length();

		projMat = new DoubleImg(2, 2);
	}

	/**
	 * Gibt Schnittpunkt der übergeordneten Geraden zurück
	 * 
	 * @param other
	 * @return Schnittpunkt
	 */
	public Vector2D intersect(Line other) {
		if (m == other.m) {
			return null;
		}
		if (Double.isInfinite(m)) {
			return new Vector2D(a.getX(), other.m * a.getX() + other.n);
		} else if (Double.isInfinite(other.m)) {
			return new Vector2D(other.a.getX(), m * other.a.getX() + n);
		}
		double x = (other.n - n) / (m - other.m);
		double y = m * x + n;

		return new Vector2D(x, y);
	}
	
	private int sign(double d) {
		return d >= 0 ? 1 : -1;
	}

	@Override
	public boolean blocks(Line l, Distance d) {
		Vector2D inter = intersect(l);
		if (inter != null) {
			if (Math.abs(d.dist(inter, l.a)) < ConstantsHelper.epsilon)
				return true;
			if ((dir.getX() >= 0) == (a.vdiff(inter).getX() >= 0)
					&& (dir.mult(-1).getX() >= 0) == (b.vdiff(inter).getX() >= 0)) {
				if ((l.dir.getX() >= 0) == (l.a.vdiff(inter).getX() >= 0)
						&& (l.dir.mult(-1).getX() >= 0) == (l.b.vdiff(inter).getX() >= 0)) {
					if ((dir.getY() >= 0) == (a.vdiff(inter).getY() >= 0)
							&& (dir.mult(-1).getY() >= 0) == (b.vdiff(inter).getY() >= 0)) {
						if ((l.dir.getY() >= 0) == (l.a.vdiff(inter).getY() >= 0)
								&& (l.dir.mult(-1).getY() >= 0) == (l.b.vdiff(inter).getY() >= 0)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public Vector2D intersection(Line l, Distance d) {
		Vector2D inter = intersect(l);
		if (inter != null) {
			if ((dir.getX() >= 0) == (a.vdiff(inter).getX() >= 0)
					&& (dir.mult(-1).getX() >= 0) == (b.vdiff(inter).getX() >= 0)) {
				if ((l.dir.getX() >= 0) == (l.a.vdiff(inter).getX() >= 0)
						&& (l.dir.mult(-1).getX() >= 0) == (l.b.vdiff(inter).getX() >= 0)) {
					if ((dir.getY() >= 0) == (a.vdiff(inter).getY() >= 0)
							&& (dir.mult(-1).getY() >= 0) == (b.vdiff(inter).getY() >= 0)) {
						if ((l.dir.getY() >= 0) == (l.a.vdiff(inter).getY() >= 0)
								&& (l.dir.mult(-1).getY() >= 0) == (l.b.vdiff(inter).getY() >= 0)) {
							return inter;
						}
					}
				}
			}
		}
		return null;
	}

	// @Override
	// public Vector2D intersection(Line l, Distance d) {
	// Vector2D inter = intersect(l);
	// if (inter != null) {
	// if ((dir.getX() >= 0) == (a.vdiff(inter).getX() >= 0)
	// && (dir.mult(-1).getX() >= 0) == (b.vdiff(inter).getX() >= 0)
	// || Math.abs(dir.getX()) <= ConstantsHelper.epsilon) {
	// if ((l.dir.getX() >= 0) == (l.a.vdiff(inter).getX() >= 0)
	// && (l.dir.mult(-1).getX() >= 0) == (l.b.vdiff(inter).getX() >= 0)
	// || Math.abs(l.dir.getX()) <= ConstantsHelper.epsilon) {
	// if ((dir.getY() >= 0) == (a.vdiff(inter).getY() >= 0)
	// && (dir.mult(-1).getY() >= 0) == (b.vdiff(inter).getY() >= 0)
	// || Math.abs(dir.getY()) <= ConstantsHelper.epsilon) {
	// if ((l.dir.getY() >= 0) == (l.a.vdiff(inter).getY() >= 0)
	// && (l.dir.mult(-1).getY() >= 0) == (l.b.vdiff(inter).getY() >= 0)
	// || Math.abs(l.dir.getX()) <= ConstantsHelper.epsilon) {
	// return inter;
	// }
	// }
	// }
	// }
	// }
	// return null;
	// }

	public Vector2D getStart() {
		return a;
	}

	public Vector2D getEnd() {
		return b;
	}

	public Vector2D getDir() {
		return dir;
	}

	@Override
	public String toString() {
		return this.a + " -> " + this.b + " - " + this.m + ", " + this.n;
	}

}
