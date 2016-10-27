package environment;

import java.util.ArrayList;
import java.util.List;

import containers.Vector2D;
import distances.Distance;

public class Circle implements Obstacle {

	private Vector2D center;
	private double radius;

	private double zero;
	private double full;

	private boolean direction;
	public boolean output;

	public Circle(Vector2D c, double rad) {
		this.center = c;
		this.radius = rad;
		this.direction = true;
		this.zero = 0;
		this.full = Math.PI * 2.1;
		this.direction = true;
		output = false;
	}

	public Circle(Vector2D c, double rad, double min, double max, boolean direction) {
		this.center = c;
		this.radius = rad;
		this.zero = min;
		if (this.zero < 0)
			this.zero += 2 * Math.PI;
		this.full = max;
		if (this.full < 0)
			this.full += 2 * Math.PI;
		this.direction = direction;
		output = false;
		System.out.println(zero + " - " + full);
	}

	public Vector2D getCenter() {
		return this.center;
	}

	public double getRadius() {
		return this.radius;
	}

	public double getLow() {
		return Math.max(0, Math.min(zero, full));
	}

	public double getHigh() {
		return Math.min(2 * Math.PI, Math.max(zero, full));
	}

	public boolean getDir() {
		return this.direction;
	}

	private double getD(Line p) {
		return p.getStart().diff(center).getX() * p.getEnd().diff(center).getY()
				- p.getStart().diff(center).getY() * p.getEnd().diff(center).getX();
	}

	private double getDisc(Line p) {
		double D = getD(p);
		double l = p.getDir().length();
		return (radius * radius * l * l - D * D);
	}

	private int sign(double d) {
		return d >= 0 ? 1 : -1;
	}

	public Vector2D validate(Vector2D inter) {
		double ang = Math.atan2(inter.getY(), inter.getX());
		if (ang < 0)
			ang += 2 * Math.PI;

		if (direction && (ang > zero && ang < full)) {
			return inter;
		} else if (!direction && (ang < zero | ang > full)) {
			return inter;
		}
		return null;
	}

	private List<Vector2D> intersect(Line p, Distance d) {
		List<Vector2D> inter = new ArrayList<Vector2D>();
		double D = getD(p);
		double disc = getDisc(p);
		if (disc < 0) {
			return inter;
		}
		double l = p.getDir().length();
		if (l == 0)
			return inter;

		for (int i = -1; i <= 1; i += 2) {
			double x = (D * p.getDir().getY() + i * sign(p.getDir().getY()) * p.getDir().getX() * Math.sqrt(disc))
					/ (l * l);
			double y = (-1 * D * p.getDir().getX() + i * Math.abs(p.getDir().getY()) * Math.sqrt(disc)) / (l * l);

			Vector2D tmpVec = new Vector2D(x, y);
			tmpVec = validate(tmpVec);
			if (tmpVec != null)
				inter.add(tmpVec);
		}

		return inter;
	}

	@Override
	public boolean blocks(Line p, Distance d) {
		double D = getDisc(p);
		if (D <= 0) {
			return false;
		} else {
			List<Vector2D> intersections = intersect(p, d);
			for (Vector2D inter : intersections) {
				inter.add(center);
				if ((p.getDir().getX() >= 0) == (p.getStart().vdiff(inter).getX() > 0)
						&& (p.getDir().mult(-1).getX() >= 0) == (p.getEnd().vdiff(inter).getX() > 0)
						|| Math.abs(p.getDir().getX()) <= ConstantsHelper.epsilon) {
					if ((p.getDir().getY() >= 0) == (p.getStart().vdiff(inter).getY() > 0)
							&& (p.getDir().mult(-1).getY() >= 0) == (p.getEnd().vdiff(inter).getY() > 0)
							|| Math.abs(p.getDir().getY()) <= ConstantsHelper.epsilon) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public Vector2D intersection(Line p, Distance d) {
		List<Vector2D> intersections = intersect(p, d);
		if (intersections.size() == 0) {
			if (output)
//				System.out.println("Size Zero");
			return null;
		}
		List<Vector2D> trueinter = new ArrayList<Vector2D>();
		for (Vector2D inter : intersections) {
			inter.add(center);
			if ((p.getDir().getX() >= 0) == (p.getStart().vdiff(inter).getX() > 0)
					&& (p.getDir().mult(-1).getX() >= 0) == (p.getEnd().vdiff(inter).getX() > 0)
					|| Math.abs(p.getDir().getX()) <= ConstantsHelper.epsilon) {
				if ((p.getDir().getY() >= 0) == (p.getStart().vdiff(inter).getY() > 0)
						&& (p.getDir().mult(-1).getY() >= 0) == (p.getEnd().vdiff(inter).getY() > 0)
						|| Math.abs(p.getDir().getY()) <= ConstantsHelper.epsilon) {
					trueinter.add(inter);
				}
			}
		}

//		if (output && trueinter.size() == 0)
//			System.out.println("inter NULL");
		double min = Double.MAX_VALUE;
		double tmp = 0;
		Vector2D minV = null;
		for (Vector2D inter : trueinter) {
			if ((tmp = d.dist(p.getStart(), inter)) < min) {
				min = tmp;
				minV = inter;
			}
		}
//		if (output && minV == null)
//			System.out.println("minV NULL");
		return minV;
	}

	@Override
	public String toString() {
		return this.center.toString() + " - (R = " + radius + ")";
	}

}
