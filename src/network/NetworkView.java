/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import network.GeometryDescriptor.GeomeryType;
import network.NetworkConnection.Side;
import static network.NetworkConnection.Side.Left;

/**
 *
 * @author caseymoncur
 */
public class NetworkView extends JFrame implements NetworkModel.ModelListener{
  
  private enum Mode {
    Normal,
    AddNode,
    AddConnections,
    Rotate
  }
  
  private static final int PADDING = 50;
  private static final int BUTTONS_PADDING = 80;
  private static final Color CONNECTION_COLOR = new Color(0x777777);
  private static final Color SELECTED_COLOR   = new Color(0xF00F00);
  private static final Color NODE_COLOR       = new Color(0x66ccff);
  private static final Color TEXT_COLOR       = new Color(0x000000);
  private static final Color BACKGROUND_COLOR = new Color(0xFFFFFF);
  private static final int BEZIER_SIZE = 100;
  
  private static Set<NetworkView> activeViews = new HashSet<NetworkView>();
  
  private NetworkModel model;
  private FontMetrics fontMetrics;
  private GeometryDescriptor selectedItem;
  private NetworkNode selectedConnectionNode;
  private Side selectedConnectionSide;
  private NetworkNode selectedDraggingNode;
  private MouseEvent startEvent;
  private MouseEvent lastEvent;
  private Mode mode = Mode.Normal;
  
  public NetworkView(final NetworkModel model) {
    super("Network");
    activeViews.add(this);
    this.model = model;
    model.addListener(this);
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    
    NetworkNode maxX = model.getMaxXNode();
    NetworkNode maxY = model.getMaxYNode();
    
    this.setBounds(0, 0, (int)maxX.getX() + getNodeWidth(maxX) + BUTTONS_PADDING + PADDING, (int)maxY.getY() + getNodeHeight(maxY) + PADDING);
    
    this.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent evt) {
        if(NetworkView.this.model.isLastListener() && NetworkView.this.model.unsavedChanges()) {
          int result = JOptionPane.showConfirmDialog(null, "Save before closing?");
          if(result == JOptionPane.NO_OPTION) exitWindow();
          else if(result == JOptionPane.YES_OPTION) {
            NetworkView.this.model.save();
            exitWindow();
          }
        } else {
          exitWindow();
        }        
      }

