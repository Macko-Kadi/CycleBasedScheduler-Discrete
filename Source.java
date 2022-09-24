package discrete_time;

import java.util.Random;

public class Source {
	double lambda;
	Random genRandom;
	double residual; //residual time for getNewCorrDistr function
	int[][] arrivals;
	Source(double lambda_){
		lambda=lambda_;
		int seed=0;
		genRandom = new Random(seed);
		arrivals=new int[Helper.TA+Helper.TV][20];
	}
	/*
	 * generates number of packets that came during one slot
	 */
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
	 * Generates number of packets that arrive in one time slot, 
	 * Distance given by uniform distribution, or modified uniform distribution (if you want to have different distributions in different slots)
	 * 
	 * @param cycleSlot
	 * @return
	*/
	/*
	int getNewCorrDistr(int cycleSlot) {
		double a=0;
		double b=Helper.TA;		
		//uncomment below line if you want to have different distributions in different slots
		//b=b+cycleSlot;
		
		int k=0;
		double p=0;
		//uncomment bellow if correlation
		//no correlation between cycles, but corelation between slots
		//if(cycleSlot==0){
		//	residual=0;	
		//}	

		if (residual>1){
			residual=residual-1;
			if (Helper.doCollectStats){
				arrivals[cycleSlot][0]++;
			}
			return 0;
		}
		
		if (residual<1 && residual!=0){
			p=residual;
			k++;
		}
		
		////end of correlation block
		do {
		    k++;
		    p += a+b*genRandom.nextDouble();
		} while (p < 1) ;
		
		residual=p-1; //when next packet will arrive	
		
		if (Helper.doCollectStats){
			arrivals[cycleSlot][k-1]++;
		}
		return k-1;
	}	*/

	int getNewCorrDistr(int cycleSlot) {
	//	double L = Math.exp(-lambda*(1+(double)(cycleSlot)/(Helper.TA+Helper.TV))); // with different slots
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;
		do {
		    k++;
		    p *= genRandom.nextDouble();
		} while (p > L);
		if (Helper.doCollectStats){
			arrivals[cycleSlot][k-1]++;
		}
		return k-1;
	}
	
	int getNewDistr() {
		double alfa=lambda/20;
		//rozk³ad prawdopodobienstwa o wartosci oczekiwanej lambda o postaci
		//0 : 1-8*a
		//1 : 4a
		//2 : 3a
		//3 : 2a
		//4 :  a
		
		double alfa0=1-10*alfa;
		double alfa1=alfa0+4*alfa;
		double alfa2=alfa1+3*alfa;
		double alfa3=alfa2+2*alfa;
		double alfa4=alfa3+alfa;
//System.out.println(alfa0+" "+ alfa1+" "+alfa2+" "+ alfa3+" "+alfa4);
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
}
