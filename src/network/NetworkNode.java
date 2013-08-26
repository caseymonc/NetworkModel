/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.util.ArrayList;

/**
* Objects of this class describe a single node in a network.
**/

public class NetworkNode
{
    
    private String name;
    private double x;
    private double y;
    private NetworkModel network;
    
    /**
    * Creates a network node
    * @param nodeName the name that the node will be identified by. Names are exact
    *	and case sensitive.
    * @param xCenter the X coordinate of the center of the node in pixels
    * @param yCenter the Y coordinate of the center of the node in pixels
    */
	public NetworkNode(String nodeName, double xCenter, double yCenter){
        this.setName(nodeName);
        this.setLocation(xCenter, yCenter);
    }

    /**
    * @return name of the node
    */
	public String getName(){
        return name;
    }

    /**
    * Changes the name of the node
    * @param newName
    */
	public void setName(String newName){
      this.name = newName;
    }

    /**
    * @return the X coordinate of the center of the node
    */
	public double getX() {
        return x;
    }

    /**
    * @return the Y coordinate of the center of the node
    */
	public double getY() {
      return y;
    }

    /**
    * Changes the location of the center of the node
    */
	public void setLocation(double xCenter, double yCenter){
      x = xCenter;
      y = yCenter;
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
    
    public String toString() {
      return "N " + x + " " + y + " " + name;
    }
    
    public static NetworkNode fromString(String s) {
      String[] pieces = s.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
      if(pieces == null || pieces.length != 4 || !pieces[0].equals("N"))
        throw new IllegalArgumentException("Must start with \"N\"");
      
      double x = Double.parseDouble(pieces[1]);
      double y = Double.parseDouble(pieces[2]);
      
      String name = pieces[3];
      
      return new NetworkNode(name, x, y);
    }
    
    public static boolean isNodeString(String s) {
      return s.startsWith("N");
    }
}
