package main;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import com.gif4j.light.GifEncoder;
import com.gif4j.light.GifFrame;
import com.gif4j.light.GifImage;

import containers.DoubleImg;
import containers.TreeNode;
import containers.Vector2D;
import containers.Vertex;
import distances.Cartesian;
import distances.Distance;
import distances.Manhattan;
import environment.Environment;
import environment.Line;

public class Planner {

	static ArrayList<Vector2D> points = new ArrayList<Vector2D>();
	static ArrayList<TreeNode> tree = new ArrayList<TreeNode>();
	static double force = 5.0;
	static int imsize = 400;
	static int sc = 1;
	static int ranPts = 40;
	static int inPts = 1;
	
	public Vector2D dVec(TreeNode a, TreeNode b){
		return new Vector2D(a.getPoint().getX() - b.getPoint().getX(),a.getPoint().getY() - b.getPoint().getY());
	}

	public static void addRandom(DoubleImg pot, Distance d){
	Random rand = new Random();
//	 INIT
	for (int i = 0; i < inPts; i++) {
		Vector2D p = new Vector2D(rand.nextInt(sc * imsize),
				rand.nextInt((int) (imsize / (rand.nextDouble() * 0 + 1))));
		for (int x = 0; x < pot.getW(); x++) {
			for (int y = 0; y < pot.getH(); y++) {
				double f = force / Math.pow((Math.abs(x - p.getX()) + Math.abs(y - p.getY())), 2);
				if (f > 2 * force)
					f = 2 * force;
				pot.set(x, y, pot.get(x, y) + f);
			}
		}
		points.add(p);
		double dMin = Double.MAX_VALUE;
		double tmp = 0;
		TreeNode nearest = null;
		TreeNode newNode = new TreeNode(p);
		for (TreeNode n : tree) {
			if ((tmp = d.dist(n, p)) < dMin) {
				dMin = tmp;
				nearest = n;
			}
		}
		if (nearest == null) {
			tree.add(newNode);
		} else {
			newNode.setDistance(d.dist(nearest, p));
			nearest.addChild(newNode);
			tree.add(newNode);
		}
	}
	}

	public static void draw(BufferedImage b, Vector2D lineL, Vector2D lineN, int steps, int col) {
		double dx = ((double) (lineN.getX() - lineL.getX())) / steps;
		double dy = ((double) (lineN.getY() - lineL.getY())) / steps;
		for (int j = 0; j < steps; j++) {
			b.setRGB((int) (Math.max((double) lineL.getX() + j * dx, 0)),
					(int) (Math.max((double) lineL.getY() + j * dy, 0)), col);
		}
	}

	public static void drawTree(BufferedImage img, TreeNode root, AnimatedGifEncoder enc) {
		if (root.getChildren().isEmpty()) {
			return;
		} else {
			for (TreeNode n : root.getChildren()) {
				draw(img, root.getPoint(), n.getPoint(), 1000, 0xFF0000);
				enc.addFrame(img);
				drawTree(img, n, enc);
			}
		}
	}
	
	public static void drawTree2(BufferedImage img, TreeNode root, AnimatedGifEncoder enc) {
		if (root.getChildren().isEmpty()) {
			return;
		} else {
			for (TreeNode n : root.getChildren()) {
				draw(img, root.getPoint(), n.getPoint(), 1000, 0xFF0000);
				enc.addFrame(img);
			}
			for(TreeNode n : root.getChildren()){
				drawTree2(img, n, enc);
			}
		}
	}

	public static Vector2D linDist(Vector2D a, Vector2D b, Vector2D p) {
		double bay = (b.getY() - a.getY());
		double bax = (b.getX() - a.getX());
		double pax = a.getX() - p.getX();
		double pay = a.getY() - p.getY();

		double cosAng = (bax * pax + bay * pay) / (Math.sqrt(pax * pax + pay * pay) * Math.sqrt(bax * bax + bay * bay));
		double abL = Math.sqrt(bax * bax + bay * bay);
		double apL = Math.sqrt(pax * pax + pay * pay);

		Vector2D orth = new Vector2D((int) (a.getX() + bax / abL * cosAng * apL),
				(int) (p.getY() + bay / abL * cosAng * apL));

		return orth;
	}

