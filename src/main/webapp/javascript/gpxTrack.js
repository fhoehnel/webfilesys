var trackPointList = new Array();

var bounds;

var map;

function handleGoogleMapsApiReady() {
    var mapCenter = new google.maps.LatLng(0, 0);
      
    var mapOptions = {
        zoom: 11,
        center: mapCenter,
        mapTypeId: google.maps.MapTypeId.HYBRID
    }
      
    map = new google.maps.Map(document.getElementById("mapCont"), mapOptions);      
      
    bounds = new google.maps.LatLngBounds();
    
    if (gpxFiles) {
    	loadAndShowMultipleGPXFiles();
    } else {
        loadAndShowTrack();    
    }
}

function loadGoogleMapsAPIScriptCode(googleMapsAPIKey) {
    var script = document.createElement("script");
    script.type = "text/javascript";
      
    if (window.location.href.indexOf("https") == 0) {
        script.src = "https://maps.google.com/maps/api/js?callback=handleGoogleMapsApiReady&key=" + googleMapsAPIKey;
    } else {
        script.src = "http://maps.google.com/maps/api/js?callback=handleGoogleMapsApiReady&key=" + googleMapsAPIKey;
    }      
    document.body.appendChild(script);
}

function loadAndShowTrack() {
	
    var url = "/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(filePath) + "&trackNumber=" + currentTrack;
    
    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
            	var response = JSON.parse(req.responseText);
            
            	showTrackOnMap(response.trackpoints);
            	
                currentTrack++;
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });      
}

function loadAndShowMultipleGPXFiles() {
	var filePath = gpxFiles.pop();
	
    var url = "/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(filePath) + "&trackNumber=0";
    
    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
            	var response = JSON.parse(req.responseText);
            
            	showTrackOnMap(response.trackpoints);
            	
            	if (gpxFiles.length > 0) {
            		loadAndShowMultipleGPXFiles();
            	}
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });      
}

function showTrackOnMap(trackpoints) {

	trackPointList = new Array();
	
	for (var i = 0; i < trackpoints.length; i++) {
		
        var latLon = new google.maps.LatLng(trackpoints[i].lat, trackpoints[i].lon);
        trackPointList.push(latLon);
        bounds.extend(latLon);
	}
                       
    var trackPath = new google.maps.Polyline({
        path: trackPointList,
        strokeColor: "#ffff00",
        strokeOpacity: 0.8,
        strokeWeight: 4
    });
         
    trackPath.setMap(map);
                       
    map.fitBounds(bounds);
}
