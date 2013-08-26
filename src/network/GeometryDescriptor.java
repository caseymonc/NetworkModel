/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

/**
 *
 * @author caseymoncur
 */
public class GeometryDescriptor {
  public enum GeomeryType {
    Node,
    Text,
    Line,
    None;
    
    public String toString() {
      if(this == Node) return "Network Node %d was clicked.";
      if(this == Text) return "Network Node %d was clicked at text index %d.";
      if(this == Line) return "Network Connection %d was clicked.";
      return "";
    }
  }
  
  private int index;
  private GeomeryType type;
  private int textIndex;
  
  public GeometryDescriptor(int index, GeomeryType type) {
    this.index = index;
    this.type = type;
  }
  
  public GeometryDescriptor(int index, GeomeryType type, int textIndex) {
    this(index, type);
    this.textIndex = textIndex;
  }
  
  public int getIndex() {
    return index;
  }
  
  public GeomeryType getType() {
    return type;
  }
  
  public String toString() {
    if(type == GeomeryType.Text)
      return String.format(type.toString(), index, textIndex);
    return String.format(type.toString(), index);
    
  }
  
}
