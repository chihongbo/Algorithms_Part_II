import edu.princeton.cs.algs4.Picture;
import java.awt.Color;

public class SeamCarver {
    // create a seam carver object based on the given picture

    private Picture picture;
    //private int width;
    //private int height;
    // private double[][] energy;

    public SeamCarver(Picture picture) {
        if (picture == null) throw new IllegalArgumentException("input picture is null");
        this.picture = new Picture(picture);

    }

    // current picture
    public Picture picture() {
        return new Picture(this.picture);
    }

    // width of current picture
    public int width() {
        return this.picture.width();
    }

    // height of current picture
    public int height() {
        return this.picture.height();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x > this.width() - 1) throw new IllegalArgumentException("x is outside it prescribed range");
        if (y < 0 || y > this.height() - 1) throw new IllegalArgumentException("y is outside it prescribed range");
        if (x == 0 || x == this.width() - 1) return 1000;
        if (y == 0 || y == this.height() - 1) return 1000;
        Color c1y = picture.get(x - 1, y);
        Color c2y = picture.get(x + 1, y);
        Color cx1 = picture.get(x, y - 1);
        Color cx2 = picture.get(x, y + 1);
        double dx2 = Math.pow(c2y.getRed() - c1y.getRed(), 2) + Math.pow(c2y.getGreen() - c1y.getGreen(), 2) + Math.pow(c2y.getBlue() - c1y.getBlue(), 2);
        double dy2 = Math.pow(cx2.getRed() - cx1.getRed(), 2) + Math.pow(cx2.getGreen() - cx1.getGreen(), 2) + Math.pow(cx2.getBlue() - cx1.getBlue(), 2);
        return Math.sqrt(dx2 + dy2);
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        return findSeam("Horizontal");
    }

    private int[] findSeam(String Dir) {
        int width = 0, height = 0;
        if (Dir.equals("Vertical")) {
            width = this.width();
            height = this.height();
        }
        if (Dir.equals("Horizontal")) {
            width = this.height();
            height = this.width();
        }
        int[] seam = new int[height];
        int[][] edgeTo = new int[width][height + 1];
        double[][] distTo = new double[width][height + 1];
        double[][] energy = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (Dir.equals("Vertical")) energy[i][j] = this.energy(i, j);
                if (Dir.equals("Horizontal")) energy[i][j] = this.energy(j, i);
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height + 1; j++) {
                distTo[i][j] = Double.POSITIVE_INFINITY;
                if (j == 0) distTo[i][j] = energy[i][0];
            }
        }

        for (int j = 0; j < height; j++) { //j for rows
            for (int i = 0; i < width; i++) {  // i for column
                if (j < height - 1) {

                    // for row j+1 and column i-1
                    if (i - 1 >= 0) {
                        //index = (j + 1) * width + i - 1; // index for column i-1, and row j+1
                        if (distTo[i - 1][j + 1] > distTo[i][j] + energy[i - 1][j + 1]) {
                            distTo[i - 1][j + 1] = distTo[i][j] + energy[i - 1][j + 1];
                            edgeTo[i - 1][j + 1] = i;
                        }

                    }

                    // for j+1 and column i
                    if (distTo[i][j + 1] > distTo[i][j] + energy[i][j + 1]) {
                        distTo[i][j + 1] = distTo[i][j] + energy[i][j + 1];
                        edgeTo[i][j + 1] = i;
                    }

                    // for row j+1 and column i+1
                    if (i + 1 < width) {
                        //index = (j + 1) * width + i + 1; // index for column i+1, and row j+1
                        if (distTo[i + 1][j + 1] > distTo[i][j] + energy[i + 1][j + 1]) {
                            distTo[i + 1][j + 1] = distTo[i][j] + energy[i + 1][j + 1];
                            edgeTo[i + 1][j + 1] = i;
                        }

                    }
                } else {
                    //index = height * width; // index for the final virtual vertex with index of height*width with only 1 column
                    if (distTo[0][j + 1] > distTo[i][j]) {
                        distTo[0][j + 1] = distTo[i][j];
                        edgeTo[0][j + 1] = i;
                    }
                }
            }
        }

        int index = 0;// the initial column # for the virtual node in height row and 0 column
        for (int j = height - 1; j >= 0; j--) {
            seam[j] = edgeTo[index][j + 1];
            index = edgeTo[index][j + 1];
        }
        return seam;

    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        return findSeam("Vertical");

    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {

        this.picture = removeSeam("Horizontal", seam);
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {

        this.picture = removeSeam("Vertical", seam);
    }

    private Picture removeSeam(String Dir, int[] seam) {
        int width = this.width();
        int height = this.height();
        Picture pic = null;
        if (seam == null) throw new IllegalArgumentException("seam is null");
        for (int i = 0; i < seam.length; i++) {
            if (Dir.equals("Vertical")) {
                if (seam[i] < 0 || seam[i] > width - 1)
                    throw new IllegalArgumentException("illegal seam value for vertical seam carving");
            }
            if (Dir.equals("Horizontal")) {
                if (seam[i] < 0 || seam[i] > height - 1)
                    throw new IllegalArgumentException("illegal seam value for horizontal seam carving");
            }

            if (i > 0) {
                if (Math.abs(seam[i] - seam[i - 1]) > 1)
                    throw new IllegalArgumentException("seam has invalid difference between close elements");
            }
        }

        if (Dir.equals("Vertical")) {
            if (width <= 1)
                throw new IllegalArgumentException("the picture width is less than 1, too small for vertical seam carving");
            if (seam.length != height)
                throw new IllegalArgumentException("the dimension of seam does not match with the height of the picture");
            pic = new Picture(width - 1, height);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width - 1; i++) {
                    if (i < seam[j]) pic.set(i, j, this.picture.get(i, j));
                    else pic.set(i, j, this.picture.get(i + 1, j));

                }
            }
        }
        if (Dir.equals("Horizontal")) {
            if (height <= 1)
                throw new IllegalArgumentException("the picture height is less than 1, too small for horizontal seam carving");
            if (seam.length != width)
                throw new IllegalArgumentException("the dimension of seam does not match with the width of the picture");
            pic = new Picture(width, height - 1);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height - 1; j++) {
                    if (j < seam[i]) pic.set(i, j, this.picture.get(i, j));
                    else pic.set(i, j, this.picture.get(i, j + 1));
                }
            }
        }
        return pic;
    }

    public static void main(String[] args) {

    }
}
