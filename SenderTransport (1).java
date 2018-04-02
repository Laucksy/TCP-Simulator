
import java.util.LinkedList;
/**
 * Write a description of class SenderTransport here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class SenderTransport
{
    NetworkLayer nl;
    Timeline tl;
    int seqNum;
    LinkedList<Message> buffer;
    
    public SenderTransport(NetworkLayer nl){
        this.nl=nl;
        seqNum=0;
        buffer=new LinkedList<Message>();
    }
    
    public void sendMessage(Message msg)
    {
        Packet toSend = new Packet(msg,seqNum++,0,0);
        nl.sendPacket(toSend,Event.RECEIVER);
        tl.startTimer(2);
        
        
        
    }
    
    public void setTimeLine(Timeline tl)
    {
        this.tl=tl;
    }
    public void receiveMessage(Packet pkt)
    {
    }
    
    
        public void timerExpired()
    { System.out.println("The timer has expired!");}
    
    
}
