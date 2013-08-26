/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author caseymoncur
 */
public class Network {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
      try {
        String fileName = "networkFile.txt";//args[0];
        NetworkModel model = new NetworkModel(fileName);
        
        NetworkView view = new NetworkView(model);
      } catch (FileNotFoundException ex) {
        Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException ex) {
        Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
}
