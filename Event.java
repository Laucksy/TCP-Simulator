/**
 * A class which represents an event and is used in the Timeline class
 */
public class Event implements Comparable<Event>
{

    int time; //time of the event
    int type; //type of event
    int host; //host where event is happening
    Packet pkt; //pkt related to event if relevant (only arrive events)

    public static int MESSAGESEND = 0;  
    public static int MESSAGEARRIVE=1;
    public static int TIMER = 2;
    public static int KILLEDTIMER=3;
    public static int SENDER = 0;
    public static int RECEIVER=1;

    /**
     * Initializing new event without packet. This will be used for sending events
     * @param time time of event
     * @param type type of event
     * @param host where event is happening.
     */
    public Event(int time, int type, int host)
    {
        this.time=time;
        this.type=type;
        this.host=host;
    }

    /**
     * Initializing new event with packet. This will be used for arriving events
     * @param time time of event
     * @param type type of event
     * @param host where event is happening.
     * @param pkt packet that is arriving.
     */

    public Event(int time, int type, int host, Packet pkt)
    {
        this.time=time;
        this.type=type;
        this.host=host;
        this.pkt=pkt;
    }
    
    /**
     * Kills timer by simply setting its event type to KILLEDTIMER
     */

    public void killTimer()
    {
        if(type!=TIMER)
        {
            System.out.println("Trying to stop a timer on an event that is not a timer! should not happen!");
            System.exit(1);
        }
        type=KILLEDTIMER;
    }
    

    public int getTime()
    {
        return time;
    }

    public int getType()
    {
        return type;
    }

    public int getHost()
    {
        return host;
    }

    public Packet getPacket()
    {
        return pkt;
    }

    public int compareTo(Event e)
    {
        return this.time-e.time;

    }

}
