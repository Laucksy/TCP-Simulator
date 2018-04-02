import java.util.*;
import java.io.FileReader;

public class NetworkSimulator
{
    public static int DEBUG;
    /**
     * Main method with follwing variables
     * @param args[0] file with messages
     * @param args[1] time between messages
     * @param args[2] loss probability
     * @param args[3] curroption probability
     * @param args[4] window Size
     * @param args[5] mss
     * @param args[6] Protocol (buffering or nno buffering of out of order packets)
     * @param args[7] Debugging flag
     */
    public static void main(String[] args)
    {
        //current event to process
        Event currentEvent;
        //checking to see if enough arguements have been sent    
        if(args.length<7)
        {
            System.out.println("need at least 5 arguements");
            System.exit(1);
        }
        //reading in file line by line. Each line will be one message
        ArrayList<String> messageArray = readFile(args[0]);
        //creating a new timeline with an average time between packets.
        Timeline tl = new Timeline(Integer.parseInt(args[1]), messageArray.size());
        //creating a new network layer with specific loss and curroption probability.
        NetworkLayer nl = new NetworkLayer(Float.parseFloat(args[2]),Float.parseFloat(args[3]),tl);
        SenderApplication sa = new SenderApplication(messageArray,nl);
        SenderTransport st = sa.getSenderTransport();
        //sender and receiver transport needs access to timeline to set timer.
        st.setTimeLine(tl);
        ReceiverTransport rt = new ReceiverTransport(nl);
        //setting window size
        st.setWindowSize(Integer.parseInt(args[4]));
        st.setMSS(Integer.parseInt(args[5]));
        //setting protocol type
        st.setProtocol(Integer.parseInt(args[6]));
        rt.setProtocol(Integer.parseInt(args[6]));
        DEBUG = Integer.parseInt(args[7]);
        //this loop will run while there are events in the priority queue
        while(true)
        {
            //get next event
            currentEvent = tl.returnNextEvent();
            //if no event present, break out
            if(currentEvent==null)
                break;
            //if event is time to send a message, call the send message function of the sender application.   
            if(currentEvent.getType()==Event.MESSAGESEND)
            {
                sa.sendMessage();
                if(DEBUG>0)
                    System.out.println("Message sent from sender to receiver at time " + currentEvent.getTime());   
            }
            //if event is a message arrival
            else if (currentEvent.getType()==Event.MESSAGEARRIVE)
            {
                //if it arrives at the sender, call the get packet from the sender
                if(currentEvent.getHost()==Event.SENDER){
                    if(DEBUG>0)
                        System.out.println("Message arriving from receiver to sender at time " + currentEvent.getTime()); 
                    st.receiveMessage(currentEvent.getPacket());
                }  
                //if it arrives at the receiver, call the get packet from the receiver
                else{
                    if(DEBUG>0)
                        System.out.println("Message arriving from sender to receiver at time " + currentEvent.getTime());
                    rt.receiveMessage(currentEvent.getPacket());
                }
            }
            //If event is an expired timer, call the timerExpired method in the sender transport.
            else if (currentEvent.getType()==Event.TIMER)
            {
                if(DEBUG>0)
                    System.out.println("Timer expired at time " + currentEvent.getTime());

                tl.stopTimer();
                st.timerExpired();
            }
            else if (currentEvent.getType()==Event.KILLEDTIMER)
            {//do nothing if it is just a turned off timer.
            }
            //this should not happen.
            else
            {
                System.out.println("Unidentified event type!");
                System.exit(1);
            }

        }
    }

    //reading in file line by line.
    public static ArrayList<String> readFile(String fileName)
    {
        ArrayList<String> messageArray = new ArrayList<String>();
        Scanner sc=null;
        try{
            sc = new Scanner(new FileReader(fileName));
        }catch(Exception e)
        {System.out.println("Could not open file " + e);}

        while(sc.hasNextLine())
            messageArray.add(sc.nextLine());
        return messageArray;
    }

}
