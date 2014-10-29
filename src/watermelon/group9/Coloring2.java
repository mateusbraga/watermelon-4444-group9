package watermelon.group9;

import java.util.ArrayList;

import watermelon.sim.seed;


public class Coloring{
	double w[][];
	Boolean subset[];
	Boolean best_subset[];
	int seedSize;
	double max_w; // the max sum of edge valules
	double sum_w; // the current sum of edge values
	int times = 0;
	//static int max_times = 6000000; // max run times for search algorithm
	
    public Coloring(int n) {
    	System.out.println("Constructor Function: create array");
    	w 			= new double[n][n];
    	subset 		= new Boolean[n];
    	best_subset = new Boolean[n];
    	seedSize 	= n;
    }
	
    void init()
    {
    	System.out.println("init value for coloring");
    	for(int i = 0; i < seedSize; i++)
			subset[i] = false;
    	max_w = 0;
    	sum_w = 0;
    	times = 0;
    }
    
    public void calMatrix(ArrayList<seed> seedList)
    {
    	System.out.println("Calculate Matrix");
    	int n = seedList.size();
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
    				
    				//System.out.println("i: " + i + ", sum is: " + sum);
    				for(int j = 0; j < n; j++)
    				{
    					w[i][j] = w[i][j]/sum;
    				}
    			}
    }
    
	static double distance(seed tmp, seed pair) {
		return Math.sqrt((tmp.x - pair.x) * (tmp.x - pair.x) + (tmp.y - pair.y) * (tmp.y - pair.y));
	}
	
	public void colorRandomly(ArrayList<seed> seedList) {
		System.out.println("Enter colorRandomly");
		init();
		calMatrix(seedList);
		int n = seedList.size();
		
		int time = 100000;
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
	
	public void colorRandomly2(ArrayList<seed> seedList) {
		System.out.println("Enter colorRandomly");
		init();
		calMatrix(seedList);
		int n = seedList.size();
		
		int time = 100000;
		while(time-- != 0)
		{
			int x = (int)(Math.random()*n); // Does the random function in java cause many duplicates?
			subset[x] = !subset[x]; // change the label of x
			double tmp = sum_w;
			for(int i = 0; i < n; i++)
			{
				if(subset[i] != subset[x]) // i and x are in different classifications now
				{
					tmp += w[i][x];
					tmp += w[x][i];
				}
				
				if(i != x &&  subset[i] == subset[x])
				{
					tmp -= w[i][x];
					tmp -= w[x][i];
				}
			}
			
			if(max_w < tmp)
			{
				max_w = tmp;
			    for(int i = 0; i < n; i++)
			    {
			    	best_subset[i] = subset[i];
			    }	
			}
			if(tmp < sum_w)
				subset[x] = !subset[x];
			else
				sum_w = tmp;
		}
		
		for(int i = 0; i < n; i++)
		{
			if(best_subset[i] == false)
				seedList.get(i).tetraploid = false;
			else
				seedList.get(i).tetraploid = true;
				
		}
	}
	
	public void dfs(int cur, double cost, int sub)
	{
		if(cur >= seedSize /*|| times > max_times*/)
			return;
		if(cur >= (sub+1) * 25)
			return;
		
		subset[cur] = true;
		
		double tmp = cost;
		for(int i = 0; i < seedSize; i++)
		{
			if(subset[i] != subset[cur]) // i and cur are in different classifications now
			{
				tmp += w[i][cur];
				tmp += w[cur][i];
			}
			
			if(i != cur &&  subset[i] == subset[cur])
			{
				tmp -= w[i][cur];
				tmp -= w[cur][i];
			}
		}
		
		if(tmp > max_w)
		{
			max_w = tmp;
			for(int i = 0; i < seedSize; i++)
		    {
		    	best_subset[i] = subset[i];
		    }	
		}
		if(tmp > cost)
		{
			for(int i = cur + 1; i < seedSize; i++)
			{
				dfs(i, tmp, sub);
				subset[i] = false;
			}
		}
	}
	
	
	public void dfs(int cur, double cost)
	{
		//times = times + 1;
		//System.out.println("times: " + times);
		if(cur >= seedSize /*|| times > max_times*/)
			return;
		subset[cur] = true;
		
		double tmp = cost;
		for(int i = 0; i < seedSize; i++)
		{
			if(subset[i] != subset[cur]) // i and cur are in different classifications now
			{
				tmp += w[i][cur];
				tmp += w[cur][i];
			}
			
			if(i != cur &&  subset[i] == subset[cur])
			{
				tmp -= w[i][cur];
				tmp -= w[cur][i];
			}
		}
		
		if(tmp > max_w)
		{
			max_w = tmp;
			for(int i = 0; i < seedSize; i++)
		    {
		    	best_subset[i] = subset[i];
		    }	
		}
		if(tmp > cost)
		{
			for(int i = cur + 1; i < seedSize; i++)
			{
				dfs(i, tmp);
				subset[i] = false;
			}
		}
	}
	
	public void colorSearch(ArrayList<seed> seedList) {
		System.out.println("Enter colorSearch");
		init();
		calMatrix(seedList);
		dfs(0, 0);
		for(int i = 0; i < seedSize; i++)
		{
			if(best_subset[i] == false)
				seedList.get(i).tetraploid = false;
			else
				seedList.get(i).tetraploid = true;
		}
	}
	
	public void colorSearchDivide(ArrayList<seed> seedList) {
		System.out.println("Enter colorSearch");
		init();
		calMatrix(seedList);
		int subnum = (int) Math.ceil(seedSize / 25.0);
		for(int i = 0; i < subnum; i++)
		{
			System.out.println("sub is " + i);
			dfs(25 * i, 0, i);
		}
		//dfs(0, 0);
		for(int i = 0; i < seedSize; i++)
		{
			if(best_subset[i] == false)
				seedList.get(i).tetraploid = false;
			else
				seedList.get(i).tetraploid = true;
		}
	}
}
