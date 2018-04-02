
import java.util.ArrayList;
/**
 * A class which represents the receiver transport layer
 */
public class SenderTransport
{
    private NetworkLayer nl;
    private Timeline tl;
    private int n;
    private int mss;
    private boolean bufferingPackets;

    public SenderTransport(NetworkLayer nl){
        this.nl=nl;
        initialize();

    }

    public void initialize()
    {
    }

    public void sendMessage(Message msg)
    {
    }

    public void receiveMessage(Packet pkt)
    {
    }

    public void timerExpired()
    { 
    }

    public void setTimeLine(Timeline tl)
    {
        this.tl=tl;
    }

    public void setWindowSize(int n)
    {
        this.n=n;
    }
    
    public void setMSS(int n)
    {
        this.n=n;
    }
    

    public void setProtocol(int n)
    {
        if(n>0)
            bufferingPackets=true;
        else
            bufferingPackets=false;
    }

}
