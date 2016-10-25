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
import environment.Obstacle;

public class Planner {

	static ArrayList<Vector2D> points = new ArrayList<Vector2D>();
	static ArrayList<TreeNode> tree = new ArrayList<TreeNode>();
	static double force = 5.0;
	static int imsize = 590;
	static int sc = 1;
	static int ranPts = 1024;
	static int inPts = 1;

	public Vector2D dVec(TreeNode a, TreeNode b) {
		return new Vector2D(a.getPoint().getX() - b.getPoint().getX(), a.getPoint().getY() - b.getPoint().getY());
	}

	public static void addRandom(DoubleImg pot, Distance d) {
		Random rand = new Random();
		// INIT
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

	public static void draw(BufferedImage b, Vector2D lineL, Vector2D lineN, int col) {
		double dx = ((double) (lineN.getX() - lineL.getX()));
		double dy = ((double) (lineN.getY() - lineL.getY()));
		double scale = Math.sqrt(dx*dx + dy*dy);
		dx/=scale;
		dy/=scale;
		for (double j = 0; j <= scale; j++) {
			b.setRGB((int) (Math.min(imsize*sc-1,Math.max((double) lineL.getX() + j * dx, 0))),
					(int) (Math.min(imsize-1, Math.max((double) lineL.getY() + j * dy, 0))), col);
		}
		b.setRGB((int) (Math.min(imsize*sc-1,Math.max((double) lineL.getX() + scale * dx, 0))),
				(int) (Math.min(imsize-1, Math.max((double) lineL.getY() + scale * dy, 0))), col);
	}
	
	public static void drawPoint(BufferedImage b, Vector2D p, int size, int col){
		for(int x = -size; x<size; x++){
			for(int y = -size; y<size; y++){
				int xN = Math.min(imsize*sc-1, Math.max(0, (int)p.getX() + x));
				int yN = Math.min(imsize-1, Math.max(0, (int)p.getY() + y));
				b.setRGB(xN, yN, col);
			}
		}
	}

	public static void drawTree(BufferedImage img, TreeNode root, AnimatedGifEncoder enc) {
		if (root.getChildren().isEmpty()) {
			return;
		} else {
			for (TreeNode n : root.getChildren()) {
				draw(img, root.getPoint(), n.getPoint(), 0xFF0000);
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
				draw(img, root.getPoint(), n.getPoint(), 0xFF0000);
			}
			if(root.getChildren().size() > 1){
				enc.addFrame(img);
			}
			for (TreeNode n : root.getChildren()) {
				drawTree2(img, n, enc);
			}
		}
	}
	
	public static void drawLines(BufferedImage b, Environment env, int col){
		for(Obstacle ob : env.getObstacles()){
			if(ob.getClass() == Line.class){
				Line l = (Line) ob;
				draw(b, l.getStart(), l.getEnd(), col);
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

		Distance d = new Manhattan();

		Environment env = new Environment(d);

		//LINES
//		Vector2D a = new Vector2D(100, 350);
//		Vector2D b = new Vector2D(sc * imsize-100, 350);
//		Line l = new Line(a, b);
//		env.add(l);

		Vector2D a2 = new Vector2D(300, 100);
		Vector2D b2 = new Vector2D(300, imsize - 100);
		Line l2 = new Line(a2, b2);
		env.add(l2);
		
		Vector2D a3 = new Vector2D(sc * imsize, 375);
		Vector2D b3 = new Vector2D(sc * imsize/4+70, 375);
		Line l3 = new Line(a3, b3);
		env.add(l3);
		
		Vector2D a4 = new Vector2D(0, 375);
		Vector2D b4 = new Vector2D(sc * imsize/4-10, 375);
		Line l4 = new Line(a4, b4);
		env.add(l4);
		
		Vector2D a5 = new Vector2D(150, 150);
		Vector2D b5 = new Vector2D(250, 150);
		Line l5 = new Line(a5, b5);
		env.add(l5);
		
		Vector2D a6 = new Vector2D(250, 150);
		Vector2D b6 = new Vector2D(250, 250);
		Line l6 = new Line(a6, b6);
		env.add(l6);
		
		Vector2D a7 = new Vector2D(150, 150);
		Vector2D b7 = new Vector2D(150, 250);
		Line l7 = new Line(a7, b7);
		env.add(l7);
		
		Vector2D a8 = new Vector2D(400, 400);
		Vector2D b8 = new Vector2D(550, 400);
		Line l8 = new Line(a8, b8);
		env.add(l8);
		
		Vector2D a9 = new Vector2D(550, 400);
		Vector2D b9 = new Vector2D(475, 500);
		Line l9 = new Line(a9, b9);
		env.add(l9);
		
		Vector2D a10 = new Vector2D(475, 500);
		Vector2D b10 = new Vector2D(400, 400);
		Line l10 = new Line(a10, b10);
		env.add(l10);
		
		System.out.println(env);

		// TODO: Collisionserkennung - Test
		System.out.println("COLL-START");
		long startTime = System.currentTimeMillis();
		int frames = 0;
		if(true)try {
			File fil = new File("/home/david/Desktop/testGif.gif");
			FileOutputStream fioutStream = new FileOutputStream(fil);
			AnimatedGifEncoder testEnc = new AnimatedGifEncoder();
			testEnc.setSize(sc * imsize, imsize);
			testEnc.setDelay(1);
			testEnc.setRepeat(0);
			testEnc.start(fioutStream);

			List<Vector2D> sources = new ArrayList<Vector2D>();
//			int offset = 50;
//			for (int i = 0; i < imsize/2; i+=5) {
//				sources.add(new Vector2D(imsize*sc/4+offset, i+imsize/4+offset));
//			}
//			for (int i = 0; i < imsize*sc/2; i+=5) {
//				sources.add(new Vector2D(imsize/4+i+offset, imsize/2+imsize/4+offset));
//			}
//			for (int i = imsize/2; i >= 0; i-=5) {
//				sources.add(new Vector2D(imsize/4+imsize/2+offset, i+imsize/4+offset));
//			}
//			for (int i = imsize/2; i >= 0; i-=5) {
//				sources.add(new Vector2D(i + imsize/4+offset, imsize/4+offset));
//			}
			
			for(double ang = 0; ang<2*Math.PI; ang+=Math.PI/50){
				sources.add(new Vector2D(200 + Math.cos(ang)*85, 200 + Math.sin(ang)*100));
			}
			for(double ang = 285; ang<575; ang+=3){
				sources.add(new Vector2D(ang, 200 + (ang-285)*0.862068966));
			}
			for(double ang = 0; ang<2*Math.PI; ang+=Math.PI/50){
				sources.add(new Vector2D(475 + Math.cos(ang)*100, 450 + Math.sin(ang)*90));
			}
			for(double ang = 574; ang>285; ang-=3){
				sources.add(new Vector2D(ang, 200 + (ang-285)*0.862068966));
			}
			
			List<Vector2D> targets = new ArrayList<Vector2D>();
			for(int i = 0; i<sc*imsize; i+=1){
				targets.add(new Vector2D(i,0));
			}
			for(int i = 0; i<imsize; i+=1){
				targets.add(new Vector2D(sc*imsize-1,i));
			}
			for(int i = sc*imsize-1; i>=0; i-=1){
				targets.add(new Vector2D(i,imsize-1));
			}
			for(int i = imsize-1; i>=0; i-=1){
				targets.add(new Vector2D(0,i));
			}
			
			int num = 0;
			System.out.println("frames: " + sources.size());
			for (Vector2D src : sources) {
				num++;
				if(num%20 == 0) System.out.println(num);
				for (Vector2D tar : targets) {
					Line testLine = new Line(src, tar);
					Vector2D inter = env.collision(testLine);
					if (inter == null) {
						//draw(img, src, tar, 0x00FF00);
					} else {
						//draw(img, inter, tar, 0xFF0000);
						//draw(img, src, inter, 0x000055);
						drawPoint(img, inter, 2, 0x0000FF);
					}
				}
				drawLines(img, env, 0x00ff00);
				drawPoint(img, src, 3, 0xFF0000);
				testEnc.addFrame(img);
				frames++;
				img = new BufferedImage(sc * imsize, imsize, BufferedImage.TYPE_INT_RGB);
			}
			testEnc.finish();
			try {
				fioutStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("COLL-END\nTime: " + (endTime-startTime) + " Millis");
		System.out.println("=> " + (double)frames/((endTime-startTime)/1000.0) + " FPS");
		System.exit(0);
		img = new BufferedImage(sc * imsize, imsize, BufferedImage.TYPE_INT_RGB);

		try {
			Vector2D p = new Vector2D(200,200);//new Vector2D(sc * imsize / 2, imsize / 2);
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
		} catch (Exception e) {

		}

		double max = 0;
		for (int x = 0; x < pot.getW(); x++) {
			for (int y = 0; y < pot.getH(); y++) {
				if (pot.get(x, y) > max) {
					max = pot.get(x, y);
					// System.out.println(max);
				}
			}
		}
		// System.out.println(max);
		double scale = 255.0 / max;
		int val = 0;
		for (int x = 0; x < pot.getW(); x++) {
			for (int y = 0; y < pot.getH(); y++) {
				val = (int) (pot.get(x, y) * scale);
				img.setRGB(x, y, val + (val << 8) + (val << 16));
			}
		}
		try {
			drawLines(img, env, 0x00ff00);
			ImageIO.write(img, "png", new File("/home/david/Desktop/prim.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// SET
		System.out.println("SET");
		Random rand = new Random();
		List<TreeNode> ncNodes = new ArrayList<TreeNode>();
		for (int i = 0; i < ranPts; i++) {
			Vector2D p = new Vector2D(rand.nextDouble() * sc * imsize, rand.nextDouble() * imsize);
			
			if(rand.nextInt(2) != 0){
				double min = Double.MAX_VALUE;
				for (int x = 0; x < pot.getW(); x++) {
					for (int y = 0; y < pot.getH(); y++) {
						if(min > pot.get(x, y)){
							min = pot.get(x, y);
							p = new Vector2D(Math.min(imsize*sc-1, Math.max(0, (int)rand.nextDouble()*20 + x))
									, Math.min(imsize-1, Math.max(0, (int)rand.nextDouble()*20 + y)));
						}
					}
				}
			}

			for (int x = 0; x < pot.getW(); x++) {
				for (int y = 0; y < pot.getH(); y++) {
					double f = force / Math.pow(d.dist(x, y, p), 2);
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
				if ((tmp = d.dist(n, p)) < dMin && env.blocks(new Line(n.getPoint(), newNode.getPoint()))) {// 
					dMin = tmp;
					bSource = n;
					continue;
				}
				if ((rtmp = d.dist(n, p)) < rdMin && !env.blocks(new Line(n.getPoint(), newNode.getPoint()))) {
					rdMin = rtmp;
					source = n;
				}
			}
			if (bSource == null && source == null) {
				tree.add(newNode);
			} else {
				if (source == null) {
					// System.out.println("No Free Path");
					source = bSource;
					ncNodes.add(new TreeNode(p));
				}
				Line vert = new Line(source.getPoint(), newNode.getPoint());
				Vector2D tmpPoint = env.collision(vert);
				if (tmpPoint != null) {
					newNode = new TreeNode(tmpPoint);
					p = newNode.getPoint();
				}

				Vertex v = new Vertex(source, newNode);
				List<TreeNode> list = v.subPoints(12, d);

				List<TreeNode> rmNodes = new ArrayList<TreeNode>();
				for(TreeNode n : ncNodes){
					int minNode = -1;
					double minD = Double.MAX_VALUE;
					for(int j =0; j<list.size(); j++){
						try{
							TreeNode m = list.get(j);
							Line check = new Line(m.getPoint(), n.getPoint());
							if(!env.blocks(check) && d.dist(n, m.getPoint()) < minD){
								minD = d.dist(n, m.getPoint());
								minNode = j;
							}
						}catch(IllegalArgumentException e){
							
						}
					}
					if(minNode >= 0){
						n.setDistance(d.dist(list.get(minNode), n.getPoint()));
						list.get(minNode).addChild(n);
						list.add(n);
						rmNodes.add(n);
					}
				}
				ncNodes.removeAll(rmNodes);
				
				points.add(newNode.getPoint());
				for (int x = 0; x < pot.getW(); x++) {
					for (int y = 0; y < pot.getH(); y++) {
						double f = force / Math.pow(d.dist(x, y, newNode.getPoint()), 2);
						if (f > 2 * force)
							f = 2 * force;
						pot.set(x, y, pot.get(x, y) + f);
					}
				}

				tree.addAll(list);
			}
		}
		System.out.println("DONE");

		File filePath = new File("/home/david/Desktop/gif.gif");
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(filePath);
		} catch (FileNotFoundException e) {
		}

		//TODO: einfuegen
		AnimatedGifEncoder encoder = new AnimatedGifEncoder();
		encoder.setSize(sc * imsize, imsize);
		encoder.setDelay(10);
		encoder.setRepeat(0);
		encoder.start(outputStream);

		// DRAW
		max = 0;
		for (int x = 0; x < pot.getW(); x++) {
			for (int y = 0; y < pot.getH(); y++) {
				if (pot.get(x, y) > max) {
					max = pot.get(x, y);
					// System.out.println(max);
				}
			}
		}
		System.out.println(max);
		scale = 255.0 / max;
		val = 0;
		// TODO: entkommentieren
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

		drawLines(img, env, 0x00ff00);
		System.out.println("DRAWING");
		System.out.println(tree.size() + " Nodes");
		//drawTree2(img, tree.get(0), encoder);
		int mod = tree.size()/200;
		if(mod == 0) mod = 1;
		int num = 0;
		for(TreeNode n : tree){
			if(n.getParent() != null){
				draw(img, n.getParent().getPoint(), n.getPoint(), 0xFF0000);
			}
			else{
				drawPoint(img, n.getPoint(), 4, 0x00FF00);
			}
			if(n.getChildren().size() == 0 || n.getChildren().size() > 1){
				encoder.addFrame(img);
			}
			num++;
		}
		encoder.addFrame(img);
		System.out.println("DONE");

		try {
			ImageIO.write(img, "png", new File("/home/david/Desktop/ter.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		encoder.finish();
		try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