	public static void main(String[] args) {
		BufferedImage img = new BufferedImage(sc * imsize, imsize, BufferedImage.TYPE_INT_RGB);
		DoubleImg pot = new DoubleImg(sc * imsize, imsize);
		Distance d = new Cartesian();
		Environment env = new Environment();
		Vector2D a = new Vector2D(0,100);
		Vector2D b = new Vector2D(sc*imsize,100);
		Line l = new Line(a, b);
		env.add(l);
		
		Vector2D a2 = new Vector2D(300,0);
		Vector2D b2 = new Vector2D(300,imsize);
		Line l2 = new Line(a2, b2);
		env.add(l2);
		
//		Vector2D a3 = new Vector2D(100,350);
//		Vector2D b3 = new Vector2D(300,351);
//		Line l3 = new Line(a3, b3);
//		env.add(l3);
		
		System.out.println(env);
		
		try{
			Vector2D p = new Vector2D(sc*imsize/2,imsize/2);
			TreeNode start = new TreeNode(p);
			points.add(p);
			tree.add(start);
			for (int x = 0; x < pot.getW(); x++) {
				for (int y = 0; y < pot.getH(); y++) {
					double f = force / d.dist(x, y, p);
					if (f > 2 * force)
						f = 2 * force;
					pot.set(x, y, pot.get(x, y) + f);
				}
			}
		}catch(Exception e){
			
		}

		double max = 0;
		for (int x = 0; x < pot.getW(); x++) {
			for (int y = 0; y < pot.getH(); y++) {
				if (pot.get(x, y) > max) {
					max = pot.get(x, y);
					System.out.println(max);
				}
			}
		}
		System.out.println(max);
		double scale = 255.0 / max;
		int val = 0;
		for (int x = 0; x < pot.getW(); x++) {
			for (int y = 0; y < pot.getH(); y++) {
				val = (int) (pot.get(x, y) * scale);
				img.setRGB(x, y, val + (val << 8) + (val << 16));
			}
		}
		try {
			draw(img,l.getStart(), l.getEnd(), 1000, 0x00FF00);
			draw(img,l2.getStart(), l2.getEnd(), 1000, 0x00FF00);
//			draw(img,l3.getStart(), l3.getEnd(), 1000, 0x00FF00);
			ImageIO.write(img, "png", new File("/home/david/Desktop/prim.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		// SET
		Random rand = new Random();
		for (int i = 0; i < ranPts; i++) {
			Vector2D p = new Vector2D(rand.nextDouble()*sc*imsize, rand.nextDouble()*imsize);
			
			for (int x = 0; x < pot.getW(); x++) {
				for (int y = 0; y < pot.getH(); y++) {
					double f = force / Math.pow(d.dist(x, y, p),2);
					if (f > 2 * force)
						f = 2 * force;
					pot.set(x, y, pot.get(x, y) + f);
				}
			}
			points.add(p);
			
			double dMin = Double.MAX_VALUE;
			double rdMin = Double.MAX_VALUE;
			double tmp = 0;
			double rtmp = 0;
			TreeNode newNode = new TreeNode(p);
			TreeNode source = null;
			TreeNode bSource = null;
			for (TreeNode n : tree) {
				boolean non = false;
				if ((tmp = d.dist(n, p)) < dMin && env.blocks(new Line(n.getPoint(), newNode.getPoint()))) {
					non=true;
					System.out.println("BLOCKING");
					dMin = tmp;
					bSource = n;
				}
				if((rtmp = d.dist(n, p)) < rdMin && !env.blocks(new Line(n.getPoint(), newNode.getPoint()))){
					if(non) System.out.println("CONFLICT");
					rdMin = rtmp;
					source = n;
					System.out.println("NOTBLOCKING");
				}
			}
			if (bSource == null) {
				tree.add(newNode);
			} else {
				if(source == null){
					System.out.println("No Free Path");
					source = bSource;
					Line vert = new Line(source.getPoint(), newNode.getPoint());
					Vector2D tmpPoint = env.collision(vert);
					if(tmpPoint != null){
						//img.setRGB((int)tmpPoint.getX(), (int)tmpPoint.getY(), 0xff00ff);
						for(int x = -3; x<=3; x++){
							for(int y = -3; y<=3; y++){
								img.setRGB((int)Math.min(sc*imsize, Math.max(0, tmpPoint.getX()+x))
										, (int)Math.min(imsize, Math.max(0, tmpPoint.getY()+y)), 0xff00ff);
							}
						}
					}
					continue;
				}
				Line vert = new Line(source.getPoint(), newNode.getPoint());
				Vector2D tmpPoint = env.collision(vert);
				if(tmpPoint != null){
					newNode = new TreeNode(tmpPoint);
					p = newNode.getPoint();
				}
				
				Vertex v = new Vertex(source, newNode);
				List<TreeNode> list = v.subPoints(5, d);
				list.add(newNode);
				for(TreeNode n : list){
					points.add(n.getPoint());
					for (int x = 0; x < pot.getW(); x++) {
						for (int y = 0; y < pot.getH(); y++) {
							double f = force / Math.pow(d.dist(x, y, n.getPoint()),2);
							if (f > 2 * force)
								f = 2 * force;
							pot.set(x, y, pot.get(x, y) + f);
						}
					}
				}
//				newNode.setDistance(dist);
//				source.addChild(newNode);
				tree.addAll(list);
			}
		}

		File filePath = new File("/home/david/Desktop/gif.gif");
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(filePath);
		} catch (FileNotFoundException e) {
		}

		AnimatedGifEncoder encoder = new AnimatedGifEncoder();
		encoder.setSize(sc * imsize, imsize);
		encoder.setDelay(50);
		encoder.setRepeat(0);
		encoder.start(outputStream);

		// DRAW
		max = 0;
		for (int x = 0; x < pot.getW(); x++) {
			for (int y = 0; y < pot.getH(); y++) {
				if (pot.get(x, y) > max) {
					max = pot.get(x, y);
					//System.out.println(max);
				}
			}
		}
		System.out.println(max);
		scale = 255.0 / max;
		val = 0;
		//TODO: entkommentieren
		for (int x = 0; x < pot.getW(); x++) {
			for (int y = 0; y < pot.getH(); y++) {
				val = (int) (pot.get(x, y) * scale);
				img.setRGB(x, y, val + (val << 8) + (val << 16));
			}
		}

		try {
			ImageIO.write(img, "png", new File("/home/david/Desktop/sec.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		//img = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		draw(img,l.getStart(), l.getEnd(), 1000, 0x00FF00);
		draw(img,l2.getStart(), l2.getEnd(), 1000, 0x00FF00);
//		draw(img,l3.getStart(), l3.getEnd(), 1000, 0x00FF00);
		drawTree2(img, tree.get(0), encoder);
//		for (int i = 1; i < tree.size(); i++) {
//			TreeNode n = tree.get(i);
//			// System.out.println(n.getPoint() + " ---> " +
//			// n.getParent().getPoint());
//			try {
//				draw(img, n.getPoint(), n.getParent().getPoint(), 1000);
//			} catch (NullPointerException e) {
//				e.printStackTrace();
//			}
//			encoder.addFrame(img);
//		}

		try {
			//draw(img,l.getStart(), l.getEnd(), 1000, 0x00FF00);
			ImageIO.write(img, "png", new File("/home/david/Desktop/ter.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		encoder.finish();
		// File gifOut = new File("/home/david/Desktop/gif.gif");
		try {
			// GifEncoder.encode(gif, gifOut);
			// File filePath = new File("/home/david/Desktop/gif2.gif");
			// FileOutputStream outputStream;
			// try {
			// outputStream = new FileOutputStream(filePath);
			// outputStream.write(bos.toByteArray());
			// } catch (FileNotFoundException e) {
			// } catch (IOException e) {
			// }
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
