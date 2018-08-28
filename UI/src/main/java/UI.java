/*
* UI.java
*
* UI class that contains code to create the UI and manage all visual elements of the UI.
*
* Authors: SEP UG02
*/

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import schemas.ApolloLandingSite;
import schemas.Area;
import schemas.Boundary;
import schemas.Circle;
import schemas.Lunarrovermap;
import schemas.ObjectFactory;
import schemas.Obstacle;
import schemas.RoverLandingSite;
import schemas.Track;
import schemas.VehicleStatus;
import schemas.Zone;

import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseMotionListener;

@SuppressWarnings("serial")
public class UI extends JFrame {
	
	private JFrame frame = this;
	
	// some miscellaneous constants 
	private static final String WINDOW_TITLE = "Rover Control Station";
	private static final int WINDOW_WIDTH = 1000;
	private static final int WINDOW_HEIGHT = 500;
	
	// Directional buttons array with constants to define each button 
	private JButton[] directionalButtons;
	private static final int NO_OF_DIRECTIONAL_BUTTONS = 4;
	
	// Some other UI features 
	private JButton manualControlButton;
	
	private JLabel currentState;
	private int map_panel_width;
	private int map_panel_height;
	private MapLabel mapImage;
	private JTextArea activityLog;
	private int numberOfLogLines = 0;
	
	// This is what we'll use to communicate with the rover using sockets 
	private RoverController controller;
	public JLabel roverXY;
	public JLabel roverColour;
	public JLabel roverDistance;
	public JLabel roverAngle;
	
	// Creates the listeners for all of our buttons.
	private Listeners listeners;
	private InputMap keyboardListeners;
	public GridMap grid;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UI frame = new UI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * This function enables/disables all buttons (used when dis/connected)
	 */
	public void enableButtons(boolean state) {
		// Enable all the directional buttons
		for( int index = 0; index < NO_OF_DIRECTIONAL_BUTTONS; index++ ) {
			directionalButtons[index].setEnabled(state);
		}
		
		// Enable manual control button
		manualControlButton.setEnabled(state);
	}
	
	/**
	 * Constructor for the UI.
	 */
	public UI() {
		// Setup the window.
		setTitle(WINDOW_TITLE);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		
		// Add the panel to add all our content to.
		JPanel panel = new JPanel();
		add(panel);
		panel.setLayout(new BorderLayout());
		
	    // Connect listeners to UI.
		this.listeners = new Listeners(this);
		
		// initialise GridMap
		// Rationale: Perimeter is 500 metres. Map is A1 size (map_panel_heightmm x map_panel_widthmm).
		// 1 metre = 5.74 mm, which is a horrible number
		// Therefore, 1 metre = 5 mm, which makes perimeter 574 meter
		// each x and y position represents 1 metre squared
		// resolution = 5 mills per cell
		map_panel_width = 800;
		map_panel_height = 640;
		grid = new GridMap(500,500,20, map_panel_width,map_panel_height);				
		
		// Create all of the UI components we need.
		createComponents();
		
		// Start Map Refresher
		new RefreshMapData().start();	
		
		// Add a UIPanel to the content panel.
		add(new UIPanel(), BorderLayout.PAGE_START);
		
		// We want the controller to communicate with this UI
		this.controller = new RoverController( this );

		// Align, resize and make window visible.
		pack();
		setLocationRelativeTo(null);
		setVisible(true);			
	}
	
	/**
	 * Calls functions that create the individual components used in the UI (e.g. buttons, labels).
	 */
	public void createComponents() {
		loadMapImage();
		createMenuButtons();
		createDirectionalButtons();
		createActivityLog();
		createManualControlButton();
		createCurrentStateLabel();
		createSensorDatalabels();
		addKeyboardButtonListeners();
		addManualControlButtonListener();
	}

