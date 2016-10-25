package containers;

import java.util.ArrayList;
import java.util.List;

import distances.Distance;

public class Vertex {
	private TreeNode start;
	private TreeNode end;
	private Vector2D direction;
	
	public Vertex(TreeNode a, TreeNode b){
		this.start = a;
		this.end = b;
		this.direction = a.getPoint().vdiff(b.getPoint());
	}
	
	/**
	 * compiles a List of Nodes including the End Node of the Vertex
	 * @param num Number of Points
	 * @param d A distance Metric
	 * @return The compiled List<TreeNode>
	 */
	public List<TreeNode> subPoints(int num, Distance d){
		List<TreeNode> list = new ArrayList<TreeNode>();
		Vector2D splitter = direction.div(num+1);
		if(num > 0){
		for(int i = 0; i < num; i++){
			list.add(new TreeNode(start.getPoint().sum(splitter.mult(i+1))));
			list.get(i).setDistance(d.dist(start, list.get(i).getPoint()));
			if(i==0)
				start.addChild(list.get(i));
			else
				list.get(i-1).addChild(list.get(i));;
		}
		
		list.get(num-1).addChild(end);
		}
		if(list.size() == 0){
			start.addChild(end);
		}
		list.add(end);
		end.setDistance(d.dist(start, end.getPoint()));
		
		return list;
	}
	
}
