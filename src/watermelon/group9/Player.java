package watermelon.group9;

import java.io.*;
import java.util.*;

import watermelon.sim.Pair;
import watermelon.sim.seed;

public class Player extends watermelon.sim.Player {
	static final double distowall = 1.0;
	static final double distotree = 2.0;
	static final double distoseed = 2.0;
	static final double tolerance = 0.0001;
	
	public void init() {}
	
	public ArrayList<seed> move(ArrayList<Pair> treelist, double width, double height, double s) {
		//pack problem
		ArrayList<ArrayList<seed>> fillers = new ArrayList<ArrayList<seed>>();
		fillers.addAll(getSquarePackings(treelist, width, height));
		fillers.addAll(getHexagonalPackings(treelist, width, height));
		
		// bestPackings contains the best packings using different strategies. 
		// The motivation behind it is that a packing might be easier to label depending on the strategy used.
		// For example, hexagonal give us a more uniform pattern than square
		ArrayList<ArrayList<seed>> bestPackings = new ArrayList<ArrayList<seed>>();
		bestPackings.add(getBestFilledPacking(fillers, getSquarePackings(treelist, width, height), treelist));
		bestPackings.add(getBestFilledPacking(fillers, getHexagonalPackings(treelist, width, height), treelist));
		
		ArrayList<seed> seedList = getBestPacking(bestPackings, treelist);
		
		//label problem
//		labelSeedsBestRandom(seedList, treelist, width, height, s);
		
		System.out.printf("Total seeds: %d\n", seedList.size());
		System.out.printf("Density: %f\n", getDensity(seedList, width, height));
		
		return seedList;
	}

	static double distance(seed tmp, Pair pair) {
		return Math.sqrt((tmp.x - pair.x) * (tmp.x - pair.x) + (tmp.y - pair.y) * (tmp.y - pair.y));
	}
	
	static double distance(seed tmp, seed pair) {
		return Math.sqrt((tmp.x - pair.x) * (tmp.x - pair.x) + (tmp.y - pair.y) * (tmp.y - pair.y));
	}
	
	public static double getDensity(ArrayList<seed> seedList, double width, double height) {
		return (seedList.size()*Math.PI)/(height*width);
	}

