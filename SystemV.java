package discrete_time;

import java.util.ArrayList;

/**
 * 
 * The project simulates a queueing system with vacations
 * The system works as follows: 
 * 
 * Discrete time points are considered - beginnings of particular slots.
 * At first, the service of a packet is finished.
 * A few packets come.
 * If it is active time and something is in a queue, a packet is taken to the service. 
 * 
 * @author macso
 *
 */
public class SystemV {
	Source source;
	int bufferSize;
	int currSlot;
	int TAcurrSlot;
	int startSlot=700000; //when start to collect data (it should be steady state) 
	int endSimSlot=53000000; 
	int tA;
	int tV;
	int cycle;
	int cycleSlot; 
	int nrOfPacketsArrived;
	int nrOfPacketsLost;
	Packet packetInService;
	int[] timesQueDist;
	int[] timesSysDist;
	int[] queuesDist;
	int[] systemDist;
	int[][] queuesDistSlot;
	int[][] systemDistSlot;
	int[][] systemTimeDistSlot;
	int[] arrivals;
	ArrayList<Packet> queueList;
	
	//for the case, when TV time is hidden in the first TA slot
	int[][] TAsystemTimeDistSlot;
	int[] TAtimesSysDist;
	int[] TAsystemDist;
	int[][] TAsystemDistSlot;
	
	SystemV(int tA_, int tV_, double lambda_, int bufferSize_){
		tA=tA_;
		tV=tV_;
		Helper.TA=tA;
		Helper.TV=tV;
		source=new Source(lambda_);
		bufferSize=bufferSize_;
				cycle=tA+tV;
		queueList = new ArrayList<Packet>();
		queuesDist= new int[bufferSize+1];
		systemDist= new int[bufferSize+1];
		timesQueDist = new int[200];
		timesSysDist = new int[200];
		arrivals = new int[200];
		queuesDistSlot= new int[cycle][bufferSize+1];
		systemDistSlot= new int[cycle][bufferSize+1];
		systemTimeDistSlot= new int[cycle][200];
		//for the case, when TV time is hidden in the first TA slot
		TAsystemTimeDistSlot=new int[tA][200];
		TAtimesSysDist= new int[200];
		TAsystemDistSlot= new int[tA][200];
		TAsystemDist= new int[200];
	}
	/*
	 * returns first packet from queue or null if queue is empty
	 */
	protected Packet getFromQueue(){
		if (queueList.size()>0){
			Packet P= queueList.get(0);
			queueList.remove(0);
			return P;
		}
		else 
			return null;
	}
	
	//SELECT DISTRIBUTION 
	public void addToQueue(boolean doCollectStats){
		int nrOfPackets=source.getPoisson();  	//poisson
	//	int nrOfPackets=source.getTwoPoints(); 	//two points, p(0)=1-lambda, p(1)=lambda
	//	int nrOfPackets=source.getGeo();  		// geometric (does not depend on lambda! Always mean=0.3)
    //  int nrOfPackets=source.getNewDistr(); 	// exemplary distribution, mean=lambda
		arrivals[nrOfPackets]+=1;
		if(doCollectStats) nrOfPacketsArrived+=nrOfPackets;
		for (int i=0; i<nrOfPackets;i++) {
			if(queueList.size()<bufferSize) 
				queueList.add(new Packet(currSlot, TAcurrSlot));
			else if(doCollectStats) nrOfPacketsLost+=1;			
		}
	}
	
	public void updateStats(boolean doCollectStats){
		int queueSize=queueList.size();
		int systemSize=queueSize;
		if (packetInService!=null) systemSize++;
		if(doCollectStats) {
			queuesDist[queueSize]++;
			queuesDistSlot[cycleSlot][queueSize]++;
			systemDist[systemSize]++;
			systemDistSlot[cycleSlot][systemSize]++;
			//for the case, when TV time is hidden in the first TA slot
			if (cycleSlot<tA){
				TAsystemDist[systemSize]++;
				TAsystemDistSlot[cycleSlot][systemSize]++;
			}
		}
	}
	
	public void endService(boolean doCollectStats){
		if (packetInService!=null){
			if(doCollectStats) {
				int timeSpendInSystem=currSlot-packetInService.slotCreation;
				timesSysDist[timeSpendInSystem]++;
				//for the case, when TV time is hidden in the first TA slot		
				int TAtimeSpendInSystem=TAcurrSlot-packetInService.TASlotCreation;
				TAtimesSysDist[TAtimeSpendInSystem]++;				
				//the distribution is related to the slot that ends (so the previous one)
				int slotThatEnds=(currSlot-1)%cycle;
				systemTimeDistSlot[slotThatEnds][timeSpendInSystem]++;		
				int TAslot=packetInService.TASlotCreation%tA;
				TAsystemTimeDistSlot[TAslot][TAtimeSpendInSystem]++;					
			}
		}
		packetInService=null;
	}
	
	public void startService(boolean doCollectStats){
		packetInService=getFromQueue();
		if (packetInService!=null){
			if (doCollectStats) timesQueDist[currSlot-packetInService.slotCreation]++;
		}
	}
	
