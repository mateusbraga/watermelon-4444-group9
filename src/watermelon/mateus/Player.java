package watermelon.mateus;

import java.io.*;
import java.util.*;

import watermelon.sim.Pair;
import watermelon.sim.seed;

public class Player extends watermelon.sim.Player {
	static double distowall = 1.021;
	static double distotree = 1.021;
	static double distoseed = 1.021;
	
	public void init() {}

	static double distance(seed tmp, Pair pair) {
		return Math.sqrt((tmp.x - pair.x) * (tmp.x - pair.x) + (tmp.y - pair.y) * (tmp.y - pair.y));
	}
	
	static double gcd(double a, double b) {
		int precision = 10000;
		return gcd((int) (a*precision), (int) (b*precision))/(1.0*precision);
	}
	
	static int gcd(int a, int b) {
		while(b != 0) {
			int temp = b;
			b = a % b;
			a = temp;
		}
		return a;
	}
	
	static CirclesRadius getMatchedCirclesRadius(double radius) {
		String filename = "radius.txt";
		File file = new File(filename);
		BufferedReader reader = null;
		
		try {
		    reader = new BufferedReader(new FileReader(file));
		    String text = null;

		    while ((text = reader.readLine()) != null) {
//		    	System.out.printf("%s\n", text);
//		    	System.out.printf("%d\n", Integer.parseInt(text));
		    
		    	String[] parts = text.split(" ");
		    	double radiusTemp = Double.parseDouble(parts[1]);
		    	
		    	if (radiusTemp <= radius) {
		    		int numberOfCircles = Integer.parseInt(parts[0]);
			    	return new CirclesRadius(radiusTemp, numberOfCircles);
		    	}
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        if (reader != null) {
		            reader.close();
		        }
		    } catch (IOException e) {
		    }
		}
		throw new IllegalArgumentException("Couldn't find number of circles for radius " + radius);
	}
	
	static ArrayList<Pair> getCirclesLocations(int numberOfCircles) {
		String filename = "csq_coords/csq" + numberOfCircles + ".txt";
		File file = new File(filename);
		BufferedReader reader = null;
		
		ArrayList<Pair> locations = new ArrayList<Pair>(numberOfCircles);
		
		try {
		    reader = new BufferedReader(new FileReader(file));
		    String text = null;

		    while ((text = reader.readLine()) != null) {
//		    	System.out.printf("%s\n", text);
		    	String[] parts = text.trim().split("\\s+");
//		    	System.out.printf("%s\n", parts[0]);
//		    	System.out.printf("%s %s\n", parts[1], parts[2]);
//		    	System.out.printf("%s\n\n\n", parts[2]);
		    	double x = Double.parseDouble(parts[1]) + 0.5;
		    	double y = Double.parseDouble(parts[2]) + 0.5;
		    	locations.add(new Pair(x, y));
//		    	System.out.printf("%f %f\n", x, y);
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        if (reader != null) {
		            reader.close();
		        }
		    } catch (IOException e) {
		    }
		}
		return locations;
	}
	
	static boolean squareHasTree(int x, int y, ArrayList<Pair> treelist, double squareSize) {
		for(Pair tree : treelist) {
			if(tree.x > squareSize*x && tree.x < squareSize*x && tree.y > squareSize*y && tree.y < squareSize*y) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<seed> move(ArrayList<Pair> treelist, double width, double length, double s) {
		double a = width;
		double b = length;
		double squareSize = gcd(a,b);
		double circleRadius = 1/squareSize;
		CirclesRadius matchedCirclesRadius = getMatchedCirclesRadius(circleRadius);
		ArrayList<Pair> circlesLocation = getCirclesLocations(matchedCirclesRadius.circles);
		
		System.out.printf("Gcd of %f and %f is appx %f\n", a, b, squareSize);
		System.out.printf("%fx%f field\n", a/squareSize, b/squareSize);
		System.out.printf("square size is %f\n", squareSize);
		System.out.printf("radius is %f\n", 1/squareSize);
		System.out.printf("numberOfCircles is %d\n", matchedCirclesRadius.circles);
		
		
		ArrayList<seed> seedList = new ArrayList<seed>();
		loop:
		for(int x = 0; x < width/squareSize; x++) {
			for (int y = 0; y < length/squareSize; y++) {
				if (squareHasTree(x, y, treelist, squareSize)) {
					continue;
				}
				for(Pair location : circlesLocation) {
					double seedX = squareSize*x + location.x/matchedCirclesRadius.radius;
					double seedY = squareSize*y + location.y/matchedCirclesRadius.radius;
					seedList.add(new seed(seedX, seedY, false));
					System.out.printf("%f %f\n", seedX, seedY);
				}
				break loop; 
			}
		}
		
		ArrayList<seed> seedlist = new ArrayList<seed>();
		for (double i = distowall; i < width - distowall; i = i + distoseed) {
			for (double j = distowall; j < length - distowall; j = j + distoseed) {
				Random random = new Random();
				seed tmp;
				if (random.nextInt(2) == 0)
					tmp = new seed(i, j, false);
				else
					tmp = new seed(i, j, true);
				boolean add = true;
				for (int f = 0; f < treelist.size(); f++) {
					if (distance(tmp, treelist.get(f)) < distotree) {
						add = false;
						break;
					}
				}
				if (add) {
					seedlist.add(tmp);
				}
			}
		}
		System.out.printf("seedlist size is %d", seedlist.size());
		return seedList;
	}

	
	public static class CirclesRadius {
		public double radius;
		public int circles;
		
		public CirclesRadius(double radius, int circles) { this.radius = radius; this.circles = circles; }
	}
}