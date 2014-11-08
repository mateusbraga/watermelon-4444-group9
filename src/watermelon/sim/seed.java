package watermelon.sim;

import java.util.ArrayList;


public class seed {
    public double x;
    public double y;
    public boolean tetraploid;
    public double score;
    
    public seed() { x = 0.0; y = 0.0; tetraploid = false; }

    public seed(double xx, double yy, boolean tetra) {
        x = xx;
        y = yy;
        tetraploid = tetra;
       
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		seed other = (seed) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}
}