	public static double roundDouble(double x, int decimalPlaces){
		double result=x*Math.pow(10, decimalPlaces);
		long l=(long)Math.round(result);
		result=l/Math.pow(10, decimalPlaces);
		return result;
	}
	public void run(){
		currSlot=0;
		boolean doCollectStats=false;
		for (int simSlot=1;simSlot<endSimSlot;simSlot++){
			currSlot=simSlot;
			cycleSlot=currSlot%cycle;
			if (cycleSlot<=tA && cycleSlot>0)
				TAcurrSlot++;
			if (simSlot==startSlot){
					doCollectStats=true;
					Helper.doCollectStats=true;
			}
			endService(doCollectStats);
			addToQueue(doCollectStats);
			//at first add to queue, then -> to service
			if (cycleSlot<tA){
				startService(doCollectStats);
			}
			updateStats(doCollectStats);		
		}
		double [] timeS=Helper.normalizeDistr1D(timesSysDist);
		double [] distS=Helper.normalizeDistr1D(systemDist);
		double [] timeQ=Helper.normalizeDistr1D(timesQueDist);
		double [] distQ=Helper.normalizeDistr1D(queuesDist);
		double [][] distSlotQ=Helper.normalizeDistr2D(queuesDistSlot);
		double [][] distSlotS=Helper.normalizeDistr2D(systemDistSlot);
		//waiting time distribution of packets which service finished at the end of i-th slot
		double [][] distTimePerSlot=Helper.normalizeDistr2D(systemTimeDistSlot);
		//for the case, when TV time is hidden in the first TA slot
		double [][] TAdistTimePerSlot=Helper.normalizeDistr2D(TAsystemTimeDistSlot);
		double [] TAtimeS=Helper.normalizeDistr1D(TAtimesSysDist);
		double [][] TAdistSlotS=Helper.normalizeDistr2D(TAsystemDistSlot);
		double [] TAdistS=Helper.normalizeDistr1D(TAsystemDist);
		double [] distArrival=Helper.normalizeDistr1D(arrivals);
		double ploss=(double)nrOfPacketsLost / (double)nrOfPacketsArrived;

		//arrival probabilities
		double [][] arrivalDistr=Helper.normalizeDistr2D(source.arrivals);
		
		int howManyValues=6;
		//Excell friendly format
		System.out.println("----time in system----");
		System.out.println(Helper.print1DExcell(timeS, howManyValues));
		System.out.println("----nr in system----");
		System.out.println(Helper.print1DExcell(distS, howManyValues));
		System.out.println("----nr in system (per slot)----");
		System.out.println(Helper.print2DExcell(distSlotS, howManyValues));
		System.out.println("----mean nr in system (per slot)----");
		System.out.println(Helper.print1DExcell(Helper.getMean(distSlotS), 10));
		System.out.println("----time in queue----");
		System.out.println(Helper.print1DExcell(timeQ, howManyValues));
		System.out.println("----nr in queue----");
		System.out.println(Helper.print1DExcell(distQ, howManyValues));
		System.out.println("----nr in queue (per slot)----");
		System.out.println(Helper.print2DExcell(distSlotQ, howManyValues));
		System.out.println("----mean nr in queue (per slot)----");
		System.out.println(Helper.print1DExcell(Helper.getMean(distSlotQ), 10));
		System.out.println("----ploss----");
		System.out.println(Helper.roundDouble(ploss,4));
	//	System.out.println("---sojourn time distribution (when service finished in the n-th slot)----");
	///	System.out.println(Helper.print2DExcell(distTimePerSlot, 5*howManyValues));		
	//	System.out.println("----nr in system (for the case, when TV time is hidden in the first TA slot)----");
	//	System.out.println(Helper.print1DExcell(TAdistS, howManyValues));
	//	System.out.println("----nr in system (for the case, when TV time is hidden in the first TA slot) (per slot)----");
	//	System.out.println(Helper.print2DExcell(TAdistSlotS, howManyValues));
	//	System.out.println("----time spend in system (for the case, when TV time is hidden in the first TA slot)----");
	//	System.out.println(Helper.print1DExcell(TAtimeS, howManyValues));
	//	System.out.println("----waiting time distribution (of packets that arrive during n-th slot) (for the case, when TV time is hidden in the first TA slot) ----");
	//	System.out.println(Helper.print2DExcell(TAdistTimePerSlot, 2*howManyValues));	
	//	System.out.println("-----arrival distribution-----");
	//	System.out.println(Helper.print1DExcell(distArrival, howManyValues));
		
		
		//Matlab friendly format
		System.out.println("----time spend in system----");
		System.out.println(Helper.print1D(timeS, 20));
//		System.out.println("----nr in system----");
//		System.out.println(Helper.print1D(distS, howManyValues));
		System.out.println("----nr in system (per slot)----");
		System.out.println(Helper.print2D(distSlotS, howManyValues));
//		System.out.println("----time in queue----");
//		System.out.println(Helper.print1D(timeQ, howManyValues));
//		System.out.println("----nr in queue----");
//		System.out.println(Helper.print1D(distQ, howManyValues));
		System.out.println("----nr in queue (per slot)----");
		System.out.println(Helper.print2D(distSlotQ, howManyValues-1));
//		System.out.println("----waiting time distribution (when service finished in the n-th slot)----");
//		System.out.println(Helper.print2D(distTimePerSlot, howManyValues));
//		//arrival probabilities
//		System.out.println("----arrival probabilities----");
//		System.out.println(Helper.print2D(arrivalDistr, howManyValues));
		}
	public static void main(String[] args) {
		SystemV system = new SystemV(5,10,0.4,15);
		system.run();
	
	}
}
