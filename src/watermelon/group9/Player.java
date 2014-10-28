package watermelon.group9;

import java.io.*;
import java.util.*;

import watermelon.sim.Pair;
import watermelon.sim.seed;

public class Player extends watermelon.sim.Player {
	static double distowall = 1.0;
	static double distotree = 2.0;
	static double distoseed = 2.0;
	
	public void init() {}
	
	public ArrayList<seed> move(ArrayList<Pair> treelist, double width, double length, double s) {
		
		//pack problem
		ArrayList<seed> seedList = packSeedsGcdSquares(treelist, width, length);
		
		System.out.printf("Total seeds: %d\n", seedList.size());
		System.out.printf("Density: %f\n", getDensity(seedList, width, length));
		
		//label problem
		labelSeedsBestRandom(seedList, treelist, width, length, s);
		
		return seedList;
	}

	static double distance(seed tmp, Pair pair) {
		return Math.sqrt((tmp.x - pair.x) * (tmp.x - pair.x) + (tmp.y - pair.y) * (tmp.y - pair.y));
	}
	
	static double distance(seed tmp, seed pair) {
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
	
	static CirclesRadius getMatchedCirclesRadiusForSquare(double radius) {
		String filename = "radius.txt";
		File file = new File(filename);
		BufferedReader reader = null;
		double lastRadius = 0;
		try {
		    reader = new BufferedReader(new FileReader(file));
		    String text = null;

		    while ((text = reader.readLine()) != null) {
//		    	System.out.printf("%s\n", text);
//		    	System.out.printf("%d\n", Integer.parseInt(text));
		    
		    	String[] parts = text.split(" ");
		    	double radiusTemp = Double.parseDouble(parts[1]);
		    	
		    	if (radiusTemp < radius) {
		    		int numberOfCircles = Integer.parseInt(parts[0]);
			    	return new CirclesRadius(lastRadius, numberOfCircles-1);
		    	} else if (radiusTemp == radius) {
		    		int numberOfCircles = Integer.parseInt(parts[0]);
			    	return new CirclesRadius(radiusTemp, numberOfCircles);
		    	}
		    	lastRadius = radiusTemp;
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
		return null;
	}
	
	static ArrayList<Pair> getCirclesLocationsForSquare(double squareSize) {
		double circleRadius = 1/squareSize;
		CirclesRadius matchedCirclesRadius = getMatchedCirclesRadiusForSquare(circleRadius);
		int numberOfCircles = matchedCirclesRadius.circles;
		
		System.out.printf("ideal radius is %f\n", 1/squareSize);
		System.out.printf("actual radius is %f\n", matchedCirclesRadius.radius);
		System.out.printf("numberOfCircles is %d\n", matchedCirclesRadius.circles);
		
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
		    	double x = (Double.parseDouble(parts[1]) + 0.5)/matchedCirclesRadius.radius;
		    	double y = (Double.parseDouble(parts[2]) + 0.5)/matchedCirclesRadius.radius;
		    	locations.add(new Pair(x, y));
//		    	System.out.printf("%f %f\n", x, y);
//		    	System.out.printf("\n");
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
	
	static boolean squareHasTree(double x, double y, ArrayList<Pair> treelist, double squareSize) {
		for(Pair tree : treelist) {
			if(pointIsInsideSquare(tree.x, tree.y, x, y, squareSize) ||
					pointIsInsideSquare(tree.x-1, tree.y, x, y, squareSize) ||
					pointIsInsideSquare(tree.x+1, tree.y, x, y, squareSize) ||
					pointIsInsideSquare(tree.x, tree.y-1, x, y, squareSize) ||
					pointIsInsideSquare(tree.x, tree.y+1, x, y, squareSize)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean pointIsInsideSquare(double xPoint, double yPoint, double xSquare, double ySquare, double squareSize) {
		return (xPoint > xSquare && xPoint < xSquare + squareSize) && (yPoint > ySquare && yPoint < ySquare + squareSize);
	}
	
	public static double getDensity(ArrayList<seed> seedList, double width, double length) {
		return (seedList.size()*Math.PI)/(length*width);
	}

	public ArrayList<seed> packSeedsGcdSquares(ArrayList<Pair> treelist, double width, double length) {
		double squareSize = gcd(width,length);
		if(squareSize == 1) {
			width--;
			length--;
			squareSize = gcd(width, length);
		}
		
		System.out.printf("Gcd of %f and %f is appx %f\n", width, length, squareSize);
		System.out.printf("%fx%f field\n", width/squareSize, length/squareSize);
		System.out.printf("square size is %f\n", squareSize);
		
		ArrayList<Pair> circlesLocation = getCirclesLocationsForSquare(squareSize);
		
		ArrayList<seed> seedList = new ArrayList<seed>();
//		loop:
		for(int x = 0; x < width/squareSize; x++) {
			for (int y = 0; y < length/squareSize; y++) {
				if (squareHasTree(x*squareSize, y*squareSize, treelist, squareSize)) {
					for (double i = x*squareSize + distowall; i < (x+1)*squareSize - distowall; i = i + distoseed) {
						for (double j = y*squareSize + distowall; j < (y+1)*squareSize - distowall; j = j + distoseed) {
							seed tmpSeed = new seed(i, j, false);
							boolean add = true;
							for (Pair tree : treelist) {
								if (distance(tmpSeed, tree) < distotree) {
									add = false;
									break;
								}
							}
							if (add) {
								seedList.add(tmpSeed);
							}
						}
					}
					continue;
				}
				for(Pair location : circlesLocation) {
					double seedX = squareSize*x + location.x;
					double seedY = squareSize*y + location.y;
					seedList.add(new seed(seedX, seedY, false));
//					System.out.printf("%f %f\n", seedX, seedY);
				}
//				break loop; 
			}
		}
		return seedList;
	}
	
	public void labelSeedsBestRandom(ArrayList<seed> seedList, ArrayList<Pair> treelist, double width, double length, double s) {
		int n = seedList.size();
		double w[][] = new double[n][n];
		// calculate the matrix
		for(int i = 0; i < n; i++)
		{
			seed seed1 = seedList.get(i);
			double sum = 0; // calculte the numerator
			
			for(int j = 0; j < n; j++)
			{
				if(i == j)
					w[i][j] = 0;
				else
				{
					seed seed2 =  seedList.get(j);
					w[i][j]    = 1/(distance(seed1, seed2) * distance(seed1, seed2));
					sum += w[i][j];
				}
			}
			
			System.out.println("i: " + i + ", sum is: " + sum);
			for(int j = 0; j < n; j++)
			{
				w[i][j] = w[i][j]/sum;
				//System.out.println("i: " + i + ", j: " + j + ", w[i][j]: " + w[i][j]);
			}
		}
		
		Boolean subset[] = new Boolean[n];
		Boolean best_subset[] = new Boolean[n];
		for(int i = 0; i < n; i++)
			subset[i] = false;
		int time = 1000000;
		double max_w = 0; // the max sum of edge valules
		double sum_w = 0; // the current sum of edge values
		
		while(time-- != 0)
		{
			int x = (int)(Math.random()*n); // Does the random function in java cause many duplicates?
			subset[x] = !subset[x]; // change the label of x
			
			for(int i = 0; i < n; i++)
			{
				if(subset[i] != subset[x]) // i and x are in different classifications now
				{
					sum_w += w[i][x];
					sum_w += w[x][i];
				}
				
				if(i != x &&  subset[i] == subset[x])
				{
					sum_w -= w[i][x];
					sum_w -= w[x][i];
				}
			}
			
			if(max_w < sum_w)
			{
				max_w = sum_w;
			    for(int i = 0; i < n; i++)
			    {
			    	best_subset[i] = subset[i];
			    }	
			}
		}
		
		for(int i = 0; i < n; i++)
		{
			if(best_subset[i] == false)
				seedList.get(i).tetraploid = false;
			else
				seedList.get(i).tetraploid = true;
				
		}
	}
	
//	public static ArrayList<Square> getSquaresForField(ArrayList<Pair> trees, double width, double length) {
//		ArrayList<Pair> corners = new ArrayList<Pair>();
//		corners.add(new Pair(0,0));
//		corners.add(new Pair(0,width));
//		corners.add(new Pair(length,0));
//		corners.add(new Pair(length,width));
//		
//		Square biggestSquare = new Square(new Pair(0,0), 0);
//		
////		ArrayList<Pair> nextCorners = new ArrayList<Pair>();
////		for(Pair p : corners) {
////			if(p.x < width/2 && p.y < height/2) {
////				double minSize = Double.MAX_VALUE;
////				for(Pair tree : trees) {
////					if(biggestSquare.size < Math.min(tree.x, tree.y)) {
////						biggestSquare.upperLeftCorner =;
////					}
////				}
////				
////			}
////		}
//		return null;
//	}
//	
//	public static getBiggestSquareSize(Pair corner, ArrayList<Pair> corners, ArrayList<Pair> trees) {
//		
//	}
	
//	public ArrayList<seed> packHexagonal(ArrayList<Pair> treelist, double width, double length) {
//		for (double x = distowall; x < width - distowall; x+= distoseed) {
//			for (double j = y*squareSize + distowall; j < (y+1)*squareSize - distowall; j = j + distoseed) {
//				seed tmpSeed = new seed(i, j, false);
//				boolean add = true;
//				for (Pair tree : treelist) {
//					if (distance(tmpSeed, tree) < distotree) {
//						add = false;
//						break;
//					}
//				}
//				if (add) {
//					seedList.add(tmpSeed);
//				}
//			}
//		}
//		
//		return null;
//	}

	public static class CirclesRadius {
		public double radius;
		public int circles;
		
		public CirclesRadius(double radius, int circles) { this.radius = radius; this.circles = circles; }
	}
	
	public static class Square {
		public Pair upperLeftCorner;
		public double size;
		
		public Square(Pair upperLeftCorner, double size) { this.upperLeftCorner = upperLeftCorner; this.size = size; }
		
		public boolean hasPair(Pair p) {
			if(p.x >= upperLeftCorner.x && p.x <= upperLeftCorner.x + size) {
				if(p.y >= upperLeftCorner.y && p.y <= upperLeftCorner.y + size) {
					return true;
				}
			}
			return false;
		}
	}
}