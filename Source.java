package discrete_time;

import java.util.Random;

public class Source {
	double lambda;
	Random genRandom;
	int[][] arrivals;
	Source(double lambda_){
		lambda=lambda_;
		int seed=0;
		genRandom = new Random(seed);
		arrivals=new int[Helper.TA+Helper.TV][20];
	}

	int getPoisson() {
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;
		do {
		    k++;
		    p *= genRandom.nextDouble();
		} while (p > L);
		return k - 1;
	}	

   /**
    * exemplary distribution with mean value = lambda
    */
   int getNewDistr() {
		double alfa=lambda/20;
		double alfa0=1-10*alfa;
		double alfa1=alfa0+4*alfa;
		double alfa2=alfa1+3*alfa;
		double alfa3=alfa2+2*alfa;
		double gen=genRandom.nextDouble();
		if (gen>alfa3)
			return 4;
		if (gen>alfa2)
			return 3;
		if (gen>alfa1)
			return 2;
		if (gen>alfa0)
			return 1;
		return 0;
	}	
   
	// generates 0 or 1	
	int getTwoPoints() {
		double gen=genRandom.nextDouble();
		if (gen<lambda)
			return 1;
		return 0;
	}	
	
	int getGeo() {
		double p = 0.2308; // mean number of generated packets = 0.3
		int k = 0;
		double gen = 0;
		do {
		    k++;
		    gen = genRandom.nextDouble();
		} while (p > gen);
		return k - 1;
	}	
}
