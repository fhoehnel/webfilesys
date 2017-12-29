package de.webfilesys.gui.user;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

import com.ctc.wstx.exc.WstxParsingException;

import de.webfilesys.util.ISO8601DateParser;

/**
 * Delivers data extracted from GPX track as JSON.
 * 
 * @author Frank Hoehnel
 */
public class GPXTrackHandler extends UserRequestHandler {

	private static final int DISTANCE_SMOOTH_FACTOR = 12;

	public GPXTrackHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}	
	
	protected void process() {

	    String filePath = getParameter("filePath"); 
		
		int trackNumber = 0;
		String trackNumberParam = getParameter("trackNumber");
		try {
			trackNumber = Integer.parseInt(trackNumberParam);
		} catch (Exception ex) {
		}
		
		double[] distanceBuffer = new double[DISTANCE_SMOOTH_FACTOR];

		for (int i = 0; i < DISTANCE_SMOOTH_FACTOR; i++) {
			distanceBuffer[i] = 0.0;
		}

		double[] durationBuffer = new double[DISTANCE_SMOOTH_FACTOR];

		for (int i = 0; i < DISTANCE_SMOOTH_FACTOR; i++) {
			durationBuffer[i] = 0.0;
		}

		BufferedReader gpxReader = null;

		try {
			resp.setContentType("application/json");

			gpxReader = new BufferedReader(new FileReader(filePath));

			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(gpxReader);

			double prevLat = Double.MIN_VALUE;
			double prevLon = Double.MIN_VALUE;

			double startPointLat = Double.MIN_VALUE;
			double startPointLon = Double.MIN_VALUE;

			long prevTime = 0L;

			String currentElementName = null;

			double dist = Double.MIN_VALUE;
			double totalDist = 0.0f;
			double distFromStart = Double.MIN_VALUE;
			double speed = Double.MIN_VALUE;
			
			String elevation = null;

			String tagName = null;

			boolean documentEnd = false;

			String ignoreUnknownTag = null;
			boolean invalidTime = false;
			
			int trackCounter = 0;
			
			boolean currentTrack = false;
			
			boolean firstTrackpoint = true;

			boolean startTimeSet = false;
			
			long startTime = 0;
			
			long endTime = 0;
			
			long timestamp = 0;
			
			String trackName = null;
			
			boolean dataInvalid = false;
			
			boolean hasElevation = false;
			
			boolean hasSpeed = false;
			
			while (!documentEnd) {
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

						if (ignoreUnknownTag != null) {
							break;
						}

						tagName = parser.getLocalName();

						currentElementName = tagName;

						if (tagName.equals("trk")) {
							
							if (trackCounter == trackNumber) {
								currentTrack = true;

								prevLat = Double.MIN_VALUE;
								prevLon = Double.MIN_VALUE;
								startPointLat = Double.MIN_VALUE;
								startPointLon = Double.MIN_VALUE;
								prevTime = 0L;
								dist = Double.MIN_VALUE;
								totalDist = 0.0f;
								distFromStart = Double.MIN_VALUE;
								speed = Double.MIN_VALUE;
							} else {
								currentTrack = false;
							}
							
							trackCounter++;
						}

						if (currentTrack) {

							if ((!tagName.equals("gpx")) && (!tagName.equals("trk")) && (!tagName.equals("trkseg"))
									&& (!tagName.equals("trkpt")) && (!tagName.equals("wpt")) && (!tagName.equals("ele"))
									&& (!tagName.equals("time")) && (!tagName.equals("metadata"))
									&& (!tagName.equals("name")) && (!tagName.equals("desc"))) {
								ignoreUnknownTag = tagName;
								break;
							}
							
							if (tagName.equals("trkpt") || tagName.equals("wpt")) {
								
								if (currentTrack) {
									if (firstTrackpoint) {
										output.println("{\"trackpoints\": [");
										firstTrackpoint = false;
									} else {
										output.println(",");
									}

									output.println("{");
									
									speed = Double.MIN_VALUE;
									
									elevation = null;
									
									timestamp = 0;

									String lat = parser.getAttributeValue(null, "lat");
									String lon = parser.getAttributeValue(null, "lon");

									if ((lat != null) && (lon != null)) {
										output.println("\"lat\": \"" + lat + "\",");
										output.print("\"lon\": \"" + lon + "\"");

										try {
											double latitude = Double.parseDouble(lat);
											double longitude = Double.parseDouble(lon);

											if (prevLat == Double.MIN_VALUE) {
												dist = 0.0f;
											} else {
												dist = calculateDistance(prevLat, prevLon, latitude, longitude);

												for (int i = 0; i < DISTANCE_SMOOTH_FACTOR - 1; i++) {
													distanceBuffer[i] = distanceBuffer[i + 1];
												}
												distanceBuffer[DISTANCE_SMOOTH_FACTOR - 1] = dist;
											}

											totalDist += dist;

											prevLat = latitude;
											prevLon = longitude;

											if ((startPointLat == Double.MIN_VALUE) || (startPointLon == Double.MIN_VALUE)) {
												distFromStart = 0.0f;
												startPointLat = latitude;
												startPointLon = longitude;
											} else {
												distFromStart = calculateDistance(startPointLat, startPointLon, latitude,
														longitude);
											}
										} catch (NumberFormatException numEx) {
											dataInvalid = true;
											Logger.getLogger(getClass()).debug(numEx, numEx);
										}
									}
								}
							}

							if (tagName.equals("trkpt") || tagName.equals("wpt")) {
								if (dist != Double.MIN_VALUE) {
									output.print(",\n\"dist\": \"");
									if (dist < 0.001f) {
										// prevent exponential format
										output.print("0.0");
									} else {
										output.print(dist);
									}
									output.print("\"");
								}

								output.print(",\n\"totalDist\": \"");
								if (totalDist < 0.001f) {
									// prevent exponential format
									output.print("0.0");
								} else {
									output.print(totalDist);
								}
								output.print("\"");

								if (distFromStart != Double.MIN_VALUE) {
									output.print(",\n\"distFromStart\": \"");
									if (distFromStart < 0.001f) {
										// prevent exponential format
										output.print("0.0");
									} else {
										output.print(distFromStart);
									}
									output.print("\"");
								}
							}

							if (tagName.equals("name")) {
								trackName = parser.getElementText();
							}
						}
						
						break;

					case XMLStreamConstants.END_ELEMENT:

						if (currentTrack) {
							tagName = parser.getLocalName();

							if (ignoreUnknownTag != null) {
								if (tagName.equals(ignoreUnknownTag)) {
									ignoreUnknownTag = null;
								}
								break;
							}

							if (tagName.equals("trkpt")) {
								if (elevation != null) {
									output.print(",\n\"ele\": \"" + elevation + "\"");
								}
								if (timestamp > 0) {
									output.print(",\n\"time\": \"" + timestamp + "\"");
								}
								output.println("}");
							} else if (tagName.equals("trk")) {
								output.println("\n]");
								
								if (startTimeSet) {
									output.print(",\n\"startTime\": \"" + startTime + "\"");
									
									if (endTime > 0) {
										output.print(",\n\"endTime\": \"" + endTime + "\"");
									}
								}
								
								if (trackName != null) {
									output.print(",\n\"trackName\": \"" + trackName + "\"");
								}
								
								if (hasElevation) {
									output.print(",\n\"hasElevation\": true");
								}

								if (hasSpeed) {
									output.print(",\n\"hasSpeed\": true");
								}
								
							    if (invalidTime) {
									output.print(",\n\"invalidTime\": true");
								}
								output.println("\n}");
								
								currentTrack = false;
							} else if (tagName.equals("time")) {
								double correctedSpeed = speed;
								if (correctedSpeed < 0.001f) {
									// prevent exponential format
									correctedSpeed = 0.0f;
								}

								if (speed != Double.MIN_VALUE) {
									output.print(",\n\"speed\": \"" + correctedSpeed + "\"");
								}
							}
						}
						
						break;

					case XMLStreamConstants.CHARACTERS:
						
						if (currentTrack) {
							if (ignoreUnknownTag != null) {
								break;
							}

							String elementText = parser.getText().trim();

							if (currentElementName.equals("ele")) {
								if (elementText.length() > 0) {
								    elevation = elementText;
								    hasElevation = true;
								}
							} else if (currentElementName.equals("time")) {
								if (elementText.length() > 0) {
									try {
										long trackPointTime = ISO8601DateParser.parse(elementText).getTime();

										timestamp = trackPointTime;
										
										if (!startTimeSet) {
											startTime = trackPointTime;
											startTimeSet = true;
										} else {
											endTime = trackPointTime;
										}
										
										if ((prevTime > 0L) && (dist != Double.MIN_VALUE)) {
											double duration = trackPointTime - prevTime;

											if (duration < 0) {
												invalidTime = true;
												speed = 0.0f;
											} else {
												for (int i = 0; i < DISTANCE_SMOOTH_FACTOR - 1; i++) {
													durationBuffer[i] = durationBuffer[i + 1];
												}
												durationBuffer[DISTANCE_SMOOTH_FACTOR - 1] = duration;

												if (duration == 0l) {
													speed = 0.0f;
												} else {
													double bufferedDuration = 0.0;
													for (int i = 0; i < DISTANCE_SMOOTH_FACTOR; i++) {
														bufferedDuration += durationBuffer[i];
													}

													double bufferedDistance = 0.0;
													for (int i = 0; i < DISTANCE_SMOOTH_FACTOR; i++) {
														bufferedDistance += distanceBuffer[i];
													}

													speed = bufferedDistance / (bufferedDuration / 1000f);
												}
											}
											
											hasSpeed = true;
										}

										prevTime = trackPointTime;
									} catch (Exception ex) {
										Logger.getLogger(getClass()).error(ex, ex);

										prevTime = 0L;
									}
								}
							}
						}
						
						break;
					default:
						// Logger.getLogger(getClass()).debug("unhandled event: " + event);
					}
				} catch (WstxParsingException epex) {
					Logger.getLogger(getClass()).warn("GPX parsing error", epex);
				}
			}

			if (trackCounter <= trackNumber) {
				output.println("{\"trackpoints\": []}");			
			}
			
			output.flush();
			
			if (dataInvalid) {
			    Logger.getLogger(getClass()).warn("GPX file contains invalid data: " + filePath);
			}
			
			if (invalidTime) {
				Logger.getLogger(getClass()).warn(
						"invalid trkpt time (before previous timestamp) in GPX file: " + filePath);
			}
		} catch (IOException ioex) {
			Logger.getLogger(getClass()).error("failed to read target file", ioex);
		} catch (XMLStreamException xmlEx) {
			Logger.getLogger(getClass()).error("error parsing XML stream", xmlEx);
		} catch (Exception e) {
			Logger.getLogger(getClass()).error("failed to transform GPX file", e);
		} finally {
			if (gpxReader != null) {
				try {
					gpxReader.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	/**
	 * Berechnet die Entfernung zwischen zwei Koordinaten in Metern.
	 *
	 * @param ax
	 *            Breite der ersten Koordinate in Dezimalgrad
	 * @param ay
	 *            Laenge der ersten Koordinate in Dezimalgrad
	 * @param bx
	 *            Breite der zweiten Koordinate in Dezimalgrad
	 * @param by
	 *            Laenge der zweiten Koordinate in Dezimalgrad
	 * @return Distanz in Metern
	 */
	double calculateDistance(double ax, double ay, double bx, double by) {

		if ((ax == bx) && (ay == by)) {
			return 0.0f;
		}

		double x = 1.0f / 298.257223563f; // Abplattung der Erde

		double a = 6378137.0f / 1000.0f; // Aequatorradius der Erde in km

		double f = (ax + bx) / 2.0f;

		double g = (ax - bx) / 2.0f;

		double l = (ay - by) / 2.0f;

		// auf Bogenmass bringen

		f = (Math.PI / 180.0f) * f;

		g = (Math.PI / 180.0f) * g;

		l = (Math.PI / 180.0f) * l;

		double s = Math.pow(Math.sin(g), 2) * Math.pow(Math.cos(l), 2)
				+ Math.pow(Math.cos(f), 2) * Math.pow(Math.sin(l), 2);

		double c = Math.pow(Math.cos(g), 2) * Math.pow(Math.cos(l), 2)
				+ Math.pow(Math.sin(f), 2) * Math.pow(Math.sin(l), 2);

		double w = Math.atan(Math.sqrt(s / c));

		double d = 2.0f * w * a;

		double r = Math.sqrt(s * c) / w;

		double h1 = (3.0f * r - 1.0f) / (2.0f * c);

		double h2 = (3.0f * r + 1.0f) / (2.0 * s);

		return (1000.0f * d * (1.0f + x * h1 * Math.pow(Math.sin(f), 2) * Math.pow(Math.cos(g), 2)
				- x * h2 * Math.pow(Math.cos(f), 2) * Math.pow(Math.sin(g), 2)));
	}

}
