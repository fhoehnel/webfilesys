var TRACK_COLORS = [
	"#ffff00", 
	"#ff4040", 
	"#00c0ff" 
];

var SLOW_MOTION_TRACK_COLORS = [
   	"#b000b0", 
   	"#ff4040", 
   	"#ffff00", 
   	"#00c0ff",
   	"#0080a0" 
];

var CANVAS_HEIGHT = 200;
var CANVAS_WIDTH = 1000;

var SLOWMOTION_DURATION = 10000;

var globalTrackCounter = 0;

var trackPointList = new Array();

var bounds;

var map;

var globalTrackMap = new Object();

var slowMotionTracks = new Array();

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
	
    var url = "/webfilesys/servlet?command=gpxTrack&filePath=" + encodeURIComponent(filePath) + "&trackNumber=" + currentTrack;
    
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
                	
                	if (response.hasSpeed) {
                		if (!response.invalidTime) {
                        	drawSpeedProfile(response);
                		} else {
                			customAlert("GPX file contains invalid time data - omitting speed profile")
                		}
                	}
                	
                	currentTrack++;
                	
                	if (currentTrack < trackNumber) {
                		loadAndShowTrack();
                	} else {
                		loadAndShowWayPoints();
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
	
    var url = "/webfilesys/servlet?command=gpxTrack&filePath=" + encodeURIComponent(filePath) + "&trackNumber=0";
    
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

function loadAndShowWayPoints() {
	
    var url = "/webfilesys/servlet?command=gpxWayPoints&filePath=" + encodeURIComponent(filePath);
    
    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
            	var response = JSON.parse(req.responseText);
            	if (response.waypoints && (response.waypoints.length > 0)) {
                	showWayPointsOnMap(response.waypoints);
            	}
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });      
}

function showWayPointsOnMap(wayPoints) {
    for (let i = 0; i < wayPoints.length; i++) {
    	
    	new google.maps.Marker({
    	    position: new google.maps.LatLng(wayPoints[i].lat, wayPoints[i].lon),
    	    label: {
    	        color: "#c0f0f0", // <= HERE
    	        fontSize: '13px',
    	        fontWeight: '900',
    	        text: wayPoints[i].name
    	    },
    	    map: map,
    	});    
    }
}