      private void exitWindow() {
        activeViews.remove(NetworkView.this);
        if(activeViews.size() > 0){
          NetworkView.this.model.removeListener(NetworkView.this);
          NetworkView.this.dispose();
        } else {
          System.exit(0);
        }
      }
    });
    
    
    this.addMouseListener(new MouseListener(){

      @Override
      public void mouseClicked(MouseEvent me) {}

      @Override
      public void mousePressed(MouseEvent me) {
      
      }

      @Override
      public void mouseReleased(MouseEvent me) {
        transformEvent(me);
        
        if (mode == Mode.AddNode) {
          NetworkNode node = new NetworkNode("\"New Node\"", me.getX(), me.getY());
          NetworkView.this.model.addNode(node);
        } else if (mode == Mode.AddConnections) {
          for(int i = 0; i < model.nNodes(); i++) {
            NetworkNode node = model.getNode(i);
            Side side = getConnectionPoint(node, me.getPoint());
            if(side != null) {
              if(selectedConnectionNode != null) {
                model.addConnection(new NetworkConnection(selectedConnectionNode.getName(), 
                                                          selectedConnectionSide, 
                                                          node.getName(), 
                                                          side));
                selectedConnectionNode = null;
                selectedConnectionSide = null;
              } else {
                selectedConnectionNode = node;
                selectedConnectionSide = side;
              }
            }
          }
        } else {
          GeometryDescriptor descriptor = pointGeometry(me.getPoint());
          selectedItem = descriptor;
          startEvent = null;
          lastEvent = null;
        }
        selectedDraggingNode = null;
        repaint();
      }

      @Override
      public void mouseEntered(MouseEvent me) {}

      @Override
      public void mouseExited(MouseEvent me) {}
      
    });
    
    this.addMouseMotionListener(new MouseMotionListener(){

      @Override
      public void mouseDragged(MouseEvent me) {
        if(mode != Mode.Normal) return;
        transformEvent(me);
        GeometryDescriptor descriptor = pointGeometry(me.getPoint());
        if(descriptor.getType() == GeomeryType.Node) {
          NetworkNode node = NetworkView.this.model.getNode(descriptor.getIndex());
          if(startEvent == null) {
            startEvent = me;
            lastEvent = me;
            selectedDraggingNode = node;
          }
          
        }
        
        if(selectedDraggingNode != null && me.getPoint().distance(startEvent.getPoint()) > 3){
          selectedDraggingNode.setLocation(selectedDraggingNode.getX() + (me.getPoint().getX() - lastEvent.getPoint().getX()), 
                  selectedDraggingNode.getY() + (me.getPoint().getY() - lastEvent.getPoint().getY()));
          lastEvent = me;
        }
      }

      @Override
      public void mouseMoved(MouseEvent me) {}
    
    });
    
    this.addKeyListener(new KeyAdapter(){
      public void keyPressed(KeyEvent event) {
        if(selectedItem != null && selectedItem.getType() == GeomeryType.Text) {
          NetworkNode node = NetworkView.this.model.getNode(selectedItem.getIndex());
          
          if(event.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            String name = node.getName();
            name = name.substring(0, selectedItem.getTextIndex()) + name.substring(selectedItem.getTextIndex() + 1, name.length());
            node.setName(name);
            selectedItem.updateTextIndex(fontMetrics.charWidth(event.getKeyChar()), -1);
            return;
          }else if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            String name = node.getName();
            node.setName(name);
            selectedItem.updateTextIndex(fontMetrics.charWidth(name.charAt(selectedItem.getTextIndex())), -1);
            return;
          }else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            String name = node.getName();
            node.setName(name);
            selectedItem.updateTextIndex(fontMetrics.charWidth(name.charAt(selectedItem.getTextIndex() + 1)), 1);
            return;
          }
          
          if(isActionKey(event)) return;
          
          String name = node.getName();
          name = name.substring(0, selectedItem.getTextIndex() + 1) + event.getKeyChar() + name.substring(selectedItem.getTextIndex() + 1, name.length());
          node.setName(name);
          selectedItem.updateTextIndex(fontMetrics.charWidth(event.getKeyChar()), 1);
          
        }
      }
    });
    
    this.setBackground(BACKGROUND_COLOR);
  
    createMenuBar();
    addSideButtons();
    
    this.setVisible(true);
  }
  
  public Side getConnectionPoint(NetworkNode node, Point point) {
    if(isConnectionPoint(node, point, Side.Bottom))
      return Side.Bottom;
    if(isConnectionPoint(node, point, Side.Top))
      return Side.Top;
    if(isConnectionPoint(node, point, Side.Left))
      return Side.Left;
    if(isConnectionPoint(node, point, Side.Right))
      return Side.Right;
    
    return null;
  }
  
  public boolean isConnectionPoint(NetworkNode node, Point point, Side side) {
    int x = getLineX(node, side);
    int y = getLineY(node, side);
    return (point.getX() - x) * (point.getX() - x) + (point.getY() - y) * (point.getY() - y) < (15 * 15);
  }
  
  public void transformEvent(MouseEvent me) {
    me.translatePoint(-BUTTONS_PADDING, -PADDING);
  }
  
  public void addSideButtons() {
    try {
      JPanel panel = new JPanel();
      panel.setPreferredSize(new Dimension(BUTTONS_PADDING, this.getHeight()));
      
      JButton normalModeButton = new JButton();
      Image normalModeImage = ImageIO.read(new File("images/1378176668_Book_of_record.png"));
      normalModeButton.setIcon(new ImageIcon(normalModeImage));
      normalModeButton.setSize(25, 25);
      panel.add(normalModeButton);
      
      normalModeButton.setBorderPainted(false); 
      normalModeButton.setContentAreaFilled(false); 
      normalModeButton.setFocusPainted(false); 
      normalModeButton.setOpaque(false);
      normalModeButton.setFocusable(false);
      
      normalModeButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent ae) {
          mode = Mode.Normal;
          selectedConnectionNode = null;
          repaint();
        }
      });
      
      JButton addNodeButton = new JButton();
      Image addNodeImage = ImageIO.read(new File("images/1378176590_Import.png"));
      addNodeButton.setIcon(new ImageIcon(addNodeImage));
      addNodeButton.setSize(25, 25);
      panel.add(addNodeButton);
      
      addNodeButton.setBorderPainted(false); 
      addNodeButton.setContentAreaFilled(false); 
      addNodeButton.setFocusPainted(false); 
      addNodeButton.setOpaque(false);
      addNodeButton.setFocusable(false);
      
      addNodeButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent ae) {
          mode = Mode.AddNode;
          selectedConnectionNode = null;
          repaint();
        }
      });
      
      JButton addConnectionButton = new JButton();
      Image addConnectionImage = ImageIO.read(new File("images/1378176200_Network_connection.png"));
      addConnectionButton.setIcon(new ImageIcon(addConnectionImage));
      addConnectionButton.setSize(25, 25);
      panel.add(addConnectionButton);
      
      addConnectionButton.setBorderPainted(false); 
      addConnectionButton.setContentAreaFilled(false); 
      addConnectionButton.setFocusPainted(false); 
      addConnectionButton.setOpaque(false);
      addConnectionButton.setFocusable(false);
      
      addConnectionButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent ae) {
          mode = Mode.AddConnections;
          selectedConnectionNode = null;
          repaint();
        }
      });
      
      JButton rotateButton = new JButton();
      Image rotateImage = ImageIO.read(new File("images/1378201788_Synchronize.png"));
      rotateButton.setIcon(new ImageIcon(rotateImage));
      rotateButton.setSize(25, 25);
      panel.add(rotateButton);
      
      rotateButton.setBorderPainted(false); 
      rotateButton.setContentAreaFilled(false); 
      rotateButton.setFocusPainted(false); 
      rotateButton.setOpaque(false);
      rotateButton.setFocusable(false);
      
      rotateButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent ae) {
          mode = Mode.Rotate;
          selectedConnectionNode = null;
          repaint();
        }
      });
      
      this.add(panel, BorderLayout.WEST);
    } catch (IOException ex) {
      Logger.getLogger(NetworkView.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public void createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    
    JMenu menu = new JMenu("File");
    menuBar.add(menu);
    
    JMenuItem menuItem = new JMenuItem("Open");
    menuItem.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_O, ActionEvent.META_MASK));
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae) {
        openNewWindow();
      }
    });
    menu.add(menuItem);
    
    menuItem = new JMenuItem("Save");
    menuItem.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_S, ActionEvent.META_MASK));
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae) {
        save();
      }
    });
    menu.add(menuItem);
    
    menuItem = new JMenuItem("Save As");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae) {
        //Create a file chooser
        final JFileChooser fc = new JFileChooser();
        //In response to a button click:
        int returnVal = fc.showSaveDialog(NetworkView.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File file = fc.getSelectedFile();
          saveAs(file.getAbsolutePath());
        }
      }
    });
    menu.add(menuItem);
    
    this.setJMenuBar(menuBar);
    
  }
  
  private void openNewWindow() {
    //Create a file chooser
    final JFileChooser fc = new JFileChooser();
    fc.setFileFilter(new FileNameExtensionFilter("TXT Files", "txt"));
    //In response to a button click:
    int returnVal = fc.showOpenDialog(NetworkView.this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = fc.getSelectedFile();
      String fileName = file.getAbsolutePath();
      NetworkModel model = NetworkModel.getModelForFileName(fileName);
      if(model == null) {
        try {
          model = new NetworkModel(fileName);
        } catch (FileNotFoundException ex) {
          Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
          Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      
      NetworkView view = new NetworkView(model);
    }
  }
  
  private void save() {
    model.save();
  }
  
  private void saveAs(String fileName) {
    try {
      model.setFileName(fileName);
      model.save();
      model = new NetworkModel(fileName);
      model.addListener(this);
    } catch (FileNotFoundException ex) {
      Logger.getLogger(NetworkView.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(NetworkView.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public void paint(Graphics g1) {
    super.paint(g1);
    Graphics2D g = (Graphics2D)g1;
    g.translate(BUTTONS_PADDING, PADDING);
    g.setStroke(new BasicStroke(1));
    fontMetrics = g.getFontMetrics();
    NetworkNode selectedNode = null;
    NetworkConnection selectedConnection = null;
    int selectedTextIndex = -1;
    if(this.selectedItem != null) {
      if(this.selectedItem.getType() == GeomeryType.Node || 
              this.selectedItem.getType() == GeomeryType.Text){
        selectedNode = model.getNode(this.selectedItem.getIndex());
      }
      
      if(this.selectedItem.getType() == GeomeryType.Line) {
        selectedConnection = model.getConnection(selectedItem.getIndex());
      }
      
      if(this.selectedItem.getType() == GeomeryType.Text) {
        selectedTextIndex = selectedItem.getTextIndex();
      }
    }
    for(int i = 0; i < model.nNodes(); i++){
      NetworkNode node = model.getNode(i);
      g.setColor(NODE_COLOR);
      g.fillOval(getNodeX(node), 
                 getNodeY(node), 
                 getNodeWidth(node), 
                 getNodeHeight(node));
      
      if(node == selectedNode) 
        g.setColor(SELECTED_COLOR);
      else
        g.setColor(CONNECTION_COLOR);
      g.drawOval(getNodeX(node), 
                 getNodeY(node), 
                 getNodeWidth(node), 
                 getNodeHeight(node));
      
      g.setColor(TEXT_COLOR);
      TextLayout text = new TextLayout(node.getName(), g.getFont(), ((Graphics2D)g).getFontRenderContext());
      if(node == selectedNode && selectedTextIndex > -1) {
        Graphics2D graphics = (Graphics2D) g.create();
        TextHitInfo hit = text.hitTestChar((float)selectedItem.getPoint().getX(), (float)selectedItem.getPoint().getY());
        Shape[] carets = text.getCaretShapes(selectedItem.getTextIndex() + 1);
        graphics.translate(getTextX(node), getTextY(node));
        graphics.draw(carets[0]);
        if (carets[1] != null) {
           graphics.draw(carets[1]);
        }
      }
      text.draw(((Graphics2D)g), getTextX(node), getTextY(node));
    }
    
    
    for(int i = 0; i < model.nConnections(); i++) {
      NetworkConnection connection = model.getConnection(i);
      
      if(connection == selectedConnection)
        g.setColor(SELECTED_COLOR);
      else
        g.setColor(CONNECTION_COLOR);
      
      NetworkNode node1 = connection.getStartNode();
      NetworkNode node2 = connection.getEndNode();
      if(node1 == null || node2 == null) continue;
      
      int startX = getLineX(node1, connection.getSide1());
      int startY = getLineY(node1, connection.getSide1());
      
      int endX = getLineX(node2, connection.getSide2());
      int endY = getLineY(node2, connection.getSide2());
      
      int p1X = getPX(startX, connection.getSide1());
      int p1Y = getPY(startY, connection.getSide1());
      
      int p2X = getPX(endX, connection.getSide2());
      int p2Y = getPY(endY, connection.getSide2());
      
      Path2D p = new GeneralPath();
      p.moveTo(startX, startY);
      p.curveTo(p1X, p1Y, p2X, p2Y, endX, endY);
      
      
      g.draw(p);
      //g.drawLine(startX, startY, endX, endY);
      
    }
    
    if(mode == Mode.AddConnections) {
      for(int i = 0; i < model.nNodes(); i++) {
        NetworkNode node = model.getNode(i);
        drawPotentialConnectionCircle(node, Side.Bottom, g);
        drawPotentialConnectionCircle(node, Side.Top, g);
        drawPotentialConnectionCircle(node, Side.Left, g);
        drawPotentialConnectionCircle(node, Side.Right, g);
      }
    }
  }
  
  private void drawPotentialConnectionCircle(NetworkNode node, Side side, Graphics2D g) {
    int pointX = getLineX(node, side);
    int pointY = getLineY(node, side);
    if(selectedConnectionNode == node && side == selectedConnectionSide)
      g.setColor(SELECTED_COLOR);
    else
      g.setColor(CONNECTION_COLOR);
    g.drawOval(pointX - 15, pointY - 15, 30, 30);
  }
  
  public int getPX(int x, Side side) {
    int p1X = x;
    switch(side){
      case Left:
        p1X -= BEZIER_SIZE;
        break;
      case Right:
        p1X += BEZIER_SIZE;
        break;
    }
    return p1X;
  }
  
  public int getPY(int y, Side side) {
    int p1Y = y;
    switch(side){
      case Top:
        p1Y -= BEZIER_SIZE;
        break;
      case Bottom:
        p1Y += BEZIER_SIZE;
        break;
    }
    return p1Y;
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
        return new GeometryDescriptor(i, GeomeryType.Text, getIndexForText(mouseLoc, model.getNode(i)), mouseLoc);
      } else if(isInOval(mouseLoc, model.getNode(i))) {
        return new GeometryDescriptor(i, GeomeryType.Node, mouseLoc);
      }
    }
    
    for(int i = model.nConnections() - 1; i >= 0; i--) {
      if(isOnLine(mouseLoc, model.getConnection(i))) {
        return new GeometryDescriptor(i, GeomeryType.Line, mouseLoc);
      }
    }
    return new GeometryDescriptor(-1, GeomeryType.None, mouseLoc);
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
    NetworkNode node1 = connection.getStartNode();
    NetworkNode node2 = connection.getEndNode();
    /*double distance = Line2D.ptSegDist(getLineX(node1, connection.getSide1()), 
                            getLineY(node1, connection.getSide1()), 
                            getLineX(node2, connection.getSide2()), 
                            getLineY(node2, connection.getSide2()), 
                            point.getX(), point.getY());
    return distance >= 0 && distance < 1;*/
    
    int startX = getLineX(node1, connection.getSide1());
    int startY = getLineY(node1, connection.getSide1());

    int endX = getLineX(node2, connection.getSide2());
    int endY = getLineY(node2, connection.getSide2());

    int p1X = getPX(startX, connection.getSide1());
    int p1Y = getPY(startY, connection.getSide1());

    int p2X = getPX(endX, connection.getSide2());
    int p2Y = getPY(endY, connection.getSide2());
    
    Path2D p = new GeneralPath();
    p.moveTo(startX, startY);
    p.curveTo(p1X, p1Y, p2X, p2Y, endX, endY);
    
    return p.contains(point);
  }

  private boolean isActionKey(KeyEvent event) {
    if (event.isActionKey()) return true;
    if(event.getKeyCode() == KeyEvent.VK_ALT) return true;
    if(event.getKeyCode() == KeyEvent.VK_CONTROL) return true;
    if(event.getKeyCode() == KeyEvent.VK_META) return true;
    if(event.getKeyCode() == KeyEvent.VK_SHIFT) return true;
    if(event.getKeyCode() == KeyEvent.VK_ENTER) return true;
    return false;
  }
  
  @Override
  public void NodeAdded(NetworkNode node) {
    this.repaint();
  }

  @Override
  public void NodeRemoved(NetworkNode node) {
    this.repaint();
  }

  @Override
  public void NodeChanged(NetworkNode node) {
    this.repaint();
  }

  @Override
  public void ConnectionAdded(NetworkConnection connection) {
    this.repaint();
  }

  @Override
  public void ConnectionRemove(NetworkConnection connection) {
    this.repaint();
  }

  @Override
  public void ConnectionChanged(NetworkConnection connection) {
    this.repaint();
  }
}
