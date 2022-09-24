package discrete_time;

public class Packet {
	protected int slotCreation;
	protected int TASlotCreation;
	Packet(int currSlot){
		slotCreation=currSlot;
	}
	//for the case, when TV time is hidden in the first TA slot
	Packet(int currSlot, int currTASlot){
		slotCreation=currSlot;
		TASlotCreation=currTASlot;
	}
}
