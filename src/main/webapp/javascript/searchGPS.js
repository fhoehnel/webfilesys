var map;

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

function hideOSMap() {
    document.getElementById("mapFrame").style.visibility = 'hidden';
    document.getElementById("mapFrame").style.display = 'none';
}

function onMapClick(e) {
    document.form1.latitude.value = e.latlng.lat.toFixed(8);
    document.form1.longitude.value = e.latlng.lng.toFixed(8);
    hideOSMap();
}

function showOSMap() {
    if (!map) {
        const osmUrl='https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
        const osm = new L.TileLayer(osmUrl, {minZoom:2, maxZoom:19});

        const googleStreets = new L.tileLayer('https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}',{minZoom:1, maxZoom:19, subdomains:['mt0','mt1','mt2','mt3']});

        const googleSat = new L.tileLayer('https://{s}.google.com/vt/lyrs=s&x={x}&y={y}&z={z}',{minZoom:1, maxZoom: 21,subdomains:['mt0','mt1','mt2','mt3']});

        map = new L.Map('mapDiv', { doubleClickZoom:false, zoomControl:false, maxBounds:([[90,-270],[-90,270]]) });

        L.control.layers({"OSM (Mapnik)": osm, "Google Street": googleStreets, "Google Earth": googleSat}).addTo(map);

        map.addLayer(osm);
        map.fitBounds([[0,-180],[0,180]]);

        map.on('click', onMapClick);
    }

    document.getElementById("mapFrame").style.display = 'block';
    document.getElementById("mapFrame").style.visibility = 'visible';
}
