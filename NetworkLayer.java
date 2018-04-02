import java.util.*;
/**
 * A class which represents the transoprt layer for both sender and receiver.
 */

public class NetworkLayer
{
    float lossProbability; //probablity of losing a packet
    float currProbability; //probability of curropting a packet
    Timeline tl; 
    Random ran; //random number generator for losing packets.

    public NetworkLayer(float lp, float cp,Timeline tl)
    {
        lossProbability=lp;
        currProbability=cp;
        this.tl=tl;
        ran = new Random();
    }

    //sending packet if it is not lost, and curropting it if necessary.
    public void sendPacket(Packet pkt, int to)
    {
        if(ran.nextDouble()<lossProbability)
        {
            if(NetworkSimulator.DEBUG>1)
                System.out.println("Packet seq:" + pkt.getSeqnum() + " ack: " + pkt.getAcknum() + " lost");
            return;    
        }
        if(ran.nextDouble()<currProbability)
        {
            if(NetworkSimulator.DEBUG>1)
                System.out.println("Packet seq:" + pkt.getSeqnum() + " ack: " + pkt.getAcknum() + " curropted");
            pkt.corrupt();
        }
        if(NetworkSimulator.DEBUG>1)
            System.out.println("Packet seq:" + pkt.getSeqnum() + " ack: " + pkt.getAcknum() + " sent");
        tl.createArriveEvent(pkt,to);
    }

}
