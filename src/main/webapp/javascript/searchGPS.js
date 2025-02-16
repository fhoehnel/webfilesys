function loadGoogleMapsAPIScriptCode(googleMapsApiKey) {
    var script = document.createElement("script");
    script.type = "text/javascript";
    
    if (window.location.href.indexOf("https") == 0) {
        script.src = "https://maps.google.com/maps/api/js?sensor=false&callback=handleGoogleMapsApiReady&key=" + googleMapsApiKey;
    } else {
        script.src = "http://maps.google.com/maps/api/js?sensor=false&callback=handleGoogleMapsApiReady&key=" + googleMapsApiKey;
    }
    
    document.body.appendChild(script);
}
  
function handleGoogleMapsApiReady() {
    // console.log("Google Maps API loaded");
}

function selectLocation() {
    var markerPos = posMarker.getPosition();
    
    document.form1.latitude.value = markerPos.lat(); 
    document.form1.longitude.value = markerPos.lng();
        
    hideMap();
}

var selectedFromDate = null;
var selectedUntilDate = null;

$(document).ready(function() {
	var dayNamesShort = [
        resourceBundle["calendar.sun"], 
        resourceBundle["calendar.mon"], 
        resourceBundle["calendar.tue"], 
        resourceBundle["calendar.wed"], 
        resourceBundle["calendar.thu"], 
        resourceBundle["calendar.fri"], 
        resourceBundle["calendar.sat"]
    ]; 

    $("#dateRangeFrom").datepicker({
        showButtonPanel: true,
        showOtherMonths: true,
        selectOtherMonths: true,
        changeMonth: true,
        changeYear: true,
        dateFormat: resourceBundle["datePickerFormat"],
        currentText: resourceBundle["calendar.today"],
        closeText: resourceBundle["button.close"],
        dayNamesMin: dayNamesShort,
        onSelect: function(dateText, inst) { 
                      selectedFromDate = $(this).datepicker('getDate'); 
                  }
    });
    
    $("#dateRangeUntil").datepicker({
        showButtonPanel: true,
        showOtherMonths: true,
        selectOtherMonths: true,
        changeMonth: true,
        changeYear: true,
        dateFormat: resourceBundle["datePickerFormat"],
        currentText: resourceBundle["calendar.today"],
        closeText: resourceBundle["button.close"],
        dayNamesMin: dayNamesShort,
        onSelect: function(dateText, inst) { 
                      selectedUntilDate = $(this).datepicker('getDate'); 
                  }
    });
});

function openFromDateSelection() {
    $("#dateRangeFrom").trigger("focus");
}

function openUntilDateSelection() {
    $("#dateRangeUntil").trigger("focus");
}

function submitIfValid() {

    if ((selectedFromDate != null) && (selectedUntilDate != null)) {
        if (selectedFromDate.getTime() > selectedUntilDate.getTime()) {
            customAlert(resourceBundle["label.searchDateConflict"]);
            return;
        }
    }
	const distance = document.getElementById("distance").value;
	if (distance === "") {
		customAlert(resourceBundle["label.searchGPSDistanceMandatory"]);
		return;
	}
    showHourGlass();
    document.getElementById("searchButton").disabled = true;
    document.getElementById("cancelButton").disabled = true;
    document.form1.submit();
}

function showMap(selectLocation) {
    document.getElementById("mapFrame").style.display = 'block';

    var latitude = document.form1.latitude.value;

    var coordinatesNotYetSelected = false;

    if (latitude == '') {
        coordinatesNotYetSelected = true;
            
        if (selectLocation) {
            latitude = '51.1';
        } else {
            alert(resourceBundle["alert.missingLatitude"]);
            return;
        }
    }
  
    var longitude = document.form1.longitude.value;

    if (longitude == '') {
        coordinatesNotYetSelected = true;

        if (selectLocation) {
            longitude = '13.76';
        } else {
            alert(resourceBundle["alert.missingLongitude"]);
            return;
        }
    }

    var zoomFactor = 11;

    var mapCenter = new google.maps.LatLng(latitude, longitude);
    
    var myOptions = {
        zoom: zoomFactor,
        center: mapCenter,
        mapTypeId: google.maps.MapTypeId.HYBRID
    }
      
    var map = new google.maps.Map(document.getElementById("map"), myOptions);      
          
    if (selectLocation) {
        document.getElementById("selectButton").style.visibility = 'visible';
    }

    var markerPos = new google.maps.LatLng(latitude, longitude);

    posMarker = new google.maps.Marker({
        position: markerPos,
    });

    posMarker.setMap(map);
        
    google.maps.event.addListener(map, 'click', function(event) {
        var clickedPos = event.latLng;
        posMarker.setPosition(clickedPos);
        // map.setCenter(clickedPos);
    });        

    document.getElementById("mapFrame").style.visibility = 'visible';
}  

function hideMap() {
    document.getElementById("selectButton").style.visibility = 'hidden';

    document.getElementById("mapFrame").style.visibility = 'hidden';
    document.getElementById("mapFrame").style.display = 'none';
}

function appendSearchResult(filePath, viewLink, iconImg, distance) {
    const searchResult = document.createElement("li");

    const searchResultLink = document.createElement("a");
    searchResultLink.setAttribute("href", viewLink);
    searchResultLink.setAttribute("class", "fn");
    searchResultLink.setAttribute("target", "_blank");
    searchResult.appendChild(searchResultLink);

    const searchResultImg = document.createElement("img");
    searchResultImg.setAttribute("src", "icons/" + iconImg);
    searchResultLink.appendChild(searchResultImg);

    const filePathText = document.createTextNode(filePath);
    searchResultLink.appendChild(filePathText);

    const distanceCont = document.createElement("span");
    distanceCont.setAttribute("class", "searchMatchInContext");
    distanceCont.innerHTML = distance + " km";
    searchResult.appendChild(distanceCont);

    document.getElementById("searchResultList").appendChild(searchResult);
}
