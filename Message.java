/**
 * A class which represents an application message (which is simply a string)
 */
public class Message {
  private String x;

  public Message(String x) {
    this.x = x;
  }

  public String getMessage() {
    return x;
  }

  public int length() {
    return x.length();
  }

  public int byteLength() {
    return x.getBytes().length;
  }

  public void corruptMessage() {
    System.out.println("--------- CORRUPTING -------");
    if (x.length() > 1) {
      x = (char) (x.charAt(0) + 1) + x.substring(1);
    }
  }

  public String toString () {
    return x;
  }
}
