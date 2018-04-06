import java.util.*;

/**
 * A class which represents the receiver transport layer
 */
public class ReceiverTransport {
  private ReceiverApplication ra;
  private NetworkLayer nl;
  private boolean bufferingPackets;
  private List<Packet> buffer;
  private int expectedSeqnum;
  private int lastRead;

  public ReceiverTransport(NetworkLayer nl) {
    ra = new ReceiverApplication();
    this.nl = nl;
    initialize();
  }

  public void initialize() {
    this.bufferingPackets = false;
    this.buffer = new ArrayList<Packet>();
    this.expectedSeqnum = 0;
    this.lastRead = 0;
  }

  public void receiveMessage(Packet pkt) {
    Message msg = new Message("");
    System.out.println("-------------------------");
    System.out.println(pkt);

    if (!pkt.isCorrupt() && pkt.getSeqnum() == expectedSeqnum) {

      System.out.println("\033[0;32mSTATUS:\t\tGOOD\033[0m");

      buffer.add(pkt);
      // ra.receiveMessage(pkt.getMessage());
      expectedSeqnum = lastReceived();
      Packet ack = new Packet(msg, pkt.getSeqnum(), expectedSeqnum);
      nl.sendPacket(ack, Event.SENDER);
    } else if (!pkt.isCorrupt()) {
      System.out.println("\033[0;32mSTATUS:\t\tOUT OF ORDER\033[0m");
      if (bufferingPackets) {
        //TODO: Add packet to buffer
        buffer.add(pkt);
      }
      Packet ack = new Packet(msg, pkt.getSeqnum(), expectedSeqnum);
      nl.sendPacket(ack, Event.SENDER);
    } else {
      System.out.println("\033[0;32mSTATUS:\t\tCORRUPT\033[0m");

      Packet ack = new Packet(msg, pkt.getSeqnum(), expectedSeqnum);
      nl.sendPacket(ack, Event.SENDER);
    }
  }

  public void sortBuffer() {
    Collections.sort(buffer, new Comparator<Packet>() {
      @Override
      public int compare(Packet p1, Packet p2) {
        return p2.getSeqnum() - p1.getSeqnum();
      }
    });
  }

  public int lastReceived() {
    sortBuffer();
    int lastRcvd = expectedSeqnum;
    int index = 0;
    while (index < buffer.size() && lastRcvd == buffer.get(index).getSeqnum()) {
      lastRcvd += buffer.get(index).getMessage().length();
      index++;
    }
    return lastRcvd;
  }

  public void popBuffer() {
    sortBuffer();
    if (buffer.size() > 0) ra.receiveMessage(buffer.remove(0).getMessage());
  }

  public void setProtocol(int n) {
    if(n > 0)
        bufferingPackets = true;
    else
        bufferingPackets = false;
  }
}
