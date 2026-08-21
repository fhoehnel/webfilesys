package de.webfilesys.gui.user;

import com.ctc.wstx.exc.WstxParsingException;
import org.apache.logging.log4j.LogManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Delivers waypoint data extracted from GPX track as text file for Open Street Map.
 * 
 * @author Frank Hoehnel
 */
public class OSMWaypointHandler extends UserRequestHandler {

	public OSMWaypointHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}	
	
	protected void process() {

	    String filePath = getParameter("filePath");

        resp.setContentType("text/plain");
        output.println("lat\tlon\ttitle\tdescription\ticon\ticonSize\ticonOffset");

        BufferedReader gpxReader = null;

		try {
			gpxReader = new BufferedReader(new FileReader(filePath));

			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(gpxReader);

			String currentElementName = null;

			String tagName = null;

			boolean documentEnd = false;

			boolean dataInvalid = false;
			
			boolean fatalError = false;

			ArrayList<WayPoint> wayPoints = new ArrayList<WayPoint>();
			
			WayPoint currentWayPoint = null;
			
			while (!documentEnd && !fatalError) {
				try {
					int event = parser.next();

					switch (event) {
					case XMLStreamConstants.END_DOCUMENT:
						parser.close();
						documentEnd = true;
						break;

					case XMLStreamConstants.START_DOCUMENT:
						break;

					case XMLStreamConstants.START_ELEMENT:

						tagName = parser.getLocalName();
						currentElementName = tagName;

						if (tagName.equals("wpt")) {
							currentWayPoint = new WayPoint();
							String lat = parser.getAttributeValue(null, "lat");
							String lon = parser.getAttributeValue(null, "lon");
							try {
								double latitude = Double.parseDouble(lat);
								double longitude = Double.parseDouble(lon);
                                currentWayPoint.setLat(latitude);
                                currentWayPoint.setLon(longitude);
    							wayPoints.add(currentWayPoint);
							} catch (NumberFormatException numEx) {
								dataInvalid = true;
								LogManager.getLogger(getClass()).debug(numEx, numEx);
							}
						}
						
						break;

					case XMLStreamConstants.END_ELEMENT:

						tagName = parser.getLocalName();
						if (tagName.equals("wpt")) {
						    LogManager.getLogger(getClass()).debug("end waypoint tag with name: " + currentWayPoint.getName());
						    currentWayPoint = null;
						}							
						break;

					case XMLStreamConstants.CHARACTERS:

						if (currentWayPoint != null) {
							String elementText = parser.getText().trim();

							if (currentElementName.equals("ele")) {
								if (!elementText.isEmpty()) {
								    currentWayPoint.setEle(elementText);
								}
							} else if (currentElementName.equals("name")) {
								if (!elementText.isEmpty()) {
								    currentWayPoint.setName(elementText);
								}
							} 
						}
						
						break;
					default:
						// LogManager.getLogger(getClass()).debug("unhandled event: " + event);
					}
				} catch (WstxParsingException epex) {
					LogManager.getLogger(getClass()).warn("GPX parsing error", epex);
					fatalError = true;
				}
			}

            for (WayPoint wayPoint : wayPoints) {
                output.print(wayPoint.getLat());
                output.print('\t');
                output.print(wayPoint.getLon());
                output.print('\t');
                output.print(wayPoint.getName());
                output.print('\t');
                String altitude = "";
                if (wayPoint.getEle() != null) {
                    int dotIdx = wayPoint.getEle().indexOf('.');
                    if (dotIdx > 0) {
                        altitude = wayPoint.getEle().substring(0, dotIdx);
                    }
                    altitude += " m";
                }
                output.print(altitude);
                output.print('\t');
                output.print("/webfilesys/images/OSMaps.png");
                output.print('\t');
                output.print("32,32");
                output.print('\t');
                output.println("-16,-16");
            }
			output.flush();
			
			if (dataInvalid) {
			    LogManager.getLogger(getClass()).warn("GPX file contains invalid data: " + filePath);
			}
		} catch (IOException ioex) {
			LogManager.getLogger(getClass()).error("failed to read GPX file", ioex);
		} catch (XMLStreamException xmlEx) {
			LogManager.getLogger(getClass()).error("error parsing XML stream", xmlEx);
		} catch (Exception e) {
			LogManager.getLogger(getClass()).error("failed to transform GPX file", e);
		} finally {
			if (gpxReader != null) {
				try {
					gpxReader.close();
				} catch (Exception ex) {
				}
			}
		}
	}
	
	public class WayPoint {
		private double lat = 0;
		private double lon = 0;
		private String ele = null;
		private String time = null;
		private String name = null;
		
		public void setLat(double newVal) {
			lat = newVal;
		}
		
		public double getLat() {
			return lat;
		}

		public void setLon(double newVal) {
			lon = newVal;
		}
		
		public double getLon() {
			return lon;
		}

		public void setEle(String newVal) {
			ele = newVal;
		}
		
		public String getEle() {
			return ele;
		}

		public void setTime(String newVal) {
			time = newVal;
		}
		
		public String getTime() {
			return time;
		}

		public void setName(String newVal) {
			name = newVal;
		}
		
		public String getName() {
			return name;
		}
	}

}
