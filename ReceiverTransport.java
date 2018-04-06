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
  private int expectedSeqnum;
  private int seqnum;
  private int lastRead;

  public ReceiverTransport(NetworkLayer nl) {
    ra = new ReceiverApplication();
    this.nl = nl;
    initialize();
  }

  public void initialize() {
    this.bufferingPackets = false;
    this.buffer = new ArrayList<Packet>();
    this.maxBufferLength = 10;
    this.expectedSeqnum = 0;
    this.lastRead = 0;
    this.seqnum = 0;
  }

  public void receiveMessage(Packet pkt) {
    Message msg = new Message("");
    String status = "";

    if (!pkt.isCorrupt() && pkt.getSeqnum() == expectedSeqnum) {

      status = "|\t\033[0;32mSTATUS:\t\t\tGOOD\033[0m\t\t\t\t\t|";

      if (buffer.size() < maxBufferLength) buffer.add(pkt);
      // System.out.println("ADDED TO BUFFERRRRR");
      // ra.receiveMessage(pkt.getMessage());
      expectedSeqnum = lastReceived();
    } else if (!pkt.isCorrupt()) {
      status = "|\t\033[0;32mSTATUS:\t\t\tOUT OF ORDER\033[0m\t\t\t\t|";
      if (bufferingPackets) {
        //TODO: Add packet to buffer
        if (buffer.size() < maxBufferLength) buffer.add(pkt);
      }
    } else {
      status = "|\t\033[0;32mSTATUS:\t\t\tCORRUPT\033[0m\t\t\t\t\t|";
    }

    Packet ack = new Packet(msg, seqnum, expectedSeqnum, 0, true);
    ack.setRcvwnd(maxBufferLength - buffer.size());
    seqnum += 1;
    System.out.println(" --- \033[0;32mReceived packet\033[0m --------------------------------------------------- ");
    System.out.println(pkt);
    System.out.println(status);
    System.out.println(" ----------------------------------------------------------------------- \n");
    System.out.println(" --- \033[0;32mSending ACK\033[0m ------------------------------------------------------- ");
    System.out.println(ack);
    System.out.println(" ----------------------------------------------------------------------- \n");

    nl.sendPacket(ack, Event.SENDER);

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
      lastRcvd += buffer.get(index).getMessage().byteLength();
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
