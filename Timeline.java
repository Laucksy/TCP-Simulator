

import java.util.*;
/**
 * This class represents the timeline of events in a priority queue
 */

public class Timeline
{
    private PriorityQueue<Event> events; //timeline of events. 
    private int timeBetweenSends; //Avg. time between two packets being sent
    private int totalMessagesToSend; //total number of messages to send
    private int sentSoFar; //number fo messages sent so far 
    private int timeSoFar; // time which has passed so far
    private Random ran; //random number generator
    private int lastArrivalTime;  //last arrival time so far
    private Event timerPointer; //pointer to currently running timer
    
    
    /**
     * A constructor to initialize variables.
     */
    public Timeline(int time, int numOfMessages)
    {
        events = new PriorityQueue<Event>();
        timeBetweenSends=time;
        totalMessagesToSend = numOfMessages;
        ran=new Random();
        timeSoFar=0;
        sentSoFar=1; //set to one because we send the fisrt packet right away
        lastArrivalTime=0;
        createSendEvent();//sengin first packet
        timerPointer=null;

    }
    /**
     * Getting next event. It it is a send event, and there are still messages that need to be sent, sending the next one.
     */

    public Event returnNextEvent()
    {
        Event tmp = events.poll();
        if(tmp==null)
            return tmp;
        timeSoFar = tmp.getTime();    
        if(tmp.getType()==Event.MESSAGESEND && sentSoFar<totalMessagesToSend)
        {
            createSendEvent();
            sentSoFar++;
        }

        return tmp;
    }
    
    /**
     * Creating a send event.First generating a random enumber using the exponential distribution with average timeBetweenSends and then adding the event.
     */

    public void createSendEvent()
    {
        double tmp = ran.nextFloat();
        tmp=(tmp==0)?0.00001:tmp;
        int time = (int)(timeBetweenSends*(-Math.log(tmp))+timeSoFar);
        if(NetworkSimulator.DEBUG>2)
            System.out.println("inserting fututre send event at " + timeSoFar + " with time: " + time );
        events.add(new Event(time,Event.MESSAGESEND,Event.SENDER));
    }

    /**
     * Creating an arrive event. This first checks for the last arrival time (since packets cannot be reordered), and the adds a random number uniformly distributed from 1-9
     * to calculate the time of the arrival event. It then adds the event to the queue.
     * @param pkt packet that will arrive
     * @param to who are we sending the packet to
     */
    
    public void createArriveEvent(Packet pkt, int to)
    {
        int newArrivalTime=timeSoFar+(int)(ran.nextGaussian()*2+10);
        lastArrivalTime = Math.max(newArrivalTime,lastArrivalTime+1);

        if(NetworkSimulator.DEBUG>2)
        {
            String tmp = (to==Event.SENDER)? "sender" : "receiver";
            System.out.println("inserting futurre arrive event at " + timeSoFar + " with time: " + lastArrivalTime + "to :" +tmp);
        }
        events.add(new Event(lastArrivalTime,Event.MESSAGEARRIVE,to,pkt));

    }
    
    /**
     * Starting timer.If it si already started it prints out an error message. setting timerPointer to point at timer event.
     * @ param increment timeout for timer
     */
    public void startTimer(int increment)
    {
        if(timerPointer!=null)
        {
            System.out.println("Timer is allready on!");
            return;
        }
        timerPointer = new Event(timeSoFar+increment,Event.TIMER,Event.SENDER);
        events.add(timerPointer);
        if(NetworkSimulator.DEBUG>2)
            System.out.println("inserting future timer event at time: " + timeSoFar + " for " +  increment);

    }
    /**
     * Kills timer and sets timerPointer to null
     */

    public void stopTimer()
    {
        if(timerPointer==null)
        {
            System.out.println("Timer is not on!");
            return;
        }     
        
        timerPointer.killTimer();
        timerPointer=null;
    }

    
}
