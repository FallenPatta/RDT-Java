package containers;

public class DoubleImg {
	private double mat[][];
	int w;
	int h;

	public DoubleImg(int w, int h) {
		setMat(new double[w][h]);
		this.w = w;
		this.h = h;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				set(x, y, 0);
			}
		}
	}

	public double get(int x, int y) {
		return this.mat[x][y];
	}

	public void set(int x, int y, double d) {
		this.mat[x][y] = d;
	}

	public void setMat(double mat[][]) {
		this.mat = mat;
	}

	public int getW() {
		return w;
	}

	public int getH() {
		return h;
	}
}