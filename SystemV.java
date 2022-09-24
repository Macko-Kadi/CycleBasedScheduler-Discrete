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
	int startSlot=300000; //czas rozbiegu
	int endSimSlot=3000000; //czas konca symulacji
	int tA;
	int tV;
	int cycle;
	int cycleSlot; //ktory slot w ramach cyklu
	Packet packetInService;
	int[] timesQueDist;
	int[] timesSysDist;
	int[] queuesDist;
	int[] systemDist;
	int[][] queuesDistSlot;
	int[][] systemDistSlot;
	int[][] systemTimeDistSlot;
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
		systemDist= new int[bufferSize+2];
		timesQueDist = new int[bufferSize+1];
		timesSysDist = new int[bufferSize+2];
		queuesDistSlot= new int[cycle][bufferSize+1];
		systemDistSlot= new int[cycle][bufferSize+2];
		systemTimeDistSlot= new int[cycle][bufferSize+2];
		//for the case, when TV time is hidden in the first TA slot
		TAsystemTimeDistSlot=new int[tA][bufferSize+2];
		TAtimesSysDist= new int[bufferSize+2];
		TAsystemDistSlot= new int[tA][bufferSize+2];
		TAsystemDist= new int[bufferSize+2];
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
	public void addToQueue(){
		int nrOfPackets=source.getPoisson();
		//exemplary distribution (independent in slots)
		//int nrOfPackets=source.getNewDistr();
		//exemplary distribution (with correlation and statistics)
	//	int nrOfPackets=source.getNewCorrDistr(cycleSlot);
		for (int i=0; i<nrOfPackets;i++)
			queueList.add(new Packet(currSlot, TAcurrSlot));
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
		//	System.out.println("slot: " + cycleSlot + " queueList.size(): "+queueList.size()+" inService: "+ packetInService + " systemSize: "+systemSize);
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
				
				
							
				//different scheme of collecting:
				//1. delays of packet that service finished at the n-th slot)
				//int TAslotThatEnds=(TAcurrSlot-1)%tA;	
				//TAsystemTimeDistSlot[TAslotThatEnds][TAtimeSpendInSystem]++;	
				//2. delays of packet that arrive at the n-th slot)
				int TAslot=packetInService.TASlotCreation%tA;
				TAsystemTimeDistSlot[TAslot][TAtimeSpendInSystem]++;	
			//	System.out.println(" currslot: "+currSlot+" Pkt created: " + packetInService.slotCreation+ " (only TA): "+packetInService.TASlotCreation+" out: " +timeSpendInSystem + " (in TA): " +TAtimeSpendInSystem+" TAcurrSlot "+TAcurrSlot);
			//	System.out.println("Pkt created: " + packetInService.slotCreation%cycle+ " (only TA): "+packetInService.TASlotCreation%tA+" out: " +timeSpendInSystem + " (in TA): " +TAtimeSpendInSystem);
				
		//		System.out.println(packetInService.slotCreation%cycle+ "  " +timeSpendInSystem + "  " +TAtimeSpendInSystem);
				
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
			addToQueue();
			if (cycleSlot<tA){
				startService(doCollectStats);
			}
			//System.out.println("cycleSlot: "+cycleSlot +" TASlot: "+TAcurrSlot%tA);
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
		//Helper.printLength(systemTimeDistSlot,15);
		
		//arrival probabilities
		double [][] arrivalDistr=Helper.normalizeDistr2D(source.arrivals);
		
		int howManyValues=10;
		//Excell friendly format
		/*
		System.out.println("----time spend in system----");
		System.out.println(Helper.print1DExcell(timeS, howManyValues));
		System.out.println("----nr in system----");
		System.out.println(Helper.print1DExcell(distS, howManyValues));
		System.out.println("----nr in system (per slot)----");
		System.out.println(Helper.print2DExcell(distSlotS, howManyValues));
		System.out.println("----time in queue----");
		System.out.println(Helper.print1DExcell(timeQ, howManyValues));
		System.out.println("----nr in queue----");
		System.out.println(Helper.print1DExcell(distQ, howManyValues));
		System.out.println("----nr in queue (per slot)----");
		System.out.println(Helper.print2DExcell(distSlotQ, howManyValues));
		System.out.println("----waiting time distribution (when service finished in the n-th slot)----");
		System.out.println(Helper.print2DExcell(distTimePerSlot, howManyValues));		
		System.out.println("----nr in system (for the case, when TV time is hidden in the first TA slot)----");
		System.out.println(Helper.print1DExcell(TAdistS, howManyValues));
		System.out.println("----nr in system (for the case, when TV time is hidden in the first TA slot) (per slot)----");
		System.out.println(Helper.print2DExcell(TAdistSlotS, howManyValues));
		System.out.println("----time spend in system (for the case, when TV time is hidden in the first TA slot)----");
		System.out.println(Helper.print1DExcell(TAtimeS, howManyValues));
		System.out.println("----waiting time distribution (of packets that arrive during n-th slot) (for the case, when TV time is hidden in the first TA slot) ----");
		System.out.println(Helper.print2DExcell(TAdistTimePerSlot, howManyValues));	
		*/
		
		//Matlab friendly format
		System.out.println("----time spend in system----");
		System.out.println(Helper.print1D(timeS, howManyValues));
		System.out.println("----nr in system----");
		System.out.println(Helper.print1D(distS, howManyValues));
		System.out.println("----nr in system (per slot)----");
		System.out.println(Helper.print2D(distSlotS, howManyValues));
		System.out.println("----time in queue----");
		System.out.println(Helper.print1D(timeQ, howManyValues));
		System.out.println("----nr in queue----");
		System.out.println(Helper.print1D(distQ, howManyValues));
		System.out.println("----nr in queue (per slot)----");
		System.out.println(Helper.print2D(distSlotQ, howManyValues));
		System.out.println("----waiting time distribution (when service finished in the n-th slot)----");
		System.out.println(Helper.print2D(distTimePerSlot, howManyValues));
		//arrival probabilities
		System.out.println("----arrival probabilities----");
		System.out.println(Helper.print2D(arrivalDistr, howManyValues));
		}
	public static void main(String[] args) {
		SystemV system = new SystemV(1,0,0.4,350);
		system.run();
	
	}
}
