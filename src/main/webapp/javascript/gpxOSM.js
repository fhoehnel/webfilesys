var TRACK_COLORS = [
    "#002080",
    "#802000",
    "#008020"
];

var OSM_SLOW_MOTION_TRACK_COLORS = [
    "#b000b0",
    "#ff4040",
    "#ffff00",
    "#00c0ff",
    "#0080a0"
];

var globalTrackCounter = 0;

var overallBounds = null;

var osmMap = null;

function showOSMTracks(gpxFilePath) {
    document.getElementById("mapDiv").style.height = (window.innerHeight - 30 )+ "px";
    const map = new OpenLayers.Map("mapDiv");
    osmMap = map;
    map.addLayer(new OpenLayers.Layer.OSM());

    showSingleOSMTrack(gpxFilePath, map);
}

function showSingleOSMTrack(gpxFilePath, map) {

    const parameters = {
        filePath: encodeURIComponent(gpxFilePath),
        trackNumber: currentTrack
    }

    fetchGet("gpxTrack", parameters,
        responseText => {
            const response = JSON.parse(responseText);
            if (response.trackpoints && response.trackpoints.length > 0) {
                const trackPoints = response.trackpoints;
                showTrackOnOSMMap(trackPoints, map);
               	showTrackMetaData(response, "osm");
               	if (response.hasElevation) {
                   	drawAltDistProfile(response);
               	}
               	if (response.hasRecordedSpeed) {
               		drawSpeedProfile(response, "recordedSpeed", "averageRecordedSpeedInMotion");
               	} else {
                   	if (response.hasSpeed) {
                   		if (!response.invalidTime) {
                           	drawSpeedProfile(response, "speed", "averageCalculatedSpeedInMotion");
                   		} else {
                   			customAlert("GPX file contains invalid time data - omitting speed profile")
                   		}
                   	}
               	}
                currentTrack++;
                if (currentTrack < trackNumber) {
                    showSingleOSMTrack(gpxFilePath, map);
                } else {
                    loadAndShowWayPointsOSM(map, gpxFilePath);
                }
            }
        },
        null,
        true,
        false
    );
}

function loadAndShowWayPointsOSM(map, gpxFilePath) {
    const pois = new OpenLayers.Layer.Text("My Points", {
                           location:"/webfilesys/servlet?command=osmWayPoints&filePath=" + encodeURIComponent(gpxFilePath),
                           projection: map.displayProjection
                       });
    map.addLayer(pois);
}

function showMultipleOSMTracks() {
    document.getElementById("mapDiv").style.height = (window.innerHeight - 30 )+ "px";
    const map = new OpenLayers.Map("mapDiv");
    map.addLayer(new OpenLayers.Layer.OSM());

    showNextOSMTrack(map);
}

function showNextOSMTrack(map) {

    const filePath = gpxFiles.pop();
    const parameters = {
        filePath: encodeURIComponent(filePath),
        trackNumber: "0"
    }
    fetchGet("gpxTrack", parameters,
        responseText => {
            const response = JSON.parse(responseText);
            showTrackOnOSMMap(response.trackpoints, map);
            showTrackMetaData(response, "osm");
            if (gpxFiles.length > 0) {
                showNextOSMTrack(map);
            }
        },
        null,
        true,
        false
    );
}

function showTrackOnOSMMap(trackPoints, map) {
    const fromProjection = new OpenLayers.Projection("EPSG:4326");
    const toProjection = map.getProjectionObject();

    const coordinates = [];
    for (let i = 0; i < trackPoints.length; i++) {
        coordinates.push(new OpenLayers.Geometry.Point(trackPoints[i].lon, trackPoints[i].lat).transform(fromProjection, toProjection));
    }

    const trackColor = TRACK_COLORS[globalTrackCounter % TRACK_COLORS.length];

    const lines = new OpenLayers.Layer.Vector("Track Line");
    const lineFeature = new OpenLayers.Feature.Vector(
        new OpenLayers.Geometry.LineString(coordinates),
        {},
        {
            strokeColor: trackColor,
            strokeWidth: 4
        }
    );
    lines.addFeatures([lineFeature]);
    map.addLayer(lines);

    if (overallBounds == null) {
        overallBounds = lineFeature.geometry.getBounds();
    } else {
        overallBounds.extend(lineFeature.geometry.getBounds());
    }
    map.zoomToExtent(overallBounds);

    globalTrackCounter++;
    globalTrackMap[globalTrackCounter - 1] = lines;
}