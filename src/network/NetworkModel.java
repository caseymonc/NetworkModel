/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Objects of this class contain information about a network nodes and their connections.  
 */
public class NetworkModel
{
    private String fileName;
    private boolean areChangesSaved;
    private List<NetworkNode> nodes;
    private List<NetworkConnection> connections;
    /**
     * Creates an empty network model that has a unique default file name and no contents
     */
	public NetworkModel (){
        nodes = new ArrayList<NetworkNode>();
        connections = new ArrayList<NetworkConnection>();
        areChangesSaved = true;
    }


    /**
     * Reads the specific file and creates a new NetworkModel object that contains all of the 
     * information in the file. If there is no such file then an exception should be thrown.
     * @param fileName the name of the file to be read.
     */
	public NetworkModel(String fileName) throws FileNotFoundException, IOException{
        this();
        setFileName(fileName);
        loadFile();
        areChangesSaved = true;
    }
	
    public void loadFile() throws FileNotFoundException, IOException {
      File file = new File(fileName);
      BufferedReader br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
         if(NetworkNode.isNodeString(line)){
           addNode(NetworkNode.fromString(line));
         }else if(NetworkConnection.isConnectionString(line)){
           addConnection(NetworkConnection.fromString(line));
         }
      }
      br.close();
    }
    
    /**
	 * Returns the name of the file associated with this model.
	 */
	public String getFileName(){
        return fileName;
    }

      /**
   * Changes the file name associated with this model
   * @param newFileName the new file name
   */
	public void setFileName(String newFileName){
        this.fileName = newFileName;
    }

    /**
    * Saves the contents of this model to its file.
    */
	public void save(){
        //TODO: save to file
        areChangesSaved = true;
    }

    /**
     * Returns true if there are unsaved changes.
     */
	public boolean unsavedChanges(){
        return !areChangesSaved;
    }

    /**
     * Adds the specified NetworkNode to the list of network objects
     * @param newNode
     */
	public void addNode(NetworkNode newNode){
        areChangesSaved = false;
        nodes.add(newNode);
        newNode.setNetwork(this);
    }

    /**
     * Returns the number of network node objects in this model.
     */
	public int nNodes(){
        return nodes.size();
    }

    /**
     * Returns the specified NetworkNode. Indexes begin at zero.
     * @param i index of the desired object. Must be less than nNodes()
     */
	public NetworkNode getNode(int i){
        return nodes.get(i);
    }

    /**
     * Removes the specified object from the list of nodes.
     * @param i the index of the object to be removed.
     */
	public void removeNode(int i){
        areChangesSaved = false;
        nodes.remove(i);
    }

    /**
     * Adds the specified NetworkConnection to the list of connections
     * @param newConnection
     */
	public void addConnection(NetworkConnection newConnection){
        areChangesSaved = false;
        connections.add(newConnection);
        newConnection.setNetwork(this);
    }

    /**
     * Returns the number of network connections in this model.
     */
	public int nConnections(){
        return connections.size();
    }

    /**
     * Returns the specified NetworkConnection. Indexes begin at zero.
     * @param i index of the desired object. Must be less than nConnections()
     */
	public NetworkConnection getConnection(int i){
        return connections.get(i);
    }

    /**
     * Removes the specified object from the list of connections
     * @param i the index of the object to be removed.
     */
	public void removeConnection(int i){
        areChangesSaved = false;
        connections.remove(i);
    }
    
    public NetworkNode getMaxXNode() {
      NetworkNode maxNode = null;
      for(NetworkNode node : nodes) {
        if(maxNode == null){
          maxNode = node;
        } else {
          if(maxNode.getX() < node.getX()){
            maxNode = node;
          }
        }
      }
      return maxNode;
    }
    
    public NetworkNode getMaxYNode() {
      NetworkNode maxNode = null;
      for(NetworkNode node : nodes) {
        if(maxNode == null){
          maxNode = node;
        } else {
          if(maxNode.getY() < node.getY()){
            maxNode = node;
          }
        }
      }
      return maxNode;
    }
    
    public NetworkNode getNodeForName(String name) {
      for(int i = 0; i < nodes.size(); i++) {
        if(nodes.get(i).getName().equals(name)) return nodes.get(i);
      }
      return null;
    }

    /**
    * This method is a regression test to verify that this class is
    * implemented correctly. It should test all of the methods including
    * the exceptions. It should be completely self checking. This 
    * should write "testing NetworkModel" to System.out before it
    * starts and "NetworkModel OK" to System.out when the test
    * terminates correctly. Nothing else should appear on a correct
    * test. Other messages should report any errors discovered.
    **/
	public static void Test(){
      System.out.println("testing NetworkModel");
      boolean success = true;
      try {
        NetworkModel model = new NetworkModel("");
        success &= false;
      } catch (FileNotFoundException ex) {
        success &= true;
      } catch (IOException ex) {
        success &= false;
      }
      
      if(!success) System.out.println("Failed to catch file not found exception");
      
      try {
        NetworkModel model = new NetworkModel("networkFile.txt");
        success &= model.nNodes() == 8;
        if(!success) System.out.println("Incorrect node count");
        success &= model.nConnections() == 7;
        if(!success) System.out.println("Incorrect connection count");
        
        success &= !model.unsavedChanges();
        if(!success) System.out.println("Incorrect unsaved changes");
        
        success &= model.getNode(0).getName().equals("\"Central\"");
        if(!success) System.out.println("Incorrect first node");
        success &= model.getConnection(0).getNode1().equals("\"Central\"");
        if(!success) System.out.println("Incorrect first connection");
        
        model.removeConnection(0);
        model.removeNode(0);
        
        success &= model.unsavedChanges();
        if(!success) System.out.println("Incorrect unsaved changes after changes");
        
        success &= model.getNode(0).getName().equals("\"Authentication server\"");
        if(!success) System.out.println("Incorrect first node after remove");
        success &= model.getConnection(0).getNode1().equals("\"Central over\"");
        if(!success) System.out.println("Incorrect first connection after remove");
      } catch (FileNotFoundException ex) {
        success &= false;
      } catch (IOException ex) {
        success &= false;
      }
      
      if(!success) System.out.println("Incorrect thrown exception");
      
      if(success)System.out.println("NetworkModel OK");
    }
}
