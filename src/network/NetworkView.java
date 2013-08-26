/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import javax.swing.JFrame;
import network.GeometryDescriptor.GeomeryType;
import network.NetworkConnection.Side;

/**
 *
 * @author caseymoncur
 */
public class NetworkView extends JFrame{
  
  private static final int PADDING = 50;
  
  private final NetworkModel model;
  
  private FontMetrics fontMetrics;
  
  
  public NetworkView(NetworkModel model) {
    super("Network");
    this.model = model;
    
    NetworkNode maxX = model.getMaxXNode();
    NetworkNode maxY = model.getMaxYNode();
    
    this.setBounds(0, 0, (int)maxX.getX() + getNodeWidth(maxX) + PADDING, (int)maxY.getY() + getNodeHeight(maxY) + PADDING);
    
    this.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent evt) {
        System.exit(0);
      }
    });
    
    this.setVisible(true);
        
    this.addMouseListener(new MouseListener(){

      @Override
      public void mouseClicked(MouseEvent me) {}

      @Override
      public void mousePressed(MouseEvent me) {}

      @Override
      public void mouseReleased(MouseEvent me) {
        GeometryDescriptor descriptor = pointGeometry(me.getPoint());
        if(descriptor != null) System.out.println(descriptor.toString());
      }

      @Override
      public void mouseEntered(MouseEvent me) {}

      @Override
      public void mouseExited(MouseEvent me) {}
      
    });
    
    this.setBackground(new Color(0xF4F4F4));
  }
  
  public void paint(Graphics g) {
    fontMetrics = g.getFontMetrics();

    for(int i = 0; i < model.nNodes(); i++){
      NetworkNode node = model.getNode(i);
      g.setColor(new Color(0xA0D1E0));
      g.fillOval(getNodeX(node), 
                 getNodeY(node), 
                 getNodeWidth(node), 
                 getNodeHeight(node));
      
      g.setColor(new Color(0x6D8398));
      g.drawOval(getNodeX(node), 
                 getNodeY(node), 
                 getNodeWidth(node), 
                 getNodeHeight(node));
      
      g.setColor(new Color(0x425363));
      g.drawString(node.getName(), 
                  getTextX(node), 
                  getTextY(node));
    }
    
    g.setColor(new Color(0x6D8398));
    for(int i = 0; i < model.nConnections(); i++) {
      NetworkConnection connection = model.getConnection(i);
      NetworkNode node1 = model.getNodeForName(connection.getNode1());
      NetworkNode node2 = model.getNodeForName(connection.getNode2());
      if(node1 == null || node2 == null) continue;
      
      int startX = getLineX(node1, connection.getSide1());
      int startY = getLineY(node1, connection.getSide1());
      
      int endX = getLineX(node2, connection.getSide2());
      int endY = getLineY(node2, connection.getSide2());
      
      g.drawLine(startX, startY, endX, endY);
      
    }
  }
  
  public int getTextX(NetworkNode node) {
    return ((int)node.getX() + getNodeWidth(node)/2) - (int)(node.getName().length() * 7.2);
  }
  
  public int getTextY(NetworkNode node) {
    return ((int)node.getY() + getNodeHeight(node)/2) - (int)(getNodeHeight(node)*(2.f/5.f));
  }
  
  public int getNodeX(NetworkNode node) {
    return (int)node.getX() - getNodeWidth(node)/2;
  }
  
  public int getNodeY(NetworkNode node) {
    return (int)node.getY() - getNodeHeight(node)/2;
  }
  
  public int getLineX(NetworkNode node, Side side) {
    int x = (int) node.getX();
    switch(side) {
      case Left:
        x -= getNodeWidth(node)/2;
        break;
      case Right:
        x += getNodeWidth(node)/2;
        break;
    }
    return x;
  }
  
  public int getLineY(NetworkNode node, Side side) {
    int y = (int) node.getY();
    switch(side) {
      case Top:
        y -= getNodeHeight(node)/2;
        break;
      case Bottom:
        y += getNodeHeight(node)/2;
        break;
    }
    return y;
  }
  
  public int getNodeHeight(NetworkNode node) {
    return Math.max(getNodeWidth(node)/3, 50);
  }
  
  public int getNodeWidth(NetworkNode node) {
    String name = node.getName();
    return name.length() * 8;
  }
  
  public GeometryDescriptor pointGeometry(Point mouseLoc) {
    for(int i = model.nNodes() - 1; i >= 0; i--) {
      if(isOnText(mouseLoc, model.getNode(i))) {
        return new GeometryDescriptor(i, GeomeryType.Text, getIndexForText(mouseLoc, model.getNode(i)));
      } else if(isInOval(mouseLoc, model.getNode(i))) {
        return new GeometryDescriptor(i, GeomeryType.Node);
      }
    }
    
    for(int i = model.nConnections() - 1; i >= 0; i--) {
      if(isOnLine(mouseLoc, model.getConnection(i))) {
        return new GeometryDescriptor(i, GeomeryType.Line);
      }
    }
    return new GeometryDescriptor(-1, GeomeryType.None);
  }
  
  private boolean isInOval(Point point, NetworkNode node) {
    // Normalize the coordinates compared to the ellipse
    // having a center at 0,0 and a radius of 0.5.
    double ellw = getNodeWidth(node);
    if (ellw <= 0.0) {
      return false;
    }
    double normx = (point.getX() - getNodeX(node)) / ellw - 0.5;
    double ellh = getNodeHeight(node);
    if (ellh <= 0.0) {
      return false;
    }
    double normy = (point.getY() - getNodeY(node)) / ellh - 0.5;
    return (normx * normx + normy * normy) < 0.25;
  }
  
  private boolean isOnText(Point point, NetworkNode node) {
    int textX = getTextX(node);
    int textY = getTextY(node) - fontMetrics.getHeight();
    
    int textWidth = fontMetrics.stringWidth(node.getName());
    int textHeight = fontMetrics.getHeight();
    
    boolean isInX = point.getX() >= textX && point.getX() <= textX + textWidth;
    boolean isInY = point.getY() >= textY && point.getY() <= textY + textHeight;
    
    return point.getX() >= textX &&
           point.getX() <= textX + textWidth &&
           point.getY() >= textY &&
           point.getY() <= textY + textHeight;
  }
  
  private int getIndexForText(Point point, NetworkNode node) {
    int textX = getTextX(node) - fontMetrics.stringWidth(node.getName().substring(0, 2));
    
    int index = 0;
    for(int i = 0; i < node.getName().length(); i++) {
      int position = textX + fontMetrics.stringWidth(textX + node.getName().substring(0, i));
      
      if(point.getX() < position) {
        index = i;
        break;
      }
        
      
    }
    
    return index;
  }
  
  private boolean isOnLine(Point point, NetworkConnection connection) {
    NetworkNode node1 = model.getNodeForName(connection.getNode1());
    NetworkNode node2 = model.getNodeForName(connection.getNode2());
    double distance = Line2D.ptSegDist(getLineX(node1, connection.getSide1()), 
                            getLineY(node1, connection.getSide1()), 
                            getLineX(node2, connection.getSide2()), 
                            getLineY(node2, connection.getSide2()), 
                            point.getX(), point.getY());
    return distance >= 0 && distance < 1;
  }
}
