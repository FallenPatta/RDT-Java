package containers;

import java.util.ArrayList;

public class TreeNode {
	private TreeNode parent;
	private Vector2D node;
	private ArrayList<TreeNode> children = new ArrayList<TreeNode>();
	private double distance;

	public TreeNode(Vector2D n) {
		this.node = n;
		this.setDistance(0);
	}
	
	public TreeNode(Vector2D n, double dist) {
		this.node = n;
		this.setDistance(dist);
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public Vector2D getPoint() {
		return this.node;
	}

	public TreeNode getParent() {
		return this.parent;
	}

	public void setParent(TreeNode p) {
		this.parent = p;
	}

	public void addChild(TreeNode c) {
		c.setParent(this);
		this.children.add(c);
	}
	
	public void removeChild(TreeNode c){
		if(this.children.contains(c)){
			this.children.remove(c);
		}
	}

	public ArrayList<TreeNode> getChildren() {
		return this.children;
	}

	public Vector2D inBetween(Vector2D p) {

		Vector2D v1 = new Vector2D(getParent().getPoint().getX() - getPoint().getX(),
				getParent().getPoint().getY() - getPoint().getY());
		Vector2D v2 = new Vector2D(p.getX() - getPoint().getX(), p.getY() - getPoint().getY());

		double cosAng = (v1.getX() * v2.getX() + v1.getY() * v2.getY()) / (v1.length() * v2.length());
		double inlen = (cosAng * v2.length()) / v1.length();
		if (inlen < 0 || inlen > 1) {
			return null;
		}

		return new Vector2D(v1.getX() * inlen, v1.getY() * inlen);
	}

	public TreeNode addBetween(Vector2D v) {

		TreeNode b = new TreeNode(getPoint().add(v));

		getParent().getChildren().remove(this);
		b.setParent(getParent());
		getParent().addChild(b);
		b.addChild(this);

		return b;
	}

}