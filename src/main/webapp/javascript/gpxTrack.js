var TRACK_COLORS = [
	"#ffff00", 
	"#ff4040", 
	"#00c0ff" 
];

var CANVAS_HEIGHT = 200;
var CANVAS_WIDTH = 1000;

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
                	
                	if (response.hasElevation) {
                    	drawAltDistProfile(response);
                	}
                	
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
	
	if (response.trackpoints) {
	   	var trackpointNumElem = document.createElement("span");
	   	trackpointNumElem.setAttribute("class", "trackName");
	   	trackpointNumElem.innerHTML = "(" + response.trackpoints.length + " trackpoints)";
	   	trackElem.appendChild(trackpointNumElem);
	}
}

function drawAltDistProfile(response) {
	
	var canvasCont = document.createElement("div");
	canvasCont.setAttribute("class", "gpsProfileCont");
    document.getElementsByTagName("body")[0].appendChild(canvasCont);    
	
    var maxValueCont = document.createElement("div");
    maxValueCont.setAttribute("class", "gpsChartText");
    canvasCont.appendChild(maxValueCont);
    
	var canvas = document.createElement("canvas");
    canvas.setAttribute("class", "gpsProfile");
    
    canvasCont.appendChild(canvas);
    
    canvas.style.width = CANVAS_WIDTH + "px";
    canvas.style.height = CANVAS_HEIGHT + "px";
    
    canvas.setAttribute("width", CANVAS_WIDTH);
    canvas.setAttribute("height", CANVAS_HEIGHT);
    
    var chartXOffset = 10;
    var chartYOffset = 10;

    var chartWidth = CANVAS_WIDTH - chartXOffset;
	var chartHeight = CANVAS_HEIGHT - chartYOffset;
	
    var ctx = canvas.getContext("2d");  
    ctx.fillStyle = "#000";
    ctx.strokeStyle = "#000";
    ctx.lineWidth = 1;
    ctx.beginPath();
    
    ctx.moveTo(chartXOffset - 1, 0);
    ctx.lineTo(chartXOffset - 1, chartHeight + 5);
    ctx.stroke();
    
    ctx.moveTo(chartXOffset - 5, chartHeight + 1);
    ctx.lineTo(CANVAS_WIDTH - 1, chartHeight + 1);
    ctx.stroke();
    
    var lastX = chartXOffset;
    var lastY = chartHeight;

    ctx.fillStyle = "#00c";

    ctx.beginPath();  
    ctx.moveTo(chartXOffset, CANVAS_HEIGHT);  

    var minElevation = 10000;
    var maxElevation = -10000;
    var maxTotalDist = 0;
    
    var elevationDistMap = [];
    
    var trackpoints = response.trackpoints;
    
    for (var i = 0; i < trackpoints.length; i++) {
    	if (trackpoints[i].ele) {
    		var mapEntry = [];
    		
    		var elevation = Number.parseFloat(trackpoints[i].ele);
    		var totalDist = Number.parseFloat(trackpoints[i].totalDist);
    		
    		mapEntry[0] = elevation;
    		mapEntry[1] = totalDist;
    		
    		elevationDistMap[i] = mapEntry;
    		
    		if (elevation < minElevation) {
    			minElevation = elevation;
    		}
    		if (elevation > maxElevation) {
    			maxElevation = elevation;
    		}
    		if (totalDist > maxTotalDist) {
    			maxTotalDist = totalDist;
    		}
    	}
    }

    var maxHeight = maxElevation - minElevation;
    
    for (var i = 0; i < elevationDistMap.length; i++) {
    	if (elevationDistMap[i]) {
            var height = elevationDistMap[i][0] - minElevation;
            
            var distance = elevationDistMap[i][1];
      
            var xPos = distance * chartWidth / maxTotalDist;
            var yPos = chartHeight - (height * chartHeight / maxHeight)
      
            if ((xPos != lastX) || (yPos != lastY)) {
                ctx.lineTo(chartXOffset + xPos, yPos);
                lastX = xPos;
                lastY = yPos;  
            }
    	}
    }
    
    ctx.lineTo(CANVAS_WIDTH - 1, chartHeight);  
    ctx.lineTo(chartXOffset, chartHeight);  
    ctx.fill();

    // elevation legend

	var legendCanvas = document.createElement("canvas");
	legendCanvas.setAttribute("class", "gpsProfileLegend");
    
	legendCanvas.setAttribute("width", 80);
	legendCanvas.setAttribute("height", CANVAS_HEIGHT);
	
    canvasCont.appendChild(legendCanvas);
	
    var legendCtx = legendCanvas.getContext("2d");  

    legendCtx.font = "10pt Arial";
    
    var elevationLegendStep = Math.ceil(maxHeight / 10);
    
    if (elevationLegendStep < 10) {
        elevationLegendStep = 10;
    } else {
        while (elevationLegendStep % 10 != 0) {
            elevationLegendStep ++;
        }
    }
    
    var legendElevation = Math.ceil(minElevation);
    
    while (legendElevation % 10 != 0) {
        legendElevation++;
    }
    
    ctx.strokeStyle = "#c0c0c0";
    ctx.lineWidth = 1;
    
    while (legendElevation < maxElevation) {
        var legendYPos = chartHeight - ((legendElevation - minElevation) * chartHeight / maxHeight);
        
        ctx.beginPath();
        ctx.moveTo(chartXOffset - 1, legendYPos);
        ctx.lineTo(CANVAS_WIDTH - 1, legendYPos);
        ctx.stroke();

        legendCtx.fillText(String(legendElevation) , 5, legendYPos + 5, 70);
        
        legendElevation += elevationLegendStep;
    }
    
    maxValueCont.innerHTML = "max: " + maxElevation + " m";
    
    var tableElem = document.createElement("table");
    tableElem.style.width = CANVAS_WIDTH + "px";
    canvasCont.appendChild(tableElem);
    
    var rowElem = document.createElement("tr");
    tableElem.appendChild(rowElem);
    
    var minElem = document.createElement("td");
    minElem.setAttribute("class", "gpsChartText");
    minElem.innerHTML = "min: " + minElevation + " m";
    rowElem.appendChild(minElem);

    var distanceElem = document.createElement("td");
    distanceElem.setAttribute("class", "gpsChartText");
    distanceElem.style.textAlign = "right";
    distanceElem.innerHTML = "distance: " + formatDecimalNumber(Math.ceil(maxTotalDist)) + " km";
    rowElem.appendChild(distanceElem);
}

