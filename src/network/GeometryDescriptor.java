/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.awt.Point;

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
  private Point point;
  
  public GeometryDescriptor(int index, GeomeryType type, Point point) {
    this.index = index;
    this.type = type;
    this.point = point;
  }
  
  public GeometryDescriptor(int index, GeomeryType type, int textIndex, Point point) {
    this(index, type, point);
    this.textIndex = textIndex;
  }
  
  public int getIndex() {
    return index;
  }
  
  public int getTextIndex() {
    return textIndex;
  }
  
  public GeomeryType getType() {
    return type;
  }
  
  public Point getPoint() {
    return point;
  }
  
  public void updateTextIndex(float width, int inc) {
    point.setLocation(point.getX() + width * inc, point.getY());
    textIndex+= inc; 
 }
  
  public String toString() {
    if(type == GeomeryType.Text)
      return String.format(type.toString(), index, textIndex);
    return String.format(type.toString(), index);
    
  }
  
}
