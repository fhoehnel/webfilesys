function showTracksOnOSM(gpxFilePath) {
    showMap(gpxFilePath);
}

function showMap(gpxFilePath) {
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
            }
        },
        null,
        true,
        false
    );
}
