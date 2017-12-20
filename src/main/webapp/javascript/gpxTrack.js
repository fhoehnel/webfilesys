var TRACK_COLORS = [
	"#ffff00", 
	"#ff4040", 
	"#00c0ff" 
];

var globalTrackCounter = 0;

var trackPointList = new Array();

var bounds;

var map;

function handleGoogleMapsApiReady() {

    var spaceForMetaData = 120;
    
    if (typeof(gpxFiles) === "undefined") {
        spaceForMetaData = 50;
    }

	document.getElementById("mapCont").style.height = (getWinHeight() - spaceForMetaData) + "px";
	
    var mapCenter = new google.maps.LatLng(0, 0);
      
    var mapOptions = {
        zoom: 11,
        center: mapCenter,
        mapTypeId: google.maps.MapTypeId.HYBRID
    }
      
    map = new google.maps.Map(document.getElementById("mapCont"), mapOptions);      
      
    bounds = new google.maps.LatLngBounds();
    
    if (typeof(gpxFiles) != "undefined") {
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
            
            	if (response.trackpoints && (response.trackpoints.length > 0)) {
                	showTrackOnMap(response.trackpoints);

                	showTrackMetaData(response);
                	
                	currentTrack++;
                	
                	if (currentTrack < trackNumber) {
                		loadAndShowTrack();
                	}
            	} else {
            		customAlert("track " + (currentTrack + 1) + " not found in GPX file");
            	}
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
            	
            	showTrackMetaData(response);
            	
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
        
	var trackColor = TRACK_COLORS[globalTrackCounter % TRACK_COLORS.length];
	
    var trackPath = new google.maps.Polyline({
        path: trackPointList,
        strokeColor: trackColor,
        strokeOpacity: 0.8,
        strokeWeight: 4
    });
         
    trackPath.setMap(map);
                       
    map.fitBounds(bounds);
    
    globalTrackCounter++;    
}

function showTrackMetaData(response) {
   	var trackElem = document.createElement("div");
   	trackElem.setAttribute("class", "trackMetaInfo");
   	var trackCont = document.getElementById("gpsTrackMetaInfo");
   	trackCont.appendChild(trackElem);

	if (response.trackName) {
	   	var trackNameElem = document.createElement("span");
	   	trackNameElem.setAttribute("class", "trackName");
	   	trackNameElem.innerHTML = response.trackName;
	   	trackElem.appendChild(trackNameElem);
	}

	if (response.startTime) {
		var startTime = new Date(parseInt(response.startTime));

		var metaData = startTime.toLocaleString();

		if (response.endTime) {
			var endTime = new Date(parseInt(response.endTime));
			metaData = metaData + " - " + endTime.toLocaleString();
		}
		
	   	var trackTimeElem = document.createElement("span");
	   	trackTimeElem.setAttribute("class", "trackTime");
	   	trackTimeElem.innerHTML = metaData;
	   	trackElem.appendChild(trackTimeElem);
	}
}
