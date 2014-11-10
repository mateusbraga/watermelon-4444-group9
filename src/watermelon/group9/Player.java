package watermelon.group9;

import java.io.*;
import java.util.*;

import watermelon.sim.Pair;
import watermelon.sim.seed;

public class Player extends watermelon.sim.Player {
	static final double distowall = 1.0;
	static final double distotree = 2.0;
	static final double distoseed = 2.0;
	static final double tolerance = 0.00001;
	static final double NEIGHBOR_RANGE = 2.5;
	private static final int GAMBLING_TIMES = 20;
	static final Random randomGen = new Random();
	
	double fieldWidth;
	double fieldHeight;
	
	public void init() {}
	
	public ArrayList<seed> move(ArrayList<Pair> treelist, double width, double height, double s) {
		fieldWidth = width;
		fieldHeight = height;
		//pack problem

		
		// bestPackings contains the best packings using different strategies. 
		// The motivation behind it is that a packing might be easier to label depending on the strategy used.
		// For example, hexagonal give us a more uniform pattern than square
		ArrayList<ArrayList<seed>> bestPackings = new ArrayList<ArrayList<seed>>();

		ArrayList<seed> fillers = new ArrayList<seed>();
		for (ArrayList<seed> p : getSquarePackings(treelist, width, height)) {
			fillers.addAll(p);
		}
		for (ArrayList<seed> p : getHexagonalPackings(treelist, width, height)) {
			fillers.addAll(p);
		}
		
		bestPackings.add(getBestFilledPacking(fillers, getSquarePackings(treelist, width, height), treelist));
		System.out.printf("best square: %d\n", bestPackings.get(bestPackings.size()-1).size());
		
		bestPackings.add(getBestFilledPacking(fillers, getHexagonalPackings(treelist, width, height), treelist));
		System.out.printf("best hexagonal: %d\n", bestPackings.get(bestPackings.size()-1).size());
		
		ArrayList<seed> seedList = getBestPacking(bestPackings, treelist);
//		ArrayList<seed> seedList =getBestFilledPacking(fillers, getSquarePackings(treelist, width, height), treelist);
//		ArrayList<seed> seedList = getBestPacking(getHexagonalPackings(treelist, width, height), treelist);
//		ArrayList<seed> seedList = getSeedsCloser(removeSeedsNearTrees(getHexagonalPackings(treelist, width, height).get(0), treelist), treelist);
//		ArrayList<seed> seedList = removeSeedsNearTrees(getHexagonalPackings(treelist, width, height).get(0), treelist);
//		ArrayList<seed> seedList = getHexagonalPackings(treelist, width, height).get(0);
		
		
		//coloring problem
//		Coloring c = new Coloring(seedList.size());
//		if(seedList.size() <= 23) {
//			c.colorSearch(seedList);
//		} else {
//			c.colorHeuristic(seedList);
//			//c.colorRandomly2(seedList);
//		}
		
		System.out.printf("Total seeds: %d\n", seedList.size());
		System.out.printf("Density: %f\n", getDensity(seedList, width, height));
		
		return seedList;
	}

