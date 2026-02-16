var TRACK_COLORS = [
    "#008020",
    "#802000",
    "#002080"
];

var globalTrackCounter = 0;

var overallBounds = null;

function showTracksOnOSM(gpxFilePath) {
    showMap(gpxFilePath);
}

function showMap(gpxFilePath) {
    document.getElementById("mapDiv").style.height = (window.innerHeight - 20 )+ "px";
    const map = new OpenLayers.Map("mapDiv");
    map.addLayer(new OpenLayers.Layer.OSM());

    fetchGet("gpxTrack", { filePath: encodeURIComponent(gpxFilePath) },
        responseText => {
            const response = JSON.parse(responseText);
            if (response.trackpoints && response.trackpoints.length > 0) {

                const trackPoints = response.trackpoints;
                const fromProjection = new OpenLayers.Projection("EPSG:4326");
                const toProjection = map.getProjectionObject();

                const coordinates = [];
                for (let i = 0; i < trackPoints.length; i++) {
                    coordinates.push(new OpenLayers.Geometry.Point(trackPoints[i].lon, trackPoints[i].lat).transform(fromProjection, toProjection));
                }

                const lines = new OpenLayers.Layer.Vector("Track Line");
                const lineFeature = new OpenLayers.Feature.Vector(
                    new OpenLayers.Geometry.LineString(coordinates),
                    {},
                    {
                        strokeColor: "#002080",
                        strokeWidth: 4
                    }
                );
                lines.addFeatures([lineFeature]);
                map.addLayer(lines);

                const lineExtent = lineFeature.geometry.getBounds();
                map.zoomToExtent(lineExtent);

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

               	loadAndShowWayPointsOSM(map, gpxFilePath);
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
    document.getElementById("mapDiv").style.height = (window.innerHeight - 20 )+ "px";
    const map = new OpenLayers.Map("mapDiv");
    map.addLayer(new OpenLayers.Layer.OSM());

    showSingleOSMTrack(map);
}

function showSingleOSMTrack(map) {

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
                showSingleOSMTrack(map);
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
}