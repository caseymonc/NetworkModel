/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

/**
* This class describes a connection between two network nodes
*/
public class NetworkConnection
{
    public enum Side {
        Left, 
        Right, 
        Top, 
        Bottom;
        
        public String toString(){
          switch(this) {
            case Left:
              return "L";
            case Right:
              return "R";
            case Top:
              return "T";
            case Bottom:
              return "B";
          }
          
          return "";
        }
        
        public static Side fromString(String s){
          if(s.equals("L")) return Side.Left;
          if(s.equals("R")) return Side.Right;
          if(s.equals("T")) return Side.Top;
          if(s.equals("B")) return Side.Bottom;
          
          return null;
        }
    }

    private String node1;
    private Side side1;
    private String node2;
    private Side side2;
    private NetworkModel network;

    /**
    * Creates a new connection
    * @param node1 the name of the first node to be connected
    * @param side1 specifies the side of node1 to which the connection is to be attached
    * @param node2 the name of the second node to be connected
    * @param side2 specifies the side of node2 to which the connection is to be attached
    */
	public NetworkConnection(String node1, Side side1, String node2, Side side2) {
      this.node1 = node1;
      this.side1 = side1;
      this.node2 = node2;
      this.side2 = side2;
    }
    
    public String getNode1() {
      return node1;
    }
    
    public String getNode2() {
      return node2;
    }
    
    public Side getSide1() {
      return side1;
    }
    
    public Side getSide2() {
      return side2;
    }
    
    public String toString() {
      return "C " + node1 + " " + side1.toString() + " " + node2 + " " + side2.toString();
    }
    
    public static NetworkConnection fromString(String s) {
      String[] pieces = s.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
      
      if(pieces == null || pieces.length != 5 || !pieces[0].equals("C"))
        throw new IllegalArgumentException("Must start with \"C\"");
      
      String node1 = pieces[1];
      Side side1 = Side.fromString(pieces[2]);
      String node2 = pieces[3];
      Side side2 = Side.fromString(pieces[4]);
      
      return new NetworkConnection(node1, side1, node2, side2);
    }
    
    /**
    * Sets a reference to the network model that this node belongs to
    * @param network
    */
	public void setNetwork(NetworkModel network) {
      this.network = network;
    }

    /**
    * @return the network that this node belongs to
    */
	public NetworkModel getNetwork() {
      return this.network;
    }
    
    public static boolean isConnectionString(String s){
      return s.startsWith("C");
    }
}