function showTrackOnMap(trackpoints, trackCounter) {

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
    
    globalTrackMap[globalTrackCounter - 1] = trackPath;
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

		var metaData = startTime.toUTCString();

		if (response.endTime) {
			var endTime = new Date(parseInt(response.endTime));
			metaData = metaData + " - " + endTime.toUTCString();
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

	    if (typeof(gpxFiles) == "undefined") {
		   	var slowMotionLink = document.createElement("a");
		   	slowMotionLink.id = "slowMotionLink-" + (globalTrackCounter - 1);
		   	slowMotionLink.setAttribute("href", "javascript:showTrackInSlowMotion(" + (globalTrackCounter - 1) + ")");
		   	slowMotionLink.setAttribute("class", "gpxSlowMotionLink");
		   	slowMotionLink.setAttribute("title", resourceBundle["slowMotionTitle"]);
		   	slowMotionLink.innerHTML = resourceBundle["slowMotionLink"];
		   	trackElem.appendChild(slowMotionLink);
	    }
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
    		
    		var elevation = parseFloat(trackpoints[i].ele);
    		var totalDist = parseFloat(trackpoints[i].totalDist);
    		
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

    const dist = formatDecimalNumber(Math.ceil(maxTotalDist));
    
    var distanceElem = document.createElement("td");
    distanceElem.setAttribute("class", "gpsChartText");
    distanceElem.style.textAlign = "right";
    distanceElem.innerHTML = "distance: " + dist + " " + (dist.indexOf(".") >= 0 ? "km" : "m");
    rowElem.appendChild(distanceElem);
}

function drawSpeedProfile(response) {
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

    ctx.fillStyle = "#c00";

    ctx.beginPath();  
    ctx.moveTo(chartXOffset, CANVAS_HEIGHT);  
    
    var minSpeed = 100000;
    var maxSpeed = (-1);
    var minTime = new Date().getTime();
    var maxTime = 0;
    
    var speedTimeMap = [];
    
    var trackpoints = response.trackpoints;
    
    for (var i = 0; i < trackpoints.length; i++) {
    	if (trackpoints[i].speed && (trackpoints[i].time)) {
    		var mapEntry = [];
    		
    		var speed = parseFloat(trackpoints[i].speed);
    		var time = parseInt(trackpoints[i].time);
    		
    		mapEntry[0] = speed;
    		mapEntry[1] = time;
    		
    		speedTimeMap[i] = mapEntry;
    		
    		if (time < minTime) {
    			minTime = time;
    		}
    		if (time > maxTime) {
    			maxTime = time;
    		}
    		if (speed < minSpeed) {
    			minSpeed = speed;
    		}
    		if (speed > maxSpeed) {
    			maxSpeed = speed;
    		}
    	}
    }
    
    // max x step which is not counted as break
    var maxStepX = chartWidth / trackpoints.length;
    if (maxStepX < 3) {
        maxStepX = 3;
    }
    
    var duration = maxTime - minTime;
    
    var maxHeight = maxSpeed - minSpeed;
    
    for (var i = 0; i < speedTimeMap.length; i++) {
    	if (speedTimeMap[i]) {
            var height = speedTimeMap[i][0] - minSpeed;
            
            var timestamp = speedTimeMap[i][1];
            
            var xPos = ((timestamp - minTime) * chartWidth) / duration;
            var yPos = chartHeight - (height / maxHeight * chartHeight);
      
            var stepX = xPos - lastX;
            
            if (stepX > maxStepX) {
                ctx.lineTo(chartXOffset + lastX, chartHeight);
                ctx.lineTo(chartXOffset + xPos, chartHeight);
            } else {
                ctx.lineTo(chartXOffset + xPos, yPos);
            }
            
            lastX = xPos;
            lastY = yPos;  
    	}
    }
    
    ctx.lineTo(CANVAS_WIDTH - 1, chartHeight);  
    ctx.lineTo(chartXOffset, chartHeight);  
    ctx.fill();
    
    // speed legend

	var legendCanvas = document.createElement("canvas");
	legendCanvas.setAttribute("class", "gpsProfileLegend");
    
	legendCanvas.setAttribute("width", 80);
	legendCanvas.setAttribute("height", CANVAS_HEIGHT);
	
    canvasCont.appendChild(legendCanvas);
	
    var legendCtx = legendCanvas.getContext("2d");  
    ctx.strokeStyle = "#c0c0c0";
    ctx.lineWidth = 1;
    legendCtx.font = "10pt Arial";

    var speedLegendStep = Math.ceil(maxHeight * 3.6 / 10);
    
    if (speedLegendStep < 2) {
        speedLegendStep = 2;
    } else {
        while (speedLegendStep % 2 != 0) {
            speedLegendStep++;
        }
    }
    
    var minSpeedKmh = minSpeed * 3.6;
    var maxSpeedKmh = maxSpeed * 3.6;
    
    var legendSpeed = Math.ceil(minSpeedKmh);
    
    while (legendSpeed < maxSpeedKmh) {
        var legendYPos = chartHeight - (((legendSpeed - minSpeedKmh) / 3.6) * chartHeight / maxHeight);
       
        ctx.beginPath();
        ctx.moveTo(chartXOffset - 1, legendYPos);
        ctx.lineTo(CANVAS_WIDTH - 1, legendYPos);
        ctx.stroke();

        legendCtx.fillText(String(legendSpeed) , 5, legendYPos + 5, 70)
        
        legendSpeed += speedLegendStep;
    }
    
    maxValueCont.innerHTML = "max: " + maxSpeedKmh.toFixed(2) + " km/h";
    
    var tableElem = document.createElement("table");
    tableElem.style.width = CANVAS_WIDTH + "px";
    canvasCont.appendChild(tableElem);
    
    var rowElem = document.createElement("tr");
    tableElem.appendChild(rowElem);
    
    var minElem = document.createElement("td");
    minElem.setAttribute("class", "gpsChartText");
    minElem.innerHTML = "min: " + minSpeedKmh.toFixed(2) + " km/h";
    rowElem.appendChild(minElem);

    var emptyElem = document.createElement("td");
    rowElem.appendChild(emptyElem);

    rowElem = document.createElement("tr");
    tableElem.appendChild(rowElem);
    
    var startTimeElem = document.createElement("td");
    startTimeElem.setAttribute("class", "gpsChartText");
    startTimeElem.innerHTML = new Date(minTime).toUTCString();
    rowElem.appendChild(startTimeElem);

    var endTimeElem = document.createElement("td");
    endTimeElem.setAttribute("class", "gpsChartText");
    endTimeElem.style.textAlign = "right";
    endTimeElem.innerHTML = new Date(maxTime).toUTCString();
    rowElem.appendChild(endTimeElem);
}
 
function showTrackInSlowMotion(trackId) {
	document.getElementById("slowMotionLink-" + trackId).style.display = "none";
	
	globalTrackMap[trackId].setMap(null);	
	
	for (var t = slowMotionTracks.length - 1; t >= 0; t--) {
		slowMotionTracks.pop().setMap(null);
	}
	
    var url = "/webfilesys/servlet?command=gpxTrack&filePath=" + encodeURIComponent(filePath) + "&trackNumber=" + trackId;
    
    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
            	var response = JSON.parse(req.responseText);
            
            	if (response.trackpoints && (response.trackpoints.length > 0)) {
            		var pointsPerStep = response.trackpoints.length / 1000;
            		if (pointsPerStep < 2) {
            			pointsPerStep = 2;
            		}
            		var delay = SLOWMOTION_DURATION / (response.trackpoints.length / pointsPerStep);

            		var invalidTime = false;
            		
            		var trackDuration = 0;
            		if (response.startTime && response.endTime) {
                		trackDuration = response.endTime - response.startTime;
            		} else {
            			invalidTime = true;
            		}
            		
            		if (response.invalidTime) {
            			invalidTime = true;
            		}
            		
            		showTrackOnMapSlow(trackId, response.trackpoints, 0, TRACK_COLORS[(globalTrackCounter - 1) % TRACK_COLORS.length], 
            				           delay, pointsPerStep, trackDuration, invalidTime);
            	} else {
            		customAlert("track " + (currentTrack + 1) + " not found in GPX file");
            	}
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });      
}