	/**
	 * Creates all menu buttons we need.
	 * The menu buttons are in an array of length 4 with elements.
	 */
	public void createMenuButtons() {
		JMenuBar menuBar = new JMenuBar();
        menuBar.add(new JMenu("File"));
        menuBar.add(new JMenu("Rover"));
        this.setJMenuBar(menuBar);
        
        JMenu fileMenu = menuBar.getMenu(0);

        // Create the open button as menu item and add the choose file dialogue as an action.
        JMenuItem open = new JMenuItem("Open...");
        open.setAccelerator(KeyStroke.getKeyStroke("control O"));
        open.addActionListener( new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
	            final JFileChooser fileChooser = new JFileChooser();

	            // Handle the return value from the file chooser.
	            int returnVal = fileChooser.showOpenDialog(frame);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                // Get the chosen file.
	                File file = fileChooser.getSelectedFile();

	                // Try and open the file to overwrite the current map.
	                // If any errors occur while opening the new map, make no changes to the current map and display an
	                // error dialogue (see readFileIntoMap function).
	                System.out.println("Opening: " + file.getName() + ".");
	                
	                boolean result = openMapFromFile(file);
	                
	                if (!result) {
	                	JOptionPane.showMessageDialog(frame, "Map not loaded correctly.");
	                }
	            } else {
	                // If the user closed the file chooser, ignore the event.
	                System.out.println("Open command cancelled by user.");
	            }
			}
        });
        fileMenu.add(open);

        // Create the save button as menu item and add the choose file dialogue listener to it.
        JMenuItem saveAs = new JMenuItem("Save as...");
        saveAs.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveAs.addActionListener( new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				// Open a file chooser and prompt a user to define an xml file to save to.
	            final JFileChooser fileChooser = new JFileChooser();

	            // Handle the return value from the file chooser.
	            int returnVal = fileChooser.showSaveDialog(frame);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                // Get the chosen file name from the file chooser.
	                File file = fileChooser.getSelectedFile();

	                System.out.println("Saving to: " + file.getName() + ".");
	                
	                boolean result = saveMapToFile(file);
	                
	                if (!result) {
	                	JOptionPane.showMessageDialog(frame, "Map not saved correctly.");
	                }
	            } else {
	                System.out.println("Save command cancelled by user.");
	            }	
			}
        });  
        fileMenu.add(saveAs);
        
        
        JMenu roverMenu = menuBar.getMenu(1);
        final UI ui = this;
        JMenuItem connectRover = new JMenuItem("Connect to rover");
        connectRover.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				if(!ui.isConnected()) {
					// Validate IP address first.
					String IP = ui.getIP();
					if (IP == null) {
		                JOptionPane.showMessageDialog(frame, "Invalid IP address.", "IP address invalid", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (!ui.validate(IP)) {
		                JOptionPane.showMessageDialog(frame, "Invalid IP address.", "IP address invalid", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					ui.getController().connectToRover(IP);
					if(ui.isConnected()) ui.addToLog("Rover is connected \n");
				}
			}
        });
        roverMenu.add(connectRover);
        
        JMenuItem disconnectRover = new JMenuItem("Disconnect from rover");
        disconnectRover.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				if(ui.isConnected()) {
					ui.getController().disconnectFromRover();
					if(!ui.isConnected()) ui.addToLog("Rover is disconnected \n");
				}
			}
        });
        roverMenu.add(disconnectRover);
	}
	
	
	/**
	 * Given a file object of a Lunarrovermap map, parses the map to populate the current gridmap with all information stored in the 
	 * Lunarrovermap object.
	 * 
	 * @param file
	 * @return true if the map was loaded correctly.
	 */
	private boolean openMapFromFile(File file) {
		// Get the string from the file.
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(file.toURI()));
		} catch (IOException e) {
			return false;
		}
		String xmlMap = new String(encoded, StandardCharsets.UTF_8);
		
		// Convert the string to a lunar rover map object.
		Lunarrovermap map;
		try {
			map = unmarshall(xmlMap);
		} catch (JAXBException e) {
			return false;
		} catch (XMLStreamException e) {
			return false;
		}
		grid.resetMap();
		schemas.Point roverLandingSite = map.getRoverLandingSite().getPoint();
		int offsetX = Integer.valueOf(roverLandingSite.getX());
		int offsetY = Integer.valueOf(roverLandingSite.getY());
		
		
		/**** Now that we know the max X and Y coordinates for all points, copy all points from map object to grid map with offset of [MaxX/2, maxY/2] ****/
		// Fill in the GridMap with the information about the ApolloLandingSite with offset [MaxX/2, MaxY/2]
		if (map.getApolloLandingSite() != null && map.getApolloLandingSite().getObstacle() != null && map.getApolloLandingSite().getObstacle().getPointOrArea() != null) {
			// Set the landing site object on the map.
			for (Object object : map.getApolloLandingSite().getObstacle().getPointOrArea()) {
				if (object instanceof Point) {
					schemas.Point point = (schemas.Point) object;
					this.grid.setValue(Double.valueOf(point.getX()) - offsetX, Double.valueOf(point.getY()) - offsetY, Colour.CYAN, false);
				} else if (object instanceof Area) {
					Area area = (Area) object;
					for (schemas.Point point : area.getPoint()) {
						this.grid.setValue(Double.valueOf(point.getX()) - offsetX, Double.valueOf(point.getY()) - offsetY, Colour.CYAN, false);
					}
				}
			}
		}
		
		// Fill in the GridMap with the information about the Boundary with offset [MaxX/2, MaxY/2]
		if (map.getBoundary() != null && map.getBoundary() != null && map.getBoundary().getArea().getPoint() != null) {
			// Set the boundary points onto our map.
			for (schemas.Point point : map.getBoundary().getArea().getPoint()) {
				this.grid.setValue(Double.valueOf(point.getX()) - offsetX, Double.valueOf(point.getY()) - offsetY, Colour.BLUE, false);
			}
		}
		
		// Fill in the GridMap with the information about all obstacles on the map with offset [MaxX/2, MaxY/2]
		if (map.getObstacle() != null) {
			// Draw all obstacles onto our map.
			for (Obstacle obstacle : map.getObstacle()) {
				Object object = obstacle.getPointOrArea();
				if (object instanceof Point) {
					Point point = (Point) object;
					this.grid.setValue(Double.valueOf(point.getX()) - offsetX, Double.valueOf(point.getY()) - offsetY, Colour.GRAY, false);
				} else if (object instanceof Area) {
					Area area = (Area) object;
					for (schemas.Point point : area.getPoint()) {
						this.grid.setValue(Double.valueOf(point.getX()) - offsetX, Double.valueOf(point.getY()) - offsetY, Colour.GRAY, false);
					}
				}
			}
		}
		
		// Fill in the GridMap with the information about the rover landing site with offset [MaxX/2, MaxY/2]
		if (map.getRoverLandingSite() != null && map.getRoverLandingSite().getPoint() != null) {
			// Draw the initial rover landing site position.
			schemas.Point roverLandingPoint = map.getRoverLandingSite().getPoint();
			if (roverLandingPoint != null && roverLandingPoint.getX() != null && roverLandingPoint.getY() != null) {
				this.grid.setRoverLanding(Double.valueOf(roverLandingPoint.getX()) - offsetX, Double.valueOf(roverLandingPoint.getY()) - offsetY);
			}
		}
		
		// Fill in the GridMap with the information about all tracks on the map with offset [MaxX/2, MaxY/2]
		if (map.getTrack() != null) {
			// Draw all tracks to the screen.
			for (Track track : map.getTrack()) {
				Object object = track.getPointOrArea();
				if (object instanceof Point) {
					schemas.Point point = (schemas.Point) object;
					this.grid.setValue(Double.valueOf(point.getX()) - offsetX, Double.valueOf(point.getY()) - offsetY, Colour.RED, false);
				} else if (object instanceof Area) {
					Area area = (Area) object;
					for (schemas.Point point : area.getPoint()) {
						this.grid.setValue(Double.valueOf(point.getX()) - offsetX, Double.valueOf(point.getY()) - offsetY, Colour.RED, false);
					}
				}
			}
		}
		
		// Fill in the GridMap with the information about all zones (crater, ngz, radiation) on the map with offset [MaxX/2, MaxY/2]
		if (map.getZone() != null) {
			// Draw all zones to the screen.
			for (Zone zone : map.getZone()) {
				for (Object object : zone.getCircleOrArea()) {
					if (object instanceof Area) {
						Area area = (Area) object;
						int colour = Colour.WHITE;
						if (zone.getState().equals("unexplored")) {
							continue;
						} else if (zone.getState().equals("radiation")) {
							colour = Colour.GREEN;
						} else if (zone.getState().equals("crater")) {
							colour = Colour.BLACK;
						} else if (zone.getState().equals("nogo")) {
							for (int i=0; i<area.getPoint().size()-1; i+=2) {
								schemas.Point firstpoint = area.getPoint().get(i);
								int firstx = Integer.valueOf(firstpoint.getX()) - offsetX;
								int firsty = Integer.valueOf(firstpoint.getY()) - offsetY;
								schemas.Point secondpoint = area.getPoint().get(i+1);
								int secondx = Integer.valueOf(secondpoint.getX()) - offsetX;
								int secondy = Integer.valueOf(secondpoint.getY()) - offsetY;
								this.grid.addNGZ(firstx, firsty, secondx - firstx, secondy - firsty, controller);
							}
							continue;
						}
						for (schemas.Point point : area.getPoint()) {
							this.grid.setValue(Double.valueOf(point.getX()) - offsetX, Double.valueOf(point.getY()) - offsetY, colour, false);
						}
					}
				}
			}
		}
		
		// Fill in the GridMap with the information about all tracks on the map with offset [MaxX/2, MaxY/2]
		if (map.getVehicleStatus() != null) {
			// Draw all tracks to the screen.
			schemas.Point point = map.getVehicleStatus().getPoint();
			this.grid.setValue(Double.valueOf(point.getX()) - offsetX, Double.valueOf(point.getY()) - offsetY, Colour.WHITE, true);
		}
		
		
		this.grid.force_refresh();
		
		// Return true since the reading was a success.
		return true;
	}
	
	
	/**
	 * Converts the GridMap to the DTD defined LunarRoverMap object and saves that to a file.
	 * 
	 * @param file
	 * @return true of the save operation finished normally.
	 */
	private boolean saveMapToFile(File file) {
		try {
			FileWriter writer = new FileWriter(file);
			
			// Create a new empty saved map.
			Lunarrovermap savedMap = new Lunarrovermap();
						
			// Define Areas, Points and ApolloLandingSite to fill in later.
			Area craterArea = new Area();
			Area radiationArea = new Area();
			Area tracksArea = new Area();
			Area boundaryArea = new Area();
			Area obstacleArea = new Area();
			Area visitedArea = new Area();
			schemas.Point roverLandingSite = new schemas.Point();
			ApolloLandingSite apolloLandingSite = new ApolloLandingSite();
			VehicleStatus status = new VehicleStatus();
			
			// Define zones for craters and radiation. Since we can't read NGZ data from GridMap, can't save NGZ data.
			Zone craters = new Zone();
			craters.setState("crater");
			Zone radiation = new Zone();
			radiation.setState("radiation");
			Zone visited = new Zone();
			visited.setState("explored");
			
			BufferedImage image = this.grid.getFullImage();
			File outputfile = new File("saved.png");
			ImageIO.write(image, "png", outputfile);
			
			// For all pixels/values in the grid...
			for (int i=0; i<image.getWidth(); i++) {
				for (int j=0; j<image.getHeight(); j++) {
					
					// Get the colour of the value and create a point for the x,y value of the pixel.
					int colour = image.getRGB(i, j) & 0xFFFFFF;
					
					schemas.Point point = new schemas.Point();
					
					long x = i*grid.getScale();
					long y = j*grid.getScale();
					point.setX(String.valueOf(x));
					point.setY(String.valueOf(y));
					
					// Depending on the colour sampled, add the point to the relevant predefined area.
					if (colour == Colour.WHITE) {
						visitedArea.getPoint().add(point);
					} else if (colour == Colour.BLACK) {
						craterArea.getPoint().add(point);
					} else if (colour == Colour.GREEN) {
						radiationArea.getPoint().add(point);
					} else if (colour == Colour.RED) {
						tracksArea.getPoint().add(point);
					} else if (colour == Colour.MAGENTA) {
						roverLandingSite = point;
					} else if (colour == Colour.BLUE) {
						boundaryArea.getPoint().add(point);
					} else if (colour == Colour.DARK_GRAY) {
						obstacleArea.getPoint().add(point);
					} else if (colour == Colour.CYAN) {
						Obstacle obstacle = new Obstacle();
						obstacle.getPointOrArea().add(point);
						apolloLandingSite.setObstacle(obstacle);
					} else if (colour == 0x800080) {
						status.setPoint(point);
					} 
				}
			}
			
			
			// Create and add a new boundary from the filled in boundary area.
			Boundary boundary = new Boundary();
			boundary.setArea(boundaryArea);
			savedMap.setBoundary(boundary);
			
			// Create and add a RoverLandingSite from the filled in roverLandingSite point.
			RoverLandingSite site = new RoverLandingSite();
			site.setPoint(roverLandingSite);
			savedMap.setRoverLandingSite(site);
			
			// Create and add all tracks found based on the tracksArea filled in previously.
			Track track = new Track();
			track.getPointOrArea().addAll(tracksArea.getPoint());
			savedMap.getTrack().add(track);
			
			// Create and add vehicle status to the new map.
			savedMap.setVehicleStatus(status);
			
			// Add all gridMap craters to the new map.
			craters.getCircleOrArea().add(craterArea);
			savedMap.getZone().add(craters);
			// Add all radiation areas to the new map.
			radiation.getCircleOrArea().add(radiationArea);
			savedMap.getZone().add(radiation);
			
			// Add all visited Points
			visited.getCircleOrArea().add(visitedArea);
			savedMap.getZone().add(visited);
			
			// Convert the LunarRoverMap to  a string and save it to a file.
			String stringMap = marshallJAXBElement(createJAXBMap(savedMap));
			writer.write(stringMap);
			writer.close();
			return true;
		} catch (IOException e) {
			return false;
		} catch (JAXBException e) {
			return false;
		}
	}
	
	/**
	 * Creates a JAXBElement of type LunarRoverMap from an existing lunarrovermap object.
	 * 
	 * @param map
	 * @return
	 */
	private JAXBElement<Lunarrovermap> createJAXBMap(Lunarrovermap map) {
		return new JAXBElement<Lunarrovermap>(new QName("", "lunarrovermap"), Lunarrovermap.class, null, map);
	}
	
	/**
	 * Marshals a JAXBElement of a generated object class into an XML string represetation.
	 * 
	 * @param element
	 * @return
	 * @throws JAXBException
	 */
    private <T> String marshallJAXBElement(JAXBElement<T> element) throws JAXBException {
        StringWriter writer = new StringWriter();

        JAXBContext context = JAXBContext.newInstance("schemas", ObjectFactory.class.getClassLoader());
        
        Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(element, writer);
        return writer.toString();
    }

    /**
     * Unmarshalls a XML string into a Generated object type.
     * 
     * @param XML
     * @return
     * @throws JAXBException
     * @throws XMLStreamException
     */
    private <T> T unmarshall(String XML) throws JAXBException, XMLStreamException {
        JAXBContext context = JAXBContext.newInstance("schemas", ObjectFactory.class.getClassLoader());

        Unmarshaller unmarshaller = context.createUnmarshaller();

        // Unmarshall element.
        StringReader reader = new StringReader(XML);
        XMLInputFactory factory = XMLInputFactory.newInstance(); // Or newFactory()
        XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);

        return (T) JAXBIntrospector.getValue(unmarshaller.unmarshal(xmlReader));
    }
    
	/**
	 * Adds a listener to the manual/auto control toggle button 
	 */
	private void addManualControlButtonListener() {
		this.manualControlButton.addMouseListener( new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if(isConnected()) {
					controller.toggleControlMode();
					currentState.setText((controller.getMode() == MODE.automatic)? "Automatic" :  "Manual");
					manualControlButton.setText("Switch to "+ ((controller.getMode() == MODE.automatic)? "Manual" : "Automatic"));
					addToLog("Switching to " + ((controller.getMode() == MODE.automatic)? "Automatic\n" :  "Manual\n"));
				}
			}
		});
	}
	
	/**
	 * Creates all directional buttons we need to manually control the rover.
	 * The directional buttons are in an array with the following order:
	 */
	public void createDirectionalButtons() {
		// Create directional buttons.
		this.directionalButtons = new JButton[NO_OF_DIRECTIONAL_BUTTONS];
		// Create the up button.
		this.directionalButtons[DIRECTIONAL_BUTTONS.UP_BUTTON.ordinal()] = new JButton("up");
		this.directionalButtons[DIRECTIONAL_BUTTONS.UP_BUTTON.ordinal()].setIcon(new ImageIcon(UI.class.getResource("key_up.png")));
		// Create the down button.
		this.directionalButtons[DIRECTIONAL_BUTTONS.DOWN_BUTTON.ordinal()] = new JButton("down");
		this.directionalButtons[DIRECTIONAL_BUTTONS.DOWN_BUTTON.ordinal()].setIcon(new ImageIcon(UI.class.getResource("key_down.png")));
		// Create the left button.
		this.directionalButtons[DIRECTIONAL_BUTTONS.LEFT_BUTTON.ordinal()] = new JButton("left");
		this.directionalButtons[DIRECTIONAL_BUTTONS.LEFT_BUTTON.ordinal()].setIcon(new ImageIcon(UI.class.getResource("key_left.png")));
		// Create the right button.
		this.directionalButtons[DIRECTIONAL_BUTTONS.RIGHT_BUTTON.ordinal()] = new JButton("right");
		this.directionalButtons[DIRECTIONAL_BUTTONS.RIGHT_BUTTON.ordinal()].setIcon(new ImageIcon(UI.class.getResource("key_right.png")));
	
		for( int index = 0; index < NO_OF_DIRECTIONAL_BUTTONS; index++ ) {
			this.directionalButtons[index].addMouseListener(listeners.DIRECTIONAL_LISTENERS[index]);
		}
	}
	
	/**
	 * Add listeners so that the rover can be controlled by using the directional arrow keys
	 * on the keyboard, rather than only through the UI buttons.
	 */
	public void addKeyboardButtonListeners() {
	    // Link the keyboard listeners to any element of the UI. Arbitrarily choosing the currentState element.
		// The chosen element is unaffected, it only acts as an anchor to get all of these listeners into the UI.
		this.keyboardListeners = this.currentState.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

	    // Create the up arrow pressed/release actions to call controller functions.
		AbstractAction up_press = new AbstractAction() {
	         public void actionPerformed(ActionEvent e) { 
	         if(isConnected() && !listeners.pressed) {
	        	 controller.upPress(); 
				 addToLog("Forward\n");
				 listeners.pressed = true;
	         	} 
	         }
	    };
		AbstractAction up_release = new AbstractAction() {
	         public void actionPerformed(ActionEvent e) { 
		         if(isConnected()) {
		        	 controller.upRelease();
		        	 listeners.pressed = false;
		        	 } 
		         }
	    };
	    // Created listeners for key up being pressed and released and link them to the actions.
	    this.keyboardListeners.put(KeyStroke.getKeyStroke("pressed UP"), "up_press");
	    this.currentState.getActionMap().put("up_press", up_press);
	    this.keyboardListeners.put(KeyStroke.getKeyStroke("released UP"), "up_release");
	    this.currentState.getActionMap().put("up_release", up_release);
	    
	    // Create the down arrow pressed/release actions to call controller functions.
	    AbstractAction down_press = new AbstractAction() {
	         public void actionPerformed(ActionEvent e) { 
		         if(isConnected() && !listeners.pressed) {
		        	 controller.downPress();
					 addToLog("Backward\n");
					 listeners.pressed = true;
			         }
		         }
	    };
		AbstractAction down_release = new AbstractAction() {
	         public void actionPerformed(ActionEvent e) { 
		         if(isConnected()) {
		        	 controller.downRelease();
		        	 listeners.pressed = false;
		         } 
		     }
	    };
	    // Created listeners for key down being pressed and released and link them to the actions.
	    this.keyboardListeners.put(KeyStroke.getKeyStroke("pressed DOWN"), "down_press");
	    this.currentState.getActionMap().put("down_press", down_press);
	    this.keyboardListeners.put(KeyStroke.getKeyStroke("released DOWN"), "down_release");
	    this.currentState.getActionMap().put("down_release", down_release);
	    
	    // Create the left arrow pressed/release actions to call controller functions.
	    AbstractAction left_press = new AbstractAction() {
	         public void actionPerformed(ActionEvent e) { 
		         if(isConnected() && !listeners.pressed) {
		        	 controller.leftPress();
					 addToLog("Left\n"); 
					 listeners.pressed = true;
		         	}
		         }
	    };
		AbstractAction left_release = new AbstractAction() {
	         public void actionPerformed(ActionEvent e) { 
		         if(isConnected()) {
		        	 controller.leftRelease();
		        	 listeners.pressed = false;
		         } 
		     }
	    };
	    // Created listeners for key left being pressed and released and link them to the actions.
	    this.keyboardListeners.put(KeyStroke.getKeyStroke("pressed LEFT"), "left_press");
	    this.currentState.getActionMap().put("left_press", left_press);
	    this.keyboardListeners.put(KeyStroke.getKeyStroke("released LEFT"), "left_release");
	    this.currentState.getActionMap().put("left_release", left_release);
	    
	    // Create the right arrow pressed/release actions to call controller functions.
	    AbstractAction right_press = new AbstractAction() {
	         public void actionPerformed(ActionEvent e) { 
		         if(isConnected() && !listeners.pressed) {
		        	 controller.rightPress();
					 addToLog("Right\n");
					 listeners.pressed = true;
			          }
		         }
	    };
		AbstractAction right_release = new AbstractAction() {
	         public void actionPerformed(ActionEvent e) { 
		         if(isConnected()) {
		        	 controller.rightRelease();
		        	 listeners.pressed = false;
		         } 
		     }
	    };
	    // Created listeners for key right being pressed and released and link them to the actions.
	    this.keyboardListeners.put(KeyStroke.getKeyStroke("pressed RIGHT"), "right_press");
	    this.currentState.getActionMap().put("right_press", right_press);
	    this.keyboardListeners.put(KeyStroke.getKeyStroke("released RIGHT"), "right_release");
	    this.currentState.getActionMap().put("right_release", right_release);
	}
	
	/**
	 * Create the manual control button to switch between automatic and manual modes.
	 */
	private void createManualControlButton() {
		this.manualControlButton = new JButton("Switch to Automatic");
	}
	
	/**
	 * Loads the example map from the resources file into a JLabel object.
	 */
	private void loadMapImage() {
		this.mapImage = new MapLabel(new ImageIcon(grid.getUIImage().getScaledInstance(map_panel_width,map_panel_height,Image.SCALE_SMOOTH)));		
	}
	
	/**
	 * Creates the activity log text field to log rover status and user interaction.
	 */
	public void createActivityLog() {
		this.activityLog = new JTextArea(35,30);
		this.activityLog.setEditable(false);
	}
	
	/** 
	 * Adds a piece of text to the log (Assuming the text is less than 30 characters long).
	 * Deletes the first line of the log if the number of items goes beyond 35 (height of text area).
	 * @param text - text to add to the log.
	 */
	public void addToLog(String text) {
		this.numberOfLogLines++;
		if (this.numberOfLogLines > 34) {
			int end;
			try {
				end = this.activityLog.getLineEndOffset(0);
				this.activityLog.replaceRange("", 0, end);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			this.numberOfLogLines--;
		}
		this.activityLog.append(text);
	}
	
	/**
	 * Creates the current state label, to be updated every time the switch state button is pressed.
	 */
	public void createCurrentStateLabel() {
		this.currentState = new JLabel("Manual");
	}
	
	public void createSensorDatalabels() {
		this.roverXY = new JLabel("undefined, undefined");
		this.roverAngle = new JLabel("undefined");
		this.roverColour = new JLabel("undefined");
		this.roverDistance = new JLabel("undefined");
	}
	
	/**
	 * Returns the controller connected to the UI.
	 * @return RoverController connected to UI.
	 */
	public RoverController getController() {
		return this.controller;
	}
	
	public boolean isConnected() {
		return this.controller.isConnected();
	}
	
	/**
	 * Validate a entered IP address as being valid or not.
	 * Utilizes voodoo magic found at: https://stackoverflow.com/questions/5667371/validate-ipv4-address-in-java
	 * @param ip address to validate
	 * @return true if ip address is valid.
	 */
	public boolean validate(String ip) {
		Pattern ipregex = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	    return ipregex.matcher(ip).matches();
	}
	
	/**
	 * Returns the IP of the rover to connect to.
	 * @return string representing rover IP.
	 */
	public String getIP() {
        String ipAddress = JOptionPane.showInputDialog(frame, "Enter IP address of Rover.");
		return ipAddress;
	}
	
	/**
	 * Class UIPanel
	 * Provides layout management for each of the major sections:
	 * Menu area, Map area, Activity log area, No-go-zone area, Switch modes area, and Control area.
	 * Uses the GridBagLayout scheme, with each major section having their own internal layout scheme. 
	 */
	public class UIPanel extends JPanel {
		
		public UIPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            
            // Set defaults.
            gbc.gridx = 0;
            gbc.gridy = 0;
            
            // Make every major section fill their section horizontally and be centered.
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.CENTER;
            
            // Menu panel has a grid width of 2 and covers the entire 2 cells.
            gbc.gridwidth = 3;
            gbc.weightx = 1;
            //add(new menuPanel(), gbc);
            
            // Map panel has a grid width of 2 and covers the entire 2 cells, but below the menu panel.
            gbc.gridwidth = 3;
            gbc.weightx = 1;
            gbc.gridy++;
            add(new mapPanel(), gbc);
            
            // Activity panel has a grid width of 2 and covers the entire 2 cells, but on the right side of the map panel.
            gbc.gridwidth = 1;
            gbc.weightx = 1;
            gbc.gridx = 3;
            add(new activityPanel(), gbc);         
                        
            // Sensor state panel has a grid width of 1 and covers the entire cell. Below the map panel, after nogozone.
            gbc.gridwidth = 1;
            gbc.weightx = 1;
            gbc.gridy++;
            gbc.gridx = 1;
            add(new sensorDataDisplayPanel(), gbc);
            
            // Switchmodes panel has a grid width of 1 and covers the entire cell. On the right of the nogozone panel.
            gbc.gridwidth = 1;
            gbc.weightx = 1;
            gbc.gridx++;
            add(new switchModesPanel(), gbc);
            
            // Controlbuttons panel has a grid width of 1 and covers the entire cells with a higher weight then other panels.
            // Appears on the right side of the switchmodes panel.
            gbc.gridwidth = 1;
            gbc.weightx = 2;
            gbc.gridx++;
            add(new controlButtonsPanel(), gbc);
		}
	}
	
	/**
	 * class mapPanel
	 * Provides layout management for the map panel section of the UI.
	 * Simply creates a title centered in a single cell, and places the map centered in a cell
	 * directly underneath the title cell.
	 */
	public class mapPanel extends JPanel {
		
		public mapPanel() {
			setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            // Set defaults.
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(4,4,4,4);
            
            // Align the map area title at the center.
            gbc.anchor = GridBagConstraints.NORTH;
            JLabel map_area = new JLabel("Map Area");
            
            add(map_area, gbc);
            
            // Add the map image directly below the title.
            gbc.gridy++;
            add(mapImage, gbc);
            

        }
	}
	
	/**
	 * class activityPanel
	 * Provides layout management for the activity panel section of the UI.
	 * Simply creates a title centered in a single cell, and places the activity log
	 * centered in a cell directly underneath the title cell.
	 */
	public class activityPanel extends JPanel {
		
		public activityPanel() {
			setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            // Set defaults.
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(4,4,4,4);
            
            // Align the map area title at the center.
            gbc.anchor = GridBagConstraints.CENTER;
            JLabel activityLog_title = new JLabel("Activity Log");
            add(activityLog_title, gbc);
            
            // Add the activity log directly underneath the title, covering the whole cell width and height.
            gbc.gridy++;
            gbc.weightx = 1;
            gbc.weighty = 1;
            add(activityLog, gbc);          
		}
	}
	
	public class sensorDataDisplayPanel extends JPanel {
		public sensorDataDisplayPanel() {
			setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            // Set defaults.
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(4,4,4,4);
            
            // Center the sensor data label.
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = 2;
            JLabel title = new JLabel("Rover Sensor Data");
            add(title,gbc);
            
            // Place the x and y label underneath to the left.
            gbc.gridwidth = 1;
            gbc.gridy++;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel roverXYState = new JLabel("x,y:");
            add(roverXYState, gbc);
            
            // Place the x and y data to the right.
            gbc.gridx++;
            gbc.anchor = GridBagConstraints.WEST;
            add(roverXY, gbc);
            
            // Place the angle label underneath to the left.
            gbc.gridwidth = 1;
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel roverAngleState = new JLabel("Angle:");
            add(roverAngleState, gbc);
            
            // Place the angle data to the right.
            gbc.gridx++;
            gbc.anchor = GridBagConstraints.WEST;
            add(roverAngle, gbc);
            
            // Place the colour label underneath to the left.
            gbc.gridwidth = 1;
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel roverColourState = new JLabel("Colour:");
            add(roverColourState, gbc);
            
            // Place the colour data to the right.
            gbc.gridx++;
            gbc.anchor = GridBagConstraints.WEST;
            add(roverColour, gbc);
            
            // Place the distance label underneath to the left.
            gbc.gridwidth = 1;
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel roverDistanceState = new JLabel("Distance:");
            add(roverDistanceState, gbc);
            
            // Place the distance data to the right.
            gbc.gridx++;
            gbc.anchor = GridBagConstraints.WEST;
            add(roverDistance, gbc);
		}
	}
	
	/**
	 * class switchModesPanel
	 * Provides layout management for the switch modes part of the UI.
	 * Contains a centered button to switch modes, with text underneath to specify which state the
	 * rover is in (manual or automatic).
	 */
	public class switchModesPanel extends JPanel {
		
		public switchModesPanel() {
			setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            // Set defaults.
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(4,4,4,4);
            
            // Center the switch modes button at the centre of the top cell, with a width of 2 cells.
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.gridwidth = 2;
            add(manualControlButton,gbc);
            
            // Place the current state text label underneath the button, aligned to the right side of the left cell.
            gbc.gridwidth = 1;
            gbc.gridy++;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel control_state = new JLabel("Current State:");
            add(control_state, gbc);
            
            // Place the "manual" or "automatic" label underneath the button, aligned to the left side of the right cell.
            gbc.gridx++;
            gbc.anchor = GridBagConstraints.WEST;
            add(currentState, gbc);
		}
	}
	
	/**
	 * class controlButtonsPanel
	 * Provides layout management for the manual control buttons of the UI.
	 */
	public class controlButtonsPanel extends JPanel {
		
		public controlButtonsPanel() {
			setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            // Set defaults.
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(4,4,4,4);
            
            // Create the control buttons title, centered with a grid with of 3 (wide enough to cover all buttons under it).
            gbc.gridwidth = 3;
            gbc.anchor = GridBagConstraints.CENTER;
            JLabel control_title = new JLabel("Control Buttons");
            add(control_title, gbc);
            
            // Create the key_up button aligned to the bottom in the center cell, below the title.
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.SOUTH;
            gbc.gridy = 1;
            gbc.gridx = 1;
            add(directionalButtons[DIRECTIONAL_BUTTONS.UP_BUTTON.ordinal()], gbc);
            
            // Creates the key_down button aligned to the top in the cell beneath the key_up button.
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.gridy = 2;
            gbc.gridx = 1;
            add(directionalButtons[DIRECTIONAL_BUTTONS.DOWN_BUTTON.ordinal()], gbc);
            
            // Creates the key_left button aligned to the right in the cell to the left of the key_down button.
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridy = 2;
            gbc.gridx = 0;
            add(directionalButtons[DIRECTIONAL_BUTTONS.LEFT_BUTTON.ordinal()], gbc);
            
            // Creates the key_right button aligned to the left in the cell to the right of the key_down button.
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridy = 2;
            gbc.gridx = 2;
            add(directionalButtons[DIRECTIONAL_BUTTONS.RIGHT_BUTTON.ordinal()], gbc);
		}
	}
	
	public class MapLabel extends JLabel {
		        
        double start_x;
        double start_y;
        double end_x;
        double end_y;
        
        boolean pressed = false;
        boolean leftClick = true;
        
		public MapLabel(ImageIcon icon) {
			
			this.setIcon(icon);
			this.setVisible(true);
			this.addMouseListener(new MouseAdapter() {
	            
	            @Override
	            public void mousePressed(MouseEvent e) {
	            	if(!pressed) {
	            		pressed = true;
	                    start_x = e.getX();
	                    start_y = e.getY();
	            	} 
	            	leftClick = SwingUtilities.isLeftMouseButton(e);
	            }

	            @Override
	            public void mouseReleased(MouseEvent e) {
	            	if(pressed) {
	            		pressed = false;
	                    end_x = e.getX();
	                    end_y = e.getY();
	            		if(e.getButton() == MouseEvent.BUTTON1)
	            		{
	            			// Left Click
	            			if(controller.isConnected()) {
	            				grid.addNGZ(Math.min(start_x, end_x), Math.min(start_y, end_y), Math.max(start_x, end_x), Math.max(start_y, end_y), controller);
	            				addToLog(String.format("Adding NGZ %f %f %f %f\n", start_x, Math.min(-start_y, -end_y), end_x, Math.max(-start_y, -end_y)));
	            			}
	            		}
	            		else if(e.getButton() == MouseEvent.BUTTON3) {
	            			// Right Click
	            			if(controller.isConnected()) {
	            				grid.removeNGZ(Math.min(start_x, end_x), Math.min(start_y, end_y), Math.max(start_x, end_x), Math.max(start_y, end_y), controller);    
	            				addToLog(String.format("Removing NGZ %f %f %f %f\n", start_x, Math.min(-start_y, -end_y), end_x, Math.max(-start_y, -end_y)));
	            			}
	            		}
	            	}
	            	start_x = -1;
	            }
	        });
			
			this.addMouseWheelListener(new MouseWheelListener() {

				public void mouseWheelMoved(MouseWheelEvent e) {
					grid.zoom(e.getWheelRotation());
				}
				
			});
			
			this.addMouseMotionListener(new MouseMotionListener() {

				public void mouseDragged(MouseEvent e)
				{
					end_x = e.getX();
                    end_y = e.getY();
					MapLabel.this.repaint();
				}

				public void mouseMoved(MouseEvent e) 
				{

				}
			});
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			//Draw selection box for setting NGZ
			if (start_x != -1) {
				
				if (!leftClick) {
					g.setColor(Color.RED);
				}
				int startX = (int)Math.min(start_x, end_x);
				int endX = (int)Math.max(start_x, end_x);
				int startY = (int)Math.min(start_y, end_y);
				int endY = (int)Math.max(start_y, end_y);
				
				int width = endX - startX;
				int height = endY - startY;
				
				g.drawRect(startX, startY, width, height);
			}
			g.setColor(Color.GRAY);
			
			int x_scale = (int)(this.getWidth()/grid.getUIImage().getWidth());
			int y_scale = (int)(this.getHeight()/grid.getUIImage().getHeight());
			
			//Draw vertical grid lines
			for (int i=0; i<=grid.getUIImage().getWidth(); i++)
			{
				g.drawLine(i*x_scale, 0, i*x_scale, this.getHeight());
			}
			g.drawLine(this.getWidth()-1, 0, this.getWidth()-1, this.getHeight());
			
			//Draw horizontal grid lines
			for (int i=0; i<=grid.getUIImage().getHeight(); i++)
			{
				g.drawLine(0, this.getHeight() - i*y_scale, this.getWidth(), this.getHeight() - i*y_scale);
			}
			g.drawLine(0, this.getHeight()-1, this.getWidth(), this.getHeight()-1);
		}
		
		
	}

	/**
	 * Class to read sensor data from a server socket, and print that to the GUI.
	 * The Map Area should also move to see the rover
	 */
	private class RefreshMapData extends Thread {
		
			public void run() {
			while (true) {
				// Refresh Map every 30 seconds
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(grid.is_outdated()) mapImage.setIcon(new ImageIcon(grid.getUIImage().getScaledInstance(map_panel_width,map_panel_height,Image.SCALE_SMOOTH)));
				
			}
		
		}
	}
}