	public void labelSeedsBestRandom(ArrayList<seed> seedList, ArrayList<Pair> treelist, double width, double height, double s) {
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
	
	public static ArrayList<seed> getBestFilledPacking(ArrayList<ArrayList<seed>> fillers, ArrayList<ArrayList<seed>> packings, ArrayList<Pair> treelist) {
		ArrayList<ArrayList<seed>> filledPackings = new ArrayList<ArrayList<seed>>();
		for(ArrayList<seed> p : packings) {
			removeSeedsNearTrees(p, treelist);
			for(ArrayList<seed> filler : fillers) {
				ArrayList<seed> result = new ArrayList<seed>(p);
				mergeSeedLists(result, filler);
				filledPackings.add(result);
			}
		}
		
		return getBestPacking(filledPackings, treelist);
	}
	
	public static void removeSeedsNearTrees(ArrayList<seed> seedList, ArrayList<Pair> treelist) {
		Iterator<seed> it = seedList.iterator();
		while (it.hasNext()) {
			seed s = it.next();
			for (Pair tree : treelist) {
				if (distance(s, tree) < distotree - tolerance) {
					it.remove();
					break;
				}
			}
		}
	}
	
	public static void mergeSeedLists(ArrayList<seed> seedListBase, ArrayList<seed> seedListFiller) {
		Iterator<seed> it = seedListFiller.iterator();
		while(it.hasNext()) {
			seed s = it.next();
			for(seed baseSeed : seedListBase) {
				if(distance(s, baseSeed) < distoseed - tolerance) {
					it.remove();
					break;
				}
			}
		}
		
		seedListBase.addAll(seedListFiller);
	}
	
	public static ArrayList<seed> getBestPacking(ArrayList<ArrayList<seed>> packings, ArrayList<Pair> treelist) {
		ArrayList<seed> bestPacking = new ArrayList<seed>();
		for(ArrayList<seed> p : packings) {
			removeSeedsNearTrees(p, treelist);
			if(p.size() > bestPacking.size()) {
				bestPacking = p;
			}
		}
		return bestPacking;
	}
	
	public ArrayList<ArrayList<seed>> getHexagonalPackings(ArrayList<Pair> treelist, double width, double height) {
		ArrayList<ArrayList<seed>> packings = new ArrayList<ArrayList<seed>>();
		
		ArrayList<seed> verticalPacking = packHexagonalDirectional(treelist, width, height, true);
		packings.add(verticalPacking);
		
		ArrayList<seed> verticalPackingInverted = new ArrayList<seed>();
		for(seed s : verticalPacking) {
			verticalPackingInverted.add(new seed(s.x, height - s.y, false));
		}
		packings.add(verticalPackingInverted);
		
		ArrayList<seed> horizontalPacking = packHexagonalDirectional(treelist, width, height, false);
		packings.add(horizontalPacking);
//		
		ArrayList<seed> horizontalPackingInverted = new ArrayList<seed>();
		for(seed s : horizontalPacking) {
			horizontalPackingInverted.add(new seed(width - s.x, s.y, false));
		}
		packings.add(horizontalPackingInverted);
		
		return packings;
	}
	
	
	public ArrayList<seed> packHexagonalDirectional(ArrayList<Pair> treelist, double width, double height, boolean doVertical) {
		ArrayList<seed> seedList = new ArrayList<seed>();
		
		double x = distowall;
		double y = height - distowall;
		
		boolean nextIsRowFromEdge = false;
		if(doVertical) {
			while(x >= distowall && x <= width - distowall) {
				while(y >= distowall && y <= height - distowall) {
					seedList.add(new seed(x, y, false));
					y -= distoseed;
				}
				if (nextIsRowFromEdge) {
					y = height - distowall;
				} else {
					y = height - distoseed;
				}
				x += Math.sqrt(3);
				nextIsRowFromEdge = !nextIsRowFromEdge;
			}
		} else {
			while(y >= distowall && y <= height - distowall) {
				while(x >= distowall && x <= width - distowall) {
					seedList.add(new seed(x, y, false));
					x += distoseed;
				}
				if (nextIsRowFromEdge) {
					x = distowall;
				} else {
					x = distoseed;
				}
				y -= Math.sqrt(3);
				nextIsRowFromEdge = !nextIsRowFromEdge;
			}	
		}
		return seedList;
	}
	
	public ArrayList<ArrayList<seed>> getSquarePackings(ArrayList<Pair> treelist, double width, double height) {
		ArrayList<ArrayList<seed>> packings = new ArrayList<ArrayList<seed>>();
		
		Square bestSquare;
		double size = Math.min(width, height);;
		
		bestSquare = new Square(new Pair(0,0), size);
		packings.add(packSquare(bestSquare));
		
		bestSquare = new Square(new Pair(width - size,0), size);
		packings.add(packSquare(bestSquare));
		
		bestSquare = new Square(new Pair(0, height - size), size);
		packings.add(packSquare(bestSquare));
		
		bestSquare = new Square(new Pair(width - size, height - size), size);
		packings.add(packSquare(bestSquare));
		
		return packings;
	}
	
	public ArrayList<seed> packSquare(Square square) {
		ArrayList<Pair> circlesLocation = getCirclesLocationsForSquare(square.size);
		
		ArrayList<seed> seedList = new ArrayList<seed>();
		for(Pair location : circlesLocation) {
			double seedX = square.upperLeftCorner.x + location.x;
			double seedY = square.upperLeftCorner.y + location.y;
			seedList.add(new seed(seedX, seedY, false));
		}
		return seedList;
	}
	
	public static CirclesRadius getMatchedCirclesRadiusForSquare(double radius) {
		String filename = "radius.txt";
		File file = new File(filename);
		BufferedReader reader = null;
		double lastRadius = 0;
		int lastNumberOfCircles = 0;
		try {
		    reader = new BufferedReader(new FileReader(file));
		    String text = null;

		    while ((text = reader.readLine()) != null) {
//		    	System.out.printf("%s\n", text);
//		    	System.out.printf("%d\n", Integer.parseInt(text));
		    
		    	String[] parts = text.split(" ");
		    	double radiusTemp = Double.parseDouble(parts[1]);
	    		int numberOfCircles = Integer.parseInt(parts[0]);

		    	if (radiusTemp < radius) {
			    	return new CirclesRadius(lastRadius, lastNumberOfCircles);
		    	} else if (radiusTemp == radius) {
			    	return new CirclesRadius(radiusTemp, numberOfCircles);
		    	}
		    	lastRadius = radiusTemp;
		    	lastNumberOfCircles = numberOfCircles;
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
	
	public static ArrayList<Pair> getCirclesLocationsForSquare(double squareSize) {
		double circleRadius = 1/squareSize;
		CirclesRadius matchedCirclesRadius = getMatchedCirclesRadiusForSquare(circleRadius);
		int numberOfCircles = matchedCirclesRadius.circles;
		
//		System.out.printf("ideal radius is %f\n", 1/squareSize);
//		System.out.printf("actual radius is %f\n", matchedCirclesRadius.radius);
//		System.out.printf("numberOfCircles is %d\n", matchedCirclesRadius.circles);
		
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
		
		public boolean squareHasTree(ArrayList<Pair> treelist) {
			for(Pair tree : treelist) {
				if(pointIsInsideSquare(tree.x, tree.y, upperLeftCorner.x, upperLeftCorner.y, size) ||
						pointIsInsideSquare(tree.x-1, tree.y, upperLeftCorner.x, upperLeftCorner.y, size) ||
						pointIsInsideSquare(tree.x+1, tree.y, upperLeftCorner.x, upperLeftCorner.y, size) ||
						pointIsInsideSquare(tree.x, tree.y-1, upperLeftCorner.x, upperLeftCorner.y, size) ||
						pointIsInsideSquare(tree.x, tree.y+1, upperLeftCorner.x, upperLeftCorner.y, size)) {
					return true;
				}
			}
			return false;
		}
		
		
		public static boolean pointIsInsideSquare(double xPoint, double yPoint, double xSquare, double ySquare, double squareSize) {
			return (xPoint > xSquare && xPoint < xSquare + squareSize) && (yPoint > ySquare && yPoint < ySquare + squareSize);
		}
		
		public String toString() {
			return "(" + upperLeftCorner.x + ", " + upperLeftCorner.y + ") " + size;
		}
	}
}