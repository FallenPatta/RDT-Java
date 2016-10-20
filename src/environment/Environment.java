package environment;

import java.util.ArrayList;
import java.util.List;

import containers.Vector2D;

public class Environment {
	
	private List<Obstacle> obstacles = new ArrayList<Obstacle>();
	
	public Environment(){
		
	}
	
	public Vector2D collision(Line l){
		List<Vector2D> collisions = new ArrayList<Vector2D>();
		
		Vector2D tmp = null;
		for(Obstacle o : obstacles){
			tmp = o.intersection(l);
			if(tmp != null) collisions.add(tmp);
		}
		if(collisions.size() != 0) return null;
		double min = Double.MAX_VALUE;
		double temp = 0;
		Vector2D minV = null;
		for(Vector2D v : collisions){
			if((temp=v.length()) < min){
				min = temp;
				minV = v;
			}
		}
		
		return minV==null? null : minV;
	}
	
	public boolean blocks(Line l){
		for(Obstacle o : obstacles){
			if(o.blocks(l)) return true;
		}
		return false;
	}
	
	public void add(Obstacle o){
		obstacles.add(o);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(Obstacle o : obstacles){
			sb.append(o.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

}
