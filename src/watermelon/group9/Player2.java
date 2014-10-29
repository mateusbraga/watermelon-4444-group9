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
	
	public static Rectangle fullFieldRectangle;
	
	public void init() {}
	
	public ArrayList<seed> move(ArrayList<Pair> treelist, double width, double length, double s) {
		fullFieldRectangle = new Rectangle(new Pair(0,0), width, length);
		
		//pack problem
		ArrayList<Packing> packings = new ArrayList<Packing>();
		packings.add(new HexagonalPacking(fullFieldRectangle));
		packings.add(new BestSquarePacking());

		ArrayList<seed> bestPacking = new ArrayList<seed>();
		for(Packing p : packings) {
			ArrayList<seed> result = p.pack(treelist, width, length);
			System.out.printf("%s got %d seeds\n", p.getClass().getName(), result.size());
			if(result.size() > bestPacking.size()) {
				bestPacking = result;
			}
		}
		
		ArrayList<seed> seedList = bestPacking;
//		seedList = new BestSquarePacking().pack(treelist, width, length);

		
		//label problem
//		labelSeedsBestRandom(seedList, treelist, width, length, s);


		Coloring c = new Coloring(seedList.size());
		if(seedList.size() <= 23)
			c.colorSearch(seedList);
		else
			c.colorRandomly2(seedList);
		
		System.out.printf("Total seeds: %d\n", seedList.size());
		System.out.printf("Density: %f\n", getDensity(seedList, width, length));
		
		return seedList;
	}

	static double distance(seed tmp, Pair pair) {
		return Math.sqrt((tmp.x - pair.x) * (tmp.x - pair.x) + (tmp.y - pair.y) * (tmp.y - pair.y));
	}
	
	static double distance(seed tmp, seed pair) {
		return Math.sqrt((tmp.x - pair.x) * (tmp.x - pair.x) + (tmp.y - pair.y) * (tmp.y - pair.y));
	}
	
	public static double getDensity(ArrayList<seed> seedList, double width, double length) {
		return (seedList.size()*Math.PI)/(length*width);
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
	
	public static class HexagonalPacking implements Packing {
		public Rectangle rectangle;
		
		public HexagonalPacking(Rectangle rectangle) { this.rectangle = rectangle; }
		
		public ArrayList<seed> pack(ArrayList<Pair> treelist, double width, double length) {
			ArrayList<seed> verticalPacking = packHexagonalDirectional(treelist, width, length, true);
			ArrayList<seed> horizontalPacking = packHexagonalDirectional(treelist, width, length, false);
			System.out.printf("Horizontal Packing: %d\n", horizontalPacking.size());
			System.out.printf("Vertical Packing: %d\n", horizontalPacking.size());
			if (verticalPacking.size() <= horizontalPacking.size()) {
				return horizontalPacking;
			}
			return verticalPacking;
		}
		
		public ArrayList<seed> packHexagonalDirectional(ArrayList<Pair> treelist, double width, double length, boolean doVertical) {
			ArrayList<seed> seedList = new ArrayList<seed>();
			
			double x = distowall + rectangle.upperLeftCorner.x;
			double y = rectangle.length - distowall + rectangle.upperLeftCorner.y;
			
			boolean nextIsRowFromEdge = false;
			if(doVertical) {
				while(x >= distowall + rectangle.upperLeftCorner.x && x <= rectangle.width + rectangle.upperLeftCorner.x - distowall) {
					while(y >= distowall + rectangle.upperLeftCorner.y && y <= rectangle.length + rectangle.upperLeftCorner.y - distowall) {
						seed tmpSeed = new seed(x, y, false);
						
						
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
						
						
						y -= distoseed;
					}
					if (nextIsRowFromEdge) {
						y = rectangle.length + rectangle.upperLeftCorner.y - distowall;
					} else {
						y = rectangle.length + rectangle.upperLeftCorner.y - distoseed;
					}
					x += Math.sqrt(3);
					nextIsRowFromEdge = !nextIsRowFromEdge;
				}
			} else {
				while(y >= distowall + rectangle.upperLeftCorner.y && y <= rectangle.length + rectangle.upperLeftCorner.y - distowall) {
					while(x >= distowall + rectangle.upperLeftCorner.x && x <= rectangle.width + rectangle.upperLeftCorner.x - distowall) {
						seed tmpSeed = new seed(x, y, false);
						

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
						
						
						x += distoseed;
					}
					if (nextIsRowFromEdge) {
						x = distowall + rectangle.upperLeftCorner.x;
					} else {
						x = distoseed + rectangle.upperLeftCorner.x;
					}
					y -= Math.sqrt(3);
					nextIsRowFromEdge = !nextIsRowFromEdge;
				}	
			}
			
		
			return seedList;
		}
	}
	
	public static class BestSquarePacking implements Packing {
		public ArrayList<seed> pack(ArrayList<Pair> treelist, double width, double length) {
			ArrayList<seed> seedList = new ArrayList<seed>();
			ArrayList<seed> tempList; 
			
			Square bestSquare = getBiggestPossibleSquare(treelist, width, length);
			System.out.printf("Best Square: %s\n", bestSquare);
			tempList = new SquarePacking(bestSquare).pack(treelist, width, length);
			seedList.addAll(tempList);
			
			Rectangle full = new Rectangle(new Pair(0,0), width, length);
			tempList = new HexagonalPacking(full).pack(treelist, width, length);
			
			Iterator<seed> seedIt = tempList.iterator();
			while(seedIt.hasNext()) {
				seed s = seedIt.next();
				for(seed squareSeed : seedList) {
					if(distance(s, squareSeed) < 1.9999) {
						seedIt.remove();
						break;
					}
				}
			}
			
			seedList.addAll(tempList);
			
			return seedList;
		}
		
		public static Square getBiggestPossibleSquare(ArrayList<Pair> treelist, double width, double length) {
			if(treelist.size() == 0) {
				return new Square(new Pair(0,0), Math.min(width, length));
			}
			
			Square bestSquare = new Square(new Pair(0,0), 0);
			Square currentSquare = new Square(new Pair(0,0), Double.MAX_VALUE);
			
			for(Pair tree : treelist) {
				double swCorner = Math.max(Math.abs(tree.x), Math.abs(length - tree.y)) - 1;
				if(swCorner < currentSquare.size) {
					currentSquare.upperLeftCorner.x = 0;
					currentSquare.upperLeftCorner.y = length - swCorner;
					currentSquare.size = swCorner;
				}
			}
			if(bestSquare.size < currentSquare.size) {
				bestSquare.upperLeftCorner.x = currentSquare.upperLeftCorner.x;
				bestSquare.upperLeftCorner.y = currentSquare.upperLeftCorner.y;
				bestSquare.size = currentSquare.size;
			}
			currentSquare.size = Double.MAX_VALUE;
			
			for(Pair tree : treelist) {
				double seCorner = Math.max(Math.abs(width - tree.x), Math.abs(length - tree.y)) - 1;
				if(seCorner < currentSquare.size) {
					currentSquare.upperLeftCorner.x = width - seCorner;
					currentSquare.upperLeftCorner.y = length - seCorner;
					currentSquare.size = seCorner;
				}
			}
			
			if(bestSquare.size < currentSquare.size) {
				bestSquare.upperLeftCorner.x = currentSquare.upperLeftCorner.x;
				bestSquare.upperLeftCorner.y = currentSquare.upperLeftCorner.y;
				bestSquare.size = currentSquare.size;
			}
			currentSquare.size = Double.MAX_VALUE;
			
			for(Pair tree : treelist) {
				double nwCorner = Math.max(Math.abs(tree.x), Math.abs(tree.y)) - 1;
				if(nwCorner < currentSquare.size) {
					currentSquare.upperLeftCorner.x = 0;
					currentSquare.upperLeftCorner.y = 0;
					currentSquare.size = nwCorner;
				}
			}
			
			if(bestSquare.size < currentSquare.size) {
				bestSquare.upperLeftCorner.x = currentSquare.upperLeftCorner.x;
				bestSquare.upperLeftCorner.y = currentSquare.upperLeftCorner.y;
				bestSquare.size = currentSquare.size;
			}
			currentSquare.size = Double.MAX_VALUE;
			
			for(Pair tree : treelist) {
				double neCorner = Math.max(Math.abs(width - tree.x), Math.abs(tree.y)) - 1;
				if(neCorner < currentSquare.size) {
					currentSquare.upperLeftCorner.x = width - neCorner;
					currentSquare.upperLeftCorner.y = 0;
					currentSquare.size = neCorner;
				}
			}
			if(bestSquare.size < currentSquare.size) {
				bestSquare.upperLeftCorner.x = currentSquare.upperLeftCorner.x;
				bestSquare.upperLeftCorner.y = currentSquare.upperLeftCorner.y;
				bestSquare.size = currentSquare.size;
			}

			return bestSquare;
		}

	}
	
	public static class SquarePacking implements Packing {
		public Square square;
		public SquarePacking(Square square) {
			this.square = square;
		}
		
		public ArrayList<seed> pack(ArrayList<Pair> treelist, double width, double length) {
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
//			    	System.out.printf("%s\n", text);
//			    	System.out.printf("%d\n", Integer.parseInt(text));
			    
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
//			    	System.out.printf("%s\n", text);
			    	String[] parts = text.trim().split("\\s+");
//			    	System.out.printf("%s\n", parts[0]);
//			    	System.out.printf("%s %s\n", parts[1], parts[2]);
//			    	System.out.printf("%s\n\n\n", parts[2]);
			    	double x = (Double.parseDouble(parts[1]) + 0.5)/matchedCirclesRadius.radius;
			    	double y = (Double.parseDouble(parts[2]) + 0.5)/matchedCirclesRadius.radius;
			    	locations.add(new Pair(x, y));
//			    	System.out.printf("%f %f\n", x, y);
//			    	System.out.printf("\n");
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
		
	}
	
	public static interface Packing {
		public ArrayList<seed> pack(ArrayList<Pair> treelist, double width, double length);
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
	public static class Rectangle {
		public Pair upperLeftCorner;
		public double width;
		public double length;
		
		public Rectangle(Pair upperLeftCorner, double width, double length) { this.upperLeftCorner = upperLeftCorner; this.width = width; this.length = length;}
		
		public boolean hasPair(Pair p) {
			if(p.x >= upperLeftCorner.x && p.x <= upperLeftCorner.x + width) {
				if(p.y >= upperLeftCorner.y && p.y <= upperLeftCorner.y + length) {
					return true;
				}
			}
			return false;
		}
		
		public String toString() {
			return "(" + upperLeftCorner.x + ", " + upperLeftCorner.y + ") " + width + "x" + length;
		}
	}
}