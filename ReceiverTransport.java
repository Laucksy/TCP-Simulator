import java.util.*;

/**
 * A class which represents the receiver transport layer
 */
public class ReceiverTransport {
  private ReceiverApplication ra;
  private NetworkLayer nl;
  private boolean bufferingPackets;
  private List<Packet> buffer;
  private int maxBufferLength;
  private int expectedSeqnum; //next seqnum to be received by transport layer
  private int seqnum;
  private int lastRead; //next seqnum to be drawn by application layer

  public ReceiverTransport(NetworkLayer nl) {
    ra = new ReceiverApplication();
    this.nl = nl;
    initialize();
  }

  public void initialize() {
    this.bufferingPackets = false;
    this.buffer = new ArrayList<Packet>();
    this.maxBufferLength = 100;
    this.expectedSeqnum = 0;
    this.lastRead = 0;
    this.seqnum = 0;
  }

  public void receiveMessage(Packet pkt) {
    Message msg = new Message("");
    String status = "";

    if (!pkt.isCorrupt() && pkt.getSeqnum() == expectedSeqnum) {
      // If not corrupt and in order, add to buffer and set new expected seqnum
      status = "|\t\033[0;32mSTATUS:\t\t\tGOOD\033[0m\t\t\t\t\t|";

      expectedSeqnum = addToBuffer(pkt);
    } else if (!pkt.isCorrupt()) {
      // If not corrupt and out of order, add to buffer
      status = "|\t\033[0;32mSTATUS:\t\t\tOUT OF ORDER\033[0m\t\t\t\t|";
      if (bufferingPackets) {
        addToBuffer(pkt);
      }
    } else {
      // If corrupt
      status = "|\t\033[0;32mSTATUS:\t\t\tCORRUPT\033[0m\t\t\t\t\t|";
    }

    // Generate ACK and send it
    Packet ack = new Packet(msg, seqnum, expectedSeqnum, 0, true);
    int sum = 0;
    for (int i = 0; i < buffer.size(); i++) {
      sum += buffer.get(i).getMessage().length();
    }
    ack.setRcvwnd(maxBufferLength - sum);
    if (NetworkSimulator.DEBUG >= 1) {
      // Print out data about packet to the console
      System.out.println(" --- \033[0;32mReceived packet\033[0m --------------------------------------------------- ");
      System.out.println(pkt);
      System.out.println(status);
      System.out.println(" ----------------------------------------------------------------------- \n");
      System.out.println(" --- \033[0;32mSending ACK\033[0m ------------------------------------------------------- ");
      System.out.println(ack);
      System.out.println(" ----------------------------------------------------------------------- \n");
    }

    nl.sendPacket(ack, Event.SENDER);

  }

  public int addToBuffer(Packet pkt) {
    System.out.println("Buffer " + buffer.size());
    for (int i = 0; i < buffer.size(); i++) {
      System.out.println(buffer.get(i).getSeqnum());
    }

    int sum = 0;
    boolean found = false;
    for (int i = 0; i < buffer.size(); i++) {
      if (buffer.get(i).getSeqnum() == pkt.getSeqnum()) found = true;
      sum += buffer.get(i).getMessage().length();
    }
    if (!found && sum + pkt.getMessage().length() < maxBufferLength) {
      buffer.add(pkt);
      return lastReceived();
    } else {
      return expectedSeqnum;
    }
  }

  public void sortBuffer() {
    Collections.sort(buffer, new Comparator<Packet>() {
      @Override
      public int compare(Packet p1, Packet p2) {
        return p1.getSeqnum() - p2.getSeqnum();
      }
    });
  }

  public int lastReceived() {
    sortBuffer();
    int lastRcvd = expectedSeqnum;
    int index = 0;
    System.out.println("LastReceived," + expectedSeqnum);
    for (int i = 0; i < buffer.size(); i++) {
      System.out.println("Buffer " + i + ": " + buffer.get(i).getSeqnum());
    }
    while (index < buffer.size() && lastRcvd > buffer.get(index).getSeqnum()) index++;
    while (index < buffer.size() && lastRcvd == buffer.get(index).getSeqnum()) {
      lastRcvd += buffer.get(index).getMessage().byteLength();
      index++;
    }
    return lastRcvd;
  }

  public void popBuffer() {
    sortBuffer();
    if (buffer.size() > 0 && buffer.get(0).getSeqnum() == lastRead) {
      lastRead += buffer.get(0).getMessage().length();
      ra.receiveMessage(buffer.remove(0).getMessage());
    }
  }

  public void setBufferSize(int n) {
    maxBufferLength = n;
  }

  public void setProtocol(int n) {
    if(n > 0)
        bufferingPackets = true;
    else
        bufferingPackets = false;
  }
}