function showTrackOnMapSlow(trackId, trackpoints, index, trackColor, delay, pointsPerStep, trackDuration, invalidTime) {
	
	if (index >= trackpoints.length - 1) {
		document.getElementById("slowMotionLink-" + trackId).style.display = "inline";
		return;
	}

	var idx = index;
	
	var trackPointList = new Array();

	var sectionStartTime = trackpoints[idx].time;
	var sectionEndTime = trackpoints[idx].time;
	
	var sectionStartDist = trackpoints[idx].totalDist;
	var sectionEndDist = sectionStartDist;
	
	for (var p = 0; (p < pointsPerStep) && (idx < trackpoints.length); p++, idx++) {
		var latLon = new google.maps.LatLng(trackpoints[idx].lat, trackpoints[idx].lon);
	    trackPointList.push(latLon);
	    sectionEndTime = trackpoints[idx].time;
	    sectionEndDist = trackpoints[idx].totalDist;
	}

	var speedTrackColor = trackColor;
	
    var speedAdjustedDelay = delay;

    if (!invalidTime) {
    	var sectionDuration = sectionEndTime - sectionStartTime;
    	
		var trackLength = trackpoints[trackpoints.length -1].totalDist;
		
    	var sectionDist = sectionEndDist - sectionStartDist;
    	
        var distPercentage = sectionDist / trackLength;
        
        var durationPercentage = sectionDuration / trackDuration;
        
        speedAdjustedDelay = delay * durationPercentage / distPercentage;
        
        if (speedAdjustedDelay > delay * 10) {
        	speedAdjustedDelay = delay * 10;
        } else if (speedAdjustedDelay < delay / 10) {
        	speedAdjustedDelay = delay / 10;
        }
        
        speedTrackColor = calculateSpeedAdjustedTrackColor(delay, speedAdjustedDelay);
    }
	
    var trackPath = new google.maps.Polyline({
        path: trackPointList,
        strokeColor: speedTrackColor,
        strokeOpacity: 0.8,
        strokeWeight: 4
    });
         
    trackPath.setMap(map);
    
    slowMotionTracks.push(trackPath);

    setTimeout(function() {
    	showTrackOnMapSlow(trackId, trackpoints, idx - 1, trackColor, delay, pointsPerStep, trackDuration, invalidTime);
    }, speedAdjustedDelay);
	
}

function calculateSpeedAdjustedTrackColor(delay, speedAdjustedDelay) {
    if (speedAdjustedDelay < 0.25 * delay) {
    	return SLOW_MOTION_TRACK_COLORS[0];
    }
    if (speedAdjustedDelay < 0.5 * delay) {
    	return SLOW_MOTION_TRACK_COLORS[1];
    }
    if (speedAdjustedDelay > 4 * delay) {
      	return SLOW_MOTION_TRACK_COLORS[4];
    }
    if (speedAdjustedDelay > 2 * delay) {
    	return SLOW_MOTION_TRACK_COLORS[3];
    }
    return SLOW_MOTION_TRACK_COLORS[2];
}
