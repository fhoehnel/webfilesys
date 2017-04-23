<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="geoData" />

<!-- root node-->
<xsl:template match="/">

<html style="height:100%">
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/geoData/css" />.css</xsl:attribute>
  </link>
  
  <title>
    WebFileSys google map
  </title>

  <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>

  <script type="text/javascript" src="/webfilesys/javascript/geoMap.js"></script>
  <script type="text/javascript" src="/webfilesys/javascript/ajaxCommon.js"></script>
  <script type="text/javascript" src="/webfilesys/javascript/util.js"></script>
  
  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/geoData/language" /></xsl:attribute>
  </script>

  <script language="javascript">

    var infoWindowList = [];
    
    var map;
  
    function handleGoogleMapsApiReady() {
        var zoomFactor = <xsl:value-of select="/geoData/mapData/zoomLevel" />;

        var centerLatitude = <xsl:value-of select="/geoData/markers/marker[1]/latitude" />;
        var centerLongitude = <xsl:value-of select="/geoData/markers/marker[1]/longitude" />;
  
        var mapCenter = new google.maps.LatLng(centerLatitude, centerLongitude);
    
        var myOptions = {
            zoom: zoomFactor,
            center: mapCenter,
            mapTypeId: google.maps.MapTypeId.HYBRID
        }
      
        map = new google.maps.Map(document.getElementById("map"), myOptions);      
    
	    var markerPos;
	    var marker;
		var infoWindow;
		var infoText;
		
        <xsl:for-each select="/geoData/markers/marker">
		  
		  markerPos = new google.maps.LatLng(<xsl:value-of select="latitude" />, <xsl:value-of select="longitude" />);
		
          marker = new google.maps.Marker({
              position: markerPos,
              title: resourceBundle["mapMarkerTitle"]
          });

          marker.setMap(map);   

          <xsl:if test="infoText">
		    infoText = '<xsl:value-of select="infoText" />';
			
            infoWindow = new google.maps.InfoWindow({
                content: infoText,
                maxWidth: 120,
                maxHeight: 40
            });

            infoWindow.open(map, marker);
            
            infoWindowList.push(infoWindow);
		  </xsl:if>
		  
		  <xsl:if test="fileName">
		    window.google.maps.event.addListener(marker, 'click', function () {
                showImageOnMap('<xsl:value-of select="fileName"/>')
            });
		  </xsl:if>
		  
        </xsl:for-each>
        
        if (infoWindowList.length == 0) {
            document.getElementById("hideInfoButton").style.display = "none";
        }
	
    }  
    
    function loadGoogleMapsAPIScriptCode() {
        var script = document.createElement("script");
        script.type = "text/javascript";
        script.src = "http://maps.google.com/maps/api/js?sensor=false&amp;callback=handleGoogleMapsApiReady";
        document.body.appendChild(script);
    }
    
    function hideMapInfoWindows() {
        for (var i = 0; i &lt; infoWindowList.length; i++) {
            infoWindowList[i].setMap(null);
        }
        document.getElementById("hideInfoButton").style.display = "none";
        document.getElementById("showInfoButton").style.display = "inline";
    }
    
    function showMapInfoWindows() {
        for (var i = 0; i &lt; infoWindowList.length; i++) {
            infoWindowList[i].setMap(map);
        }
        document.getElementById("hideInfoButton").style.display = "inline";
        document.getElementById("showInfoButton").style.display = "none";
    }
  </script>

</head>

<body onload="loadGoogleMapsAPIScriptCode()" style="margin:0px;height:100%;">

  <div id="map" style="width:100%;height:100%;"></div>
  
  <div style="position:absolute;top:10px;right:10px;"> 

    <form>
        <input id="hideInfoButton" type="button" resource="button.hideMapInfo" onclick="hideMapInfoWindows()" 
            style="font-size:13px;font-weight:bold;color:black;"/>

        <input id="showInfoButton" type="button" resource="button.showMapInfo" onclick="showMapInfoWindows()" 
            style="font-size:13px;font-weight:bold;color:black;display:none;"/>

        <input type="button" resource="button.closeMap" onclick="setTimeout('self.close()', 100)" 
            style="font-size:13px;font-weight:bold;color:black;"/>
    </form>
      
  </div>

  <script type="text/javascript">
    setBundleResources();
  </script>

</body>

</html>

</xsl:template>

</xsl:stylesheet>
