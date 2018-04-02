/**
 * A class whoch represents an application message (which is simply a string)
 */

public class Message
{
    
    private String x;
    
    public Message(String x)
    {
        this.x=x;
    }
    
    public String getMessage()
    {
        return x;
    }
    
    public void corruptMessage()
    {
       x=String.valueOf(x.charAt(0)+1) + x.substring(1);

    }
}