	static double distance(seed tmp, Pair pair) {
		return Math.sqrt((tmp.x - pair.x) * (tmp.x - pair.x) + (tmp.y - pair.y) * (tmp.y - pair.y));
	}
	static double distance(Pair tmp, seed pair) {
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
	
	public ArrayList<seed> getBestFilledPacking(ArrayList<seed> fillers, ArrayList<ArrayList<seed>> packings, ArrayList<Pair> treelist) {
		ArrayList<ArrayList<seed>> filledPackings = new ArrayList<ArrayList<seed>>();
		for(ArrayList<seed> p : packings) {
			ArrayList<seed> result = removeSeedsNearTrees(p, treelist);
			result = getSeedsCloser(result, treelist);
			result = mergeSeedLists(result, fillers);
			result = getSeedsCloser(result, treelist);
			result = mergeSeedLists(result, fillers);
			filledPackings.add(result);
			System.out.printf(".");
		}
		
		return getBestPacking(filledPackings, treelist);
	}
	
	public ArrayList<seed> removeSeedsNearTrees(ArrayList<seed> seedList, ArrayList<Pair> treelist) {
		PackingSeeds packing = new PackingSeeds(seedList);
		PackingSeeds treePacking = new PackingSeeds(treelist, true);
		
		ArrayList<seed> gamblingSeeds = new ArrayList<seed>();
		for (Pair tree : treelist) {
			seed treeSeed = new seed(tree.x, tree.y, false);
			Set<seed> conflictSet = packing.getNeighbors(treeSeed, distotree - tolerance);
			for(seed badSeed: conflictSet) {
				addGamblingSeeds(treeSeed, badSeed, gamblingSeeds);
				packing.remove(badSeed);
			}
		}
		
		for(seed s : gamblingSeeds) {
			Set<seed> treeConflictSet = treePacking.getNeighbors(s, distotree-tolerance);
			if (treeConflictSet.size() == 0) {
				Set<seed> conflictSet = packing.getNeighbors(s, distoseed- tolerance);
				if(conflictSet.size() == 0) {
					packing.add(s);
				}
				
			}
		}
		
		return packing.getArrayList();
	}
	
	public boolean isAdjacentToWall(seed s) {
		if (s.x <= 1 + tolerance || s.y <= 1 +tolerance || s.x >= fieldWidth - tolerance || s.y >= fieldHeight - tolerance) {
			return true;
		}
		return false;
	}
	
	public ArrayList<seed> getSeedsCloser(ArrayList<seed> seedList, ArrayList<Pair> treelist) {
		PackingSeeds packing = new PackingSeeds(seedList);
		PackingSeeds treePacking = new PackingSeeds(treelist, true);
		
		for(Pair tree : treelist) {
			seed s = new seed(tree.x, tree.y, false);
			Set<seed> neighbors = packing.getNeighbors(s, 6);
//			System.out.printf("%f, %f has %d neighbors\n", treeSeed.x, treeSeed.y, neighbors.size());
			for(seed neighbor : neighbors) {
				double dist = distance(s, neighbor);
				if (dist < 2.01) {
					continue;
				}
				if(isAdjacentToWall(neighbor)) {
					continue;
				}
				if(neighbor.x < s.x) {
					continue;
				}
//					if(neighbor.y < s.y) {
//						continue;
//					}
				getSeedsCloserEngine(packing, treePacking, s, neighbor);
			}
		}
//		
		for(seed s : seedList) {
			Set<seed> neighbors = packing.getNeighbors(s, 6);
//			System.out.printf("%f, %f has %d neighbors\n", treeSeed.x, treeSeed.y, neighbors.size());
			for(seed neighbor : neighbors) {
				double dist = distance(s, neighbor);
				if (dist < 2.01) {
					continue;
				}
				if(isAdjacentToWall(neighbor)) {
					continue;
				}
				if(neighbor.x < s.x) {
					continue;
				}
//					if(neighbor.y < s.y) {
//						continue;
//					}
				getSeedsCloserEngine(packing, treePacking, s, neighbor);
			}
		}
		
		return packing.getArrayList();
	}
	
	public void getSeedsCloserEngine(PackingSeeds packing, PackingSeeds treePacking, seed fixed, seed moveable) {
		packing.remove(moveable);
		
		ArrayList<seed> gamblingSeeds = new ArrayList<seed>();
		addGamblingSeeds(fixed, moveable, gamblingSeeds);
//			System.out.printf("gambling %d\n", gamblingSeeds.size());
		
		for(seed potentialSeed : gamblingSeeds) {
//				System.out.printf("-");
//				System.out.printf("original:%f,%f new: %f,%f\n", neighbor.x, neighbor.y, potentialSeed.x, potentialSeed.y);
			Set<seed> conflictSet = packing.getNeighbors(potentialSeed, distoseed - tolerance);
			if(conflictSet.size() == 0) {
				Set<seed> treeConflictSet = treePacking.getNeighbors(potentialSeed, distotree-tolerance);
				if (treeConflictSet.size() == 0) {
//					System.out.printf("Got closer to tree! From %f to %f\n", distance(fixed, moveable), distance(potentialSeed, fixed));
					packing.add(potentialSeed);
					return;
				}
			}
		}
		packing.add(moveable);
	}
	
	public ArrayList<seed> mergeSeedLists(ArrayList<seed> seedListBase, ArrayList<seed> seedListFiller) {
		PackingSeeds packing = new PackingSeeds(seedListBase);
		
		ArrayList<seed> gamblingSeeds = new ArrayList<seed>(); 
		for(seed fillerSeed : seedListFiller) {
			Set<seed> conflicts = packing.getNeighbors(fillerSeed, distoseed - tolerance);
			if(conflicts.size() == 0) {
				packing.add(fillerSeed);
			}
			
			for(seed s: conflicts) {
				addGamblingSeeds(s, fillerSeed, gamblingSeeds);
			}
		}
		
		for(seed s : gamblingSeeds) {
			Set<seed> conflictSet = packing.getNeighbors(s, distoseed - tolerance);
			if(conflictSet.size() == 0) {
				packing.add(s);
			}
		}
		
		return packing.getArrayList();
	}
	
	public void addGamblingSeeds(seed fixed, seed invalid, ArrayList<seed> gamblingSeeds) {
		for(int i = 0; i < GAMBLING_TIMES; i++) {
			seed gamble = getRandomCloserPosition(fixed, invalid);
			if (seedInsideField(gamble)) {
				gamblingSeeds.add(gamble);
			}
		}
	}
	
	public seed getRandomCloserPosition(seed fixed, seed moveable) {
		double dist = distance(fixed, moveable);
		
		double dx = randomGen.nextDouble() * (distoseed - dist);
//			System.out.printf("%f %f,%f %f,%f - dx %f\n", dist, fixed.x, fixed.y, moveable.x, moveable.y, dx);
		double newX;
		double newY;
		if (fixed.x < moveable.x) {
			// move invalid to the right
			newX = moveable.x + dx;
		} else {
			newX = moveable.x - dx;
		}
		
		double squaredXDiff = (newX-fixed.x)*(newX-fixed.x);
		double squaredYDiff =  4 - squaredXDiff;
		double dy = Math.sqrt(squaredYDiff);
		if (fixed.y < moveable.y) {
			// move invalid to bottom
			newY = fixed.y + dy;
		} else {
			newY = fixed.y - dy;
		}
		
		seed newSeed = new seed(newX, newY, false);
//		System.out.printf("newSeed %f,%f\n", newSeed.x, newSeed.y);
		return newSeed;
	}
	
	public boolean seedInsideField(seed s) {
		if(Double.isNaN(s.x) || Double.isNaN(s.y)) {
			return false;
		}
		if (s.x < distowall - tolerance || s.y < distowall - tolerance) {
			return false;
		}
		if (s.x > fieldWidth - distowall + tolerance || s.y > fieldHeight -distowall + tolerance) {
			return false;
		}
		return true;
	}
	
	public ArrayList<seed> getBestPacking(ArrayList<ArrayList<seed>> packings, ArrayList<Pair> treelist) {
		ArrayList<seed> bestPacking = new ArrayList<seed>();
		for(ArrayList<seed> p : packings) {
			ArrayList<seed> result = removeSeedsNearTrees(p, treelist);
//			System.out.printf("Packing before: %d after removing seeds near trees: %d\n", p.size(), result.size());
			if(result.size() > bestPacking.size()) {
				bestPacking = result;
			}
		}
		return bestPacking;
	}
	
	public ArrayList<ArrayList<seed>> getHexagonalPackings(ArrayList<Pair> treelist, double width, double height) {
		ArrayList<ArrayList<seed>> packings = new ArrayList<ArrayList<seed>>();
		
		// botton-up
		ArrayList<seed> verticalPacking = packHexagonalDirectional(treelist, width, height, true);
		packings.add(verticalPacking);
		
		packings.add(generateHorizontalInvertedPacking(verticalPacking, width, height));
		packings.add(generateVerticalInvertedPacking(verticalPacking, width, height));
		
		// left-right
		ArrayList<seed> horizontalPacking = packHexagonalDirectional(treelist, width, height, false);
		packings.add(horizontalPacking);
		
		packings.add(generateVerticalInvertedPacking(horizontalPacking, width, height));
		packings.add(generateHorizontalInvertedPacking(horizontalPacking, width, height));
		
		return packings;
	}
	
	
	public ArrayList<seed> packHexagonalDirectional(ArrayList<Pair> treelist, double width, double height, boolean doVertical) {
		ArrayList<seed> seedList = new ArrayList<seed>();
		
		double x = distowall;
		double y = height - distowall;
		
		boolean nextIsRowFromEdge = false;
		if(doVertical) {
			while(x >= distowall -tolerance && x <= width - distowall + tolerance) {
				while(y >= distowall -tolerance && y <= height - distowall + tolerance) {
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
			while(y >= distowall -tolerance && y <= height - distowall+tolerance) {
				while(x >= distowall-tolerance && x <= width - distowall+tolerance) {
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
		ArrayList<seed> squarePacking = packSquare(bestSquare);
		ArrayList<seed> currentPacking = squarePacking;
		
		// top-left corner
		packings.add(currentPacking);
		//top-right corner
		packings.add(generateMovedOriginPacking(currentPacking, width - size, 0));
		//bottom-left corner
		packings.add(generateMovedOriginPacking(currentPacking, 0, height - size));
		//bottom-righ corner
		packings.add(generateMovedOriginPacking(currentPacking, width - size, height-size));
		
		//variant invert vertical
		currentPacking = generateVerticalInvertedPacking(currentPacking, size, size);
		// top-left corner
		packings.add(currentPacking);
		//top-right corner
		packings.add(generateMovedOriginPacking(currentPacking, width - size, 0));
		//bottom-left corner
		packings.add(generateMovedOriginPacking(currentPacking, 0, height - size));
		//bottom-righ corner
		packings.add(generateMovedOriginPacking(currentPacking, width - size, height-size));
		
		//variant invert horizontal and vertical
		currentPacking = generateHorizontalInvertedPacking(currentPacking, size, size);
		// top-left corner
		packings.add(currentPacking);
		//top-right corner
		packings.add(generateMovedOriginPacking(currentPacking, width - size, 0));
		//bottom-left corner
		packings.add(generateMovedOriginPacking(currentPacking, 0, height - size));
		//bottom-righ corner
		packings.add(generateMovedOriginPacking(currentPacking, width - size, height-size));
		
		//variant invert horizontal
		currentPacking = generateHorizontalInvertedPacking(squarePacking, size, size);
		// top-left corner
		packings.add(currentPacking);
		//top-right corner
		packings.add(generateMovedOriginPacking(currentPacking, width - size, 0));
		//bottom-left corner
		packings.add(generateMovedOriginPacking(currentPacking, 0, height - size));
		//bottom-righ corner
		packings.add(generateMovedOriginPacking(currentPacking, width - size, height-size));
		
		return packings;
	}
	
	public ArrayList<seed> generateVerticalInvertedPacking(ArrayList<seed> seedList, double width, double height) {
		ArrayList<seed> verticalInvertedPacking = new ArrayList<seed>();
		for(seed s : seedList) {
			verticalInvertedPacking.add(new seed(s.x, Math.abs(height - s.y), false));
		}
		return verticalInvertedPacking;
	}
	
	public ArrayList<seed> generateHorizontalInvertedPacking(ArrayList<seed> seedList, double width, double height) {
		ArrayList<seed> horizontalInvertedPacking = new ArrayList<seed>();
		for(seed s : seedList) {
			horizontalInvertedPacking.add(new seed(Math.abs(width - s.x), s.y, false));
		}
		return horizontalInvertedPacking;
	}
	
	public ArrayList<seed> generateMovedOriginPacking(ArrayList<seed> seedList, double dx, double dy) {
		ArrayList<seed> newOriginPacking = new ArrayList<seed>();
		for(seed s : seedList) {
			newOriginPacking.add(new seed(s.x + dx, s.y + dy, false));
		}
		return newOriginPacking;
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
	
	public class Comparators {
        public Comparator<seed> BySeedX = new Comparator<seed>() {
            @Override
            public int compare(seed o1, seed o2) {
                double diff = o1.x - o2.x;
                if (diff > 0) {
                	return 1;
                } else if (diff == 0) {
                	if (o1.y == o2.y) {
                		return 0;
                	} else {
                		return BySeedY.compare(o1, o2);
                	}
                } else {
                	return -1;
                }
            }
        };
        public Comparator<seed> BySeedY = new Comparator<seed>() {
            @Override
            public int compare(seed o1, seed o2) {
                double diff = o1.y - o2.y;
                if (diff > 0) {
                	return 1;
                } else if (diff == 0) {
                	if (o1.x == o2.x) {
                		return 0;
                	} else {
                		return BySeedX.compare(o1, o2);
                	}
                } else {
                	return -1;
                }
            }
        };
    }
	public Comparators myComparators = new Comparators();
	
	public class PackingSeeds {
		SortedSet<seed> seedsByX;
		SortedSet<seed> seedsByY;
		
		public PackingSeeds(ArrayList<seed> seedList) {
			seedsByX = new TreeSet<seed>(myComparators.BySeedX);
			seedsByY = new TreeSet<seed>(myComparators.BySeedY);
			
			seedsByX.addAll(seedList);
			seedsByY.addAll(seedList);
		}
		
		public PackingSeeds(ArrayList<Pair> treelist, boolean anything) {
			seedsByX = new TreeSet<seed>(myComparators.BySeedX);
			seedsByY = new TreeSet<seed>(myComparators.BySeedY);
			
			for(Pair tree : treelist) {
				seed treeSeed = new seed(tree.x, tree.y, false);
				seedsByX.add(treeSeed);
				seedsByY.add(treeSeed);
			}
			
		}
		
		public void remove(seed conflict) {
			seedsByX.remove(conflict);
			seedsByY.remove(conflict);
		}

		public void add(seed s) {
			seedsByX.add(s);
			seedsByY.add(s);
		}
		
		public Set<seed> getNeighbors(final seed s, double range) {
			SortedSet<seed> byX = seedsByX.subSet(new seed(s.x -range, s.y, false), new seed(s.x +range, s.y, false));
			SortedSet<seed> byY = seedsByY.subSet(new seed(s.x, s.y - range, false), new seed(s.x, s.y + range, false));
			
			SortedSet<seed> neighbors = new TreeSet<seed>(new Comparator<seed>() {
	            @Override
	            public int compare(seed o1, seed o2) {
	                double diff = distance(o1, s) - distance(o2, s);
	                if (diff > 0) {
	                	return 1;
	                } else if (diff == 0) {
	                	return myComparators.BySeedX.compare(o1, o2);
	                } else {
	                	return -1;
	                }
	            }
	        });
			neighbors.addAll(byX);
			neighbors.retainAll(byY);
			
//			System.out.printf("byX %d byY %d neighbors %d\n", byX.size(), byY.size(), neighbors.size());
			
			Iterator<seed> it = neighbors.iterator();
			while(it.hasNext()) {
				seed itSeed = it.next();
				if(distance(s, itSeed) > range) {
					it.remove();
				}
			}
			return neighbors;
		}
		
		public ArrayList<seed> getArrayList() {
			return new ArrayList<seed>(seedsByY);
		}
	}
}