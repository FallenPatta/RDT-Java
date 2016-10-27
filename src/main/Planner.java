package main;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.spi.NumberFormatProvider;
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
import environment.Circle;
import environment.ConstantsHelper;
import environment.Environment;
import environment.Line;
import environment.Obstacle;
import environment.OpenPoly;
import environment.Triangle;

public class Planner {

	static ArrayList<Vector2D> points = new ArrayList<Vector2D>();
	static ArrayList<TreeNode> tree = new ArrayList<TreeNode>();
	static double force = 5.0;
	static int imsize = 590;
	static int sc = 1;
	static int ranPts = 5000;
	static int inPts = 1;
	static int targetResolution = 1;
	static int srcResolution = 3;
	static BufferedImage img = new BufferedImage(sc * imsize, imsize, BufferedImage.TYPE_INT_RGB);
	
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
		double scale = Math.sqrt(dx * dx + dy * dy);
		dx /= scale;
		dy /= scale;
		for (double j = 0; j <= scale; j++) {
			b.setRGB((int) (Math.min(imsize * sc - 1, Math.max((double) lineL.getX() + j * dx, 0))),
					(int) (Math.min(imsize - 1, Math.max((double) lineL.getY() + j * dy, 0))), col);
		}
		b.setRGB((int) (Math.min(imsize * sc - 1, Math.max((double) lineL.getX() + scale * dx, 0))),
				(int) (Math.min(imsize - 1, Math.max((double) lineL.getY() + scale * dy, 0))), col);
	}

	public static void drawPoint(BufferedImage b, Vector2D p, int size, int col) {
		for (int x = -size; x < size; x++) {
			for (int y = -size; y < size; y++) {
				int xN = Math.min(imsize * sc - 1, Math.max(0, (int) p.getX() + x));
				int yN = Math.min(imsize - 1, Math.max(0, (int) p.getY() + y));
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
			if (root.getChildren().size() > 1 || root.getChildren().size() == 0) {
				enc.addFrame(img);
			}
			for (TreeNode n : root.getChildren()) {
				drawTree2(img, n, enc);
			}
		}
	}

	public static void drawLines(BufferedImage b, Environment env, int col) {
		for (Obstacle ob : env.getObstacles()) {
			if (ob.getClass() == Line.class) {
				Line l = (Line) ob;
				draw(b, l.getStart(), l.getEnd(), col);
			}
		}
	}

	public static void drawCircles(BufferedImage b, Environment env, int col) {
		for (Obstacle ob : env.getObstacles()) {
			if (ob.getClass() == Circle.class) {
				Circle c = (Circle) ob;
				for (double ang = 0; ang < 2 * Math.PI; ang += Math.PI / 400) {
					double x = Math.cos(ang) * c.getRadius();
					double y = Math.sin(ang) * c.getRadius();
					Vector2D d = c.validate(new Vector2D(x, y));
					if (d != null) {
						d.add(c.getCenter());
						drawPoint(b, d, 1, col);
					}
				}
			}
		}
	}

	public static void drawTriangles(BufferedImage b, Environment env, int col) {
		for (Obstacle ob : env.getObstacles()) {
			if (ob.getClass() == Triangle.class) {
				Triangle t = (Triangle) ob;
				for (Obstacle o : t.getObstacles()) {
					if (o.getClass() == Circle.class) {
						Circle c = (Circle) o;
						for (double ang = 0; ang < 2 * Math.PI; ang += Math.PI / 400) {
							double x = Math.cos(ang) * c.getRadius();
							double y = Math.sin(ang) * c.getRadius();
							Vector2D d = c.validate(new Vector2D(x, y));
							if (d != null) {
								d.add(c.getCenter());
								drawPoint(b, d, 1, col);
							}
						}
					} else if (o.getClass() == Line.class) {
						Line l = (Line) o;
						draw(b, l.getStart(), l.getEnd(), col);
					}
				}
			}
		}
	}

	public static void drawPolys(BufferedImage b, Environment env, int col) {
		for (Obstacle ob : env.getObstacles()) {
			if (ob.getClass() == OpenPoly.class) {
				OpenPoly t = (OpenPoly) ob;
				for (Obstacle o : t.getObstacles()) {
					if (o.getClass() == Circle.class) {
						Circle c = (Circle) o;
						for (double ang = 0; ang < 2 * Math.PI; ang += Math.PI / 400) {
							double x = Math.cos(ang) * c.getRadius();
							double y = Math.sin(ang) * c.getRadius();
							Vector2D d = c.validate(new Vector2D(x, y));
							if (d != null) {
								d.add(c.getCenter());
								drawPoint(b, d, 1, col);
							}
						}
					} else if (o.getClass() == Line.class) {
						Line l = (Line) o;
						draw(b, l.getStart(), l.getEnd(), col);
					}
				}
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
		DoubleImg pot = new DoubleImg(sc * imsize, imsize);

		Distance d = new Cartesian();

		Environment env = new Environment(d);

		Random random = new Random();

		// LINES
		// Vector2D a = new Vector2D(100, 350);
		// Vector2D b = new Vector2D(sc * imsize - 100, 350);
		// Line l = new Line(a, b);
		// env.add(l);

		// Vector2D a2 = new Vector2D(300, 100);
		// Vector2D b2 = new Vector2D(300, imsize - 100);
		// Line l2 = new Line(a2, b2);
		// env.add(l2);

		// Vector2D a3 = new Vector2D(sc * imsize, 0);
		// Vector2D b3 = new Vector2D(0, imsize);
		// Line l3 = new Line(a3, b3);
		// env.add(l3);

		// Vector2D a4 = new Vector2D(0, 375);
		// Vector2D b4 = new Vector2D(sc * imsize / 4 - 10, 375);
		// Line l4 = new Line(a4, b4);
		// env.add(l4);
		//
		Vector2D s1 = new Vector2D(150, 250);
		Vector2D s2 = new Vector2D(150, 150);
		Vector2D s3 = new Vector2D(250, 150);
		Vector2D s4 = new Vector2D(250, 250);

		List<Vector2D> pList = new ArrayList<Vector2D>();
		pList.add(s1);
		pList.add(s2);
		pList.add(s3);
		pList.add(s4);
		OpenPoly starter = new OpenPoly(pList);
		env.add(starter);

		Circle c0 = new Circle(new Vector2D(sc * imsize / 2, imsize / 2), imsize / 2 - 10);
		env.add(c0);

		Circle c1 = new Circle(new Vector2D(sc * imsize / 2 - 120, imsize / 2 + 50), 21.0001);
		env.add(c1);

		Circle c2 = new Circle(new Vector2D(sc * imsize / 2 + 135, imsize / 2 - 15), 100, 0.25 * Math.PI, 1.5 * Math.PI,
				false);
		env.add(c2);

		Circle c3 = new Circle(new Vector2D(sc * imsize / 2 + 120, imsize / 4), 30);
		env.add(c3);

		int pro = 0;
		for (int i = 0; i < 9; i++) {
			pro += i * 2 + 6;
			Circle cir = new Circle(new Vector2D(sc * imsize / 2 + imsize / 4, imsize / 2 - 100 + pro), 1 + i);
			env.add(cir);
		}

		Vector2D A = new Vector2D(400, 400);
		Vector2D B = new Vector2D(450, 400);
		Vector2D C = new Vector2D(425, 500);

		Triangle t = new Triangle(A, B, C);
		env.add(t);

		List<Vector2D> vecs = new ArrayList<Vector2D>();
		Vector2D cen = new Vector2D(imsize / 2, imsize / 2 + 100);
		vecs.add(cen);
		for (double ang = 0; ang < Math.PI; ang += Math.PI / 10 - ConstantsHelper.epsilon) {
			double x = Math.cos(ang) * 30;
			double y = Math.sin(ang) * 60;
			vecs.add(new Vector2D(x, y).add(cen));
		}
		vecs.add(vecs.get(0));
		OpenPoly poly = new OpenPoly(vecs);
		env.add(poly);

		// vecs = new ArrayList<Vector2D>();
		// vecs.add(cen);
		// for(int i = 0; i<12; i++){
		// double x = random.nextDouble()*sc*imsize;
		// double y = random.nextDouble()*imsize;
		// vecs.add(new Vector2D(x,y));
		// }
		// vecs.add(cen);
		// OpenPoly poly2 = new OpenPoly(vecs);
		// env.add(poly2);

		// Line l8 = new Line(A, B);
		// env.add(l8);
		// Line l9 = new Line(B, C);
		// env.add(l9);
		// Line l10 = new Line(A, C);
		// env.add(l10);
		//
		// Circle c4 = new Circle(A, 0.1);
		// env.add(c4);
		// Circle c5 = new Circle(B, 0.1);
		// env.add(c5);
		// Circle c6 = new Circle(C, 0.1);
		// env.add(c6);

		System.out.println(env);

		// TODO: Collisionserkennung - Test
		System.out.println("COLL-START");
		long startTime = System.currentTimeMillis();
		int frames = 0;
		if (false)
			try {
				File fil = new File("/home/david/Desktop/testGif.gif");
				FileOutputStream fioutStream = new FileOutputStream(fil);
				AnimatedGifEncoder testEnc = new AnimatedGifEncoder();
				testEnc.setSize(sc * imsize, imsize);
				testEnc.setDelay(1);
				testEnc.setRepeat(0);
				testEnc.start(fioutStream);

				List<Vector2D> sources = new ArrayList<Vector2D>();
				// int offset = 50;
				// for (int i = 0; i < imsize / 2; i += 5) {
				// sources.add(new Vector2D(imsize * sc / 4 + offset, i + imsize
				// / 4 + offset));
				// }
				// for (int i = 0; i < imsize * sc / 2; i += 5) {
				// sources.add(new Vector2D(imsize / 4 + i + offset, imsize / 2
				// + imsize / 4 + offset));
				// }
				// for (int i = imsize / 2; i >= 0; i -= 5) {
				// sources.add(new Vector2D(imsize / 4 + imsize / 2 + offset, i
				// + imsize / 4 + offset));
				// }
				// for (int i = imsize / 2; i >= 0; i -= 5) {
				// sources.add(new Vector2D(i + imsize / 4 + offset, imsize / 4
				// + offset));
				// }

				for (double ang = 0; ang < 2 * Math.PI; ang += Math.PI / 50) {
					sources.add(new Vector2D(200 + Math.cos(ang) * 85, 200 + Math.sin(ang) * 120));
				}
				for (double ang = 285; ang < 575; ang += srcResolution) {
					sources.add(new Vector2D(ang, 200 + (ang - 285) * 0.862068966));
				}
				for (double ang = 0; ang < 2 * Math.PI; ang += Math.PI / 50) {
					sources.add(new Vector2D(475 + Math.cos(ang) * 100, 450 + Math.sin(ang) * 90));
				}
				for (double ang = 574; ang > 285; ang -= srcResolution) {
					sources.add(new Vector2D(ang, 200 + (ang - 285) * 0.862068966));
				}

				List<Vector2D> targets = new ArrayList<Vector2D>();
				for (int i = 0; i < sc * imsize; i += targetResolution) {
					targets.add(new Vector2D(i, 0));
				}
				for (int i = 0; i < imsize; i += targetResolution) {
					targets.add(new Vector2D(sc * imsize - 1, i));
				}
				for (int i = sc * imsize - 1; i >= 0; i -= targetResolution) {
					targets.add(new Vector2D(i, imsize - 1));
				}
				for (int i = imsize - 1; i >= 0; i -= targetResolution) {
					targets.add(new Vector2D(0, i));
				}
				int num = 0;
				System.out.println("frames: " + sources.size());
				int vSp = targets.size() / 4;
				for (Vector2D src : sources) {
					num++;
					if (num % 20 == 0)
						System.out.println(num);
					List<ImgUpdater> tlst = new ArrayList<ImgUpdater>();
					for (int sp = 0; sp < 4; sp++) {
						int low = sp * vSp;
						int hi = sp == 3 ? targets.size() : (sp + 1) * vSp;
						tlst.add(new ImgUpdater(new BufferedImage(sc * imsize, imsize, BufferedImage.TYPE_INT_RGB), low, hi, targets, src, env));
					}
					for(int i = 0; i<tlst.size(); i++){
						tlst.get(i).start();
					}
					for(int i = 0; i<tlst.size(); i++){
							tlst.get(i).join();
					}
					List<BufferedImage> iml = new ArrayList<BufferedImage>();
					for(int i = 0; i<tlst.size(); i++){
						BufferedImage im = tlst.get(i).getImg();
						iml.add(im);
					}
						for(int x = 0; x<img.getWidth(); x++){
							for(int y = 0; y<img.getHeight(); y++){
								int color = 0;
								for(int i = 0; i<iml.size(); i++){
									color+=iml.get(i).getRGB(x, y);
								}
								img.setRGB(x, y, color);
							}
						}
					
						drawLines(img, env, 0x00FF00);
						drawCircles(img, env, 0x0000FF);
						drawPolys(img, env, 0x00FFFF);
						drawTriangles(img, env, 0xFFFF00);
						drawPoint(img, src, 3, 0x000000);
						testEnc.addFrame(img);
//						testEnc.finish();
						frames++;
						img = new BufferedImage(sc * imsize, imsize, BufferedImage.TYPE_INT_RGB);
						drawPoint(img, src, 3, 0xFF0000);	
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
		else if (false) {
			try {
				File fil = new File("/home/david/Desktop/testGif.gif");
				FileOutputStream fioutStream = new FileOutputStream(fil);
				AnimatedGifEncoder testEnc = new AnimatedGifEncoder();
				testEnc.setSize(sc * imsize, imsize);
				testEnc.setDelay(1);
				testEnc.setRepeat(0);
				testEnc.start(fioutStream);

				double scaleFactor = 6;

				Vector2D propagator = new Vector2D(200, 200);
				for (int i = 0; i < 50; i++) {
					Vector2D goal = new Vector2D(random.nextDouble() * sc * imsize, random.nextDouble() * imsize);
					Vector2D realgoal = env.collision(new Line(propagator, goal), 15);
					if (realgoal == null)
						realgoal = goal;
					Vector2D ctrl = propagator.vdiff(realgoal);
					Vector2D ctr = ctrl.div(ctrl.length() / scaleFactor);
					for (int n = 0; n < ctrl.length() / scaleFactor; n++) {
						img = new BufferedImage(sc * imsize, imsize, BufferedImage.TYPE_INT_RGB);
						drawLines(img, env, 0x00ff00);
						drawCircles(img, env, 0x0000ff);
						drawPoint(img, propagator.add(ctr), 3, 0x00FF00);
						testEnc.addFrame(img);
					}
				}
				// ############################
				Vector2D goal = new Vector2D(200, 200);
				Vector2D realgoal = goal;
				Vector2D ctrl = propagator.vdiff(realgoal);
				Vector2D ctr = ctrl.div(ctrl.length() / scaleFactor);
				for (int n = 0; n < ctrl.length() / scaleFactor; n++) {
					img = new BufferedImage(sc * imsize, imsize, BufferedImage.TYPE_INT_RGB);
					drawLines(img, env, 0x00ff00);
					drawCircles(img, env, 0x0000ff);
					drawPoint(img, propagator.add(ctr), 3, 0xFF0000);
					testEnc.addFrame(img);
				}
				// ############################
				testEnc.finish();
				try {
					fioutStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {

			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("COLL-END\nTime: " + (endTime - startTime) + " Millis");
		System.out.println("=> " + (double) frames / ((endTime - startTime) / 1000.0) + " FPS");
//		System.exit(0);
		img = new BufferedImage(sc * imsize, imsize, BufferedImage.TYPE_INT_RGB);

		try {
			Vector2D p = new Vector2D(200, 200);// new Vector2D(sc * imsize / 2,
												// imsize / 2);
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
			drawCircles(img, env, 0x0000ff);
			drawTriangles(img, env, 0xff0ff);
			drawPolys(img, env, 0x00ffff);
			ImageIO.write(img, "png", new File("/home/david/Desktop/prim.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// SET
		System.out.println("SET");
		long startSet = System.currentTimeMillis();
		Random rand = new Random();
		List<TreeNode> ncNodes = new ArrayList<TreeNode>();
		for (int i = 0; i < ranPts; i++) {
			Vector2D p = new Vector2D(rand.nextDouble() * sc * imsize, rand.nextDouble() * imsize);

			if (rand.nextInt(2) != 0) {
				double min = Double.MAX_VALUE;
				for (int x = 0; x < pot.getW(); x++) {
					for (int y = 0; y < pot.getH(); y++) {
						if (min > pot.get(x, y)) {
							min = pot.get(x, y);
							p = new Vector2D(Math.min(imsize * sc - 1, Math.max(0, (int) rand.nextDouble() * 20 + x)),
									Math.min(imsize - 1, Math.max(0, (int) rand.nextDouble() * 20 + y)));
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
				try{
					if ((tmp = d.dist(n.getPoint(), p)) < dMin && env.blocks(new Line(n.getPoint(), newNode.getPoint()))) {//
						dMin = tmp;
						bSource = n;
						continue;
					}
					if ((rtmp = d.dist(n, p)) < rdMin && !env.blocks(new Line(n.getPoint(), newNode.getPoint()))) {
						rdMin = rtmp;
						source = n;
					}
				}catch(IllegalArgumentException e){
					
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
				Vector2D tmpPoint = env.collision(vert, 4);
				if (tmpPoint != null) {
					newNode = new TreeNode(tmpPoint);
					p = newNode.getPoint();
				}

				Vertex v = new Vertex(source, newNode);
				List<TreeNode> list = v.subPoints(4, d);

				List<TreeNode> rmNodes = new ArrayList<TreeNode>();
				for (TreeNode n : ncNodes) {
					int minNode = -1;
					double minD = Double.MAX_VALUE;
					for (int j = 0; j < list.size(); j++) {
						try {
							TreeNode m = list.get(j);
							Line check = new Line(m.getPoint(), n.getPoint());
							if (!env.blocks(check) && d.dist(n, m.getPoint()) < minD) {
								minD = d.dist(n, m.getPoint());
								minNode = j;
							}
						} catch (IllegalArgumentException e) {

						}
					}
					if (minNode >= 0) {
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
				
//				for(TreeNode o : tree){
//					for(TreeNode n : list){
//						try{
//							Line checkNew = new Line(n.getPoint(), o.getPoint());
//							if(!env.blocks(checkNew) && o.getDistance() > d.dist(n, o.getPoint())){
//								o.setDistance(d.dist(n, o.getPoint()));
//								n.addChild(o);
//							}
//						}catch(IllegalArgumentException e){
//							
//						}
//					}
//				}
				tree.addAll(list);
//				System.out.println(tree.size());
			}
		}
		long endSet = System.currentTimeMillis();
		System.out.println("DONE");
		System.out.println((double)ranPts/((endSet-startSet)/1000.0) + " Points per Second, concluding to: " + tree.size() + " Nodes.");

		File filePath = new File("/home/david/Desktop/gif.gif");
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(filePath);
		} catch (FileNotFoundException e) {
		}

		// TODO: einfuegen
		AnimatedGifEncoder encoder = new AnimatedGifEncoder();
		encoder.setSize(sc * imsize, imsize);
		encoder.setDelay(100);
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

		System.out.println("DRAWING");
		drawLines(img, env, 0x00FF00);
		drawCircles(img, env, 0x0000FF);
		drawPolys(img, env, 0x00FFFF);
		drawTriangles(img, env, 0xFFFF00);
//		drawTree(img, tree.get(0), encoder);
		int sign = tree.size()/20;
		int numSign = tree.size()/5;
		int num = 0;
		for (TreeNode n : tree) {
			num++;
			if(num < 100 || num%sign==0){
				//System.out.print("=");
				if(encoder.delay != 10){
					encoder.setDelay(10);
				}
				encoder.addFrame(img);
				if(num >= 99){
					encoder.setDelay(100);
				}
			}
			if(num%numSign==0)
				System.out.print(" " + num/numSign*20+"% ");
			if (n.getParent() != null) {
				draw(img, n.getParent().getPoint(), n.getPoint(), 0xFF0000);
			} else {
				drawPoint(img, n.getPoint(), 4, 0x00FF00);
			}
//			if (n.getChildren().size() == 0 || n.getChildren().size() > 1) {
//			}
		}
		encoder.addFrame(img);
		encoder.setDelay(1000);
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
	
	static class ImgUpdater implements Runnable{
		
		private Thread t;
		private int l;
		private int h;
		private List<Vector2D> targets;
		private Vector2D source;
		private Environment env;
		private BufferedImage img;
		
		public ImgUpdater(BufferedImage img, int l, int h, List<Vector2D> targets, Vector2D source, Environment env){
			this.l = l;
			this.h= h;
			this.targets = targets;
			this.source = source;
			this.env = env;
			this.img = img;
		}

		@Override
		public void run() {
			for (int i = l; i < h; i++) {
				Vector2D tar;
				tar = targets.get(i);
				Line testLine = new Line(source, tar);
				Vector2D inter = env.collision(testLine, 0);
				if (inter == null) {
					draw(this.img, source, tar, 0xFFFFFF);
					// drawPoint(img, tar, 1, 0x00FF00);
				} else {
					draw(this.img, inter, tar, 0x000000);
					draw(this.img, source, inter, 0xFFFFFF);
					// drawPoint(img, inter, 1, 0x0000FF);
				}
			}
			
		}
		
		public void start(){
			this.t = new Thread(this);
			this.t.start();
		}
		
		public void join(){
			try {
				if(this.t.isAlive())
					this.t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public BufferedImage getImg(){
			return this.img;
		}
		
	}

}
