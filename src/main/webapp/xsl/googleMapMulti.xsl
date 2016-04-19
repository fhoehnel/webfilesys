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

  <script type="text/javascript" src="/webfilesys/javascript/geoMap.js"></script>

  <script language="javascript">
  
    function handleGoogleMapsApiReady()
    {
        // var latitude = '<xsl:value-of select="/geoTag/latitude" />';
        // var longitude = '<xsl:value-of select="/geoTag/longitude" />';
        var zoomFactor = <xsl:value-of select="/geoData/mapData/zoomLevel" />;

        // var infoText = '<xsl:value-of select="/geoTag/infoText" />';
  
        var centerLatitude = <xsl:value-of select="/geoData/markers/marker[1]/latitude" />;
        var centerLongitude = <xsl:value-of select="/geoData/markers/marker[1]/longitude" />;
  
        var mapCenter = new google.maps.LatLng(centerLatitude, centerLongitude);
    
        var myOptions = {
            zoom: zoomFactor,
            center: mapCenter,
            mapTypeId: google.maps.MapTypeId.HYBRID
        }
      
        var map = new google.maps.Map(document.getElementById("map"), myOptions);      
    
	    var markerPos;
	    var marker;
		var infoWindow;
		var infoText;
		
        <xsl:for-each select="/geoData/markers/marker">
		  
		  markerPos = new google.maps.LatLng(<xsl:value-of select="latitude" />, <xsl:value-of select="longitude" />);
		
          marker = new google.maps.Marker({
              position: markerPos
          });

          marker.setMap(map);   

          <xsl:if test="infoText">
		    infoText = '<xsl:value-of select="infoText" />';
			
            infoWindow = new google.maps.InfoWindow({
                // content: '<div style="width:120px;height:40px;overflow-x:auto;overflow-y:auto">' + infoText + '</div>'
                content: infoText,
                maxWidth: 120,
                maxHeight: 40
            });

            infoWindow.open(map, marker);
		  </xsl:if>
		  
        </xsl:for-each>
	
    }  
    
    function loadGoogleMapsAPIScriptCode() {
        var script = document.createElement("script");
        script.type = "text/javascript";
        script.src = "http://maps.google.com/maps/api/js?sensor=false&amp;callback=handleGoogleMapsApiReady";
        document.body.appendChild(script);
    }
    
  </script>

</head>

<body onload="loadGoogleMapsAPIScriptCode()" style="margin:0px;height:100%;">

  <div id="map" style="width:100%;height:100%;"></div>

</body>

</html>

</xsl:template>

</xsl:stylesheet>
