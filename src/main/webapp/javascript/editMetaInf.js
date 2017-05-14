function checkLengthAndSubmit() {  
    if (document.form1.description.value.length>1024) {  
        alert(resourceBundle["alert.descriptionTooLong"]);
    } else { 
        document.form1.submit();
    }
} 
  
function uncheckDefaultColor() {
    document.getElementById("defaultColorCheckbox").checked = false;
}
  
function resetTextColor(checkbox) {
    if (checkbox.checked) {
        console.log("resetting text color");
        document.getElementById("textColor").value = "00000";
        document.getElementById("textColor").style.color = "#000000";
        document.getElementById("textColor").style.backgroundColor = "#ffffff";
    }
}
  
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
  
var posMarker;
  
function selectLocation() {
    var markerPos = posMarker.getPosition();
    
    document.form1.latitude.value = markerPos.lat(); 
    document.form1.longitude.value = markerPos.lng();
        
    hideMap();
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

    var zoomFactor = parseInt(document.form1.zoomFactor[document.form1.zoomFactor.selectedIndex].value);
      
    var infoText;

    if (selectLocation) {
        infoText = resourceBundle["label.hintGoogleMapSelect"];
    } else {
        infoText = document.form1.infoText.value;
    }        
      
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
        
    if ((selectLocation && coordinatesNotYetSelected) ||
        (!selectLocation && (infoText != ''))) {
        var infowindow = new google.maps.InfoWindow({
            content: '<div style="width:160px;height:40px;overflow-x:auto;overflow-y:auto">' + infoText + '</div>'
        });

        infowindow.open(map, posMarker);
    }    
        
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
