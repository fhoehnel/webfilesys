<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="geoTag" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/metaInf/css" />.css</xsl:attribute>
</link>
  
<script type="text/javascript">
  <xsl:attribute name="src">http://maps.google.com/maps?file=api&amp;v=2&amp;key=<xsl:value-of select="/geoTag/googleMapsAPIKey" /></xsl:attribute>
</script>

<title>
  <xsl:value-of select="/geoTag/shortPath" />
</title>

<script language="javascript">
  
    function showMap()
    {
        var latitude = '<xsl:value-of select="/geoTag/latitude" />';
        var longitude = '<xsl:value-of select="/geoTag/longitude" />';
        var zoomFactor = '<xsl:value-of select="/geoTag/zoomFactor" />';
  
        var infoText = '<xsl:value-of select="/geoTag/infoText" />';
      
        if (GBrowserIsCompatible()) 
        {
            var map = new GMap2(document.getElementById("map"));
            map.addControl(new GSmallMapControl());
            map.addControl(new GMapTypeControl());
            map.setCenter(new GLatLng(latitude, longitude), parseInt(zoomFactor));
            map.setMapType(G_HYBRID_MAP);
          
            if (infoText != '')
            {
                map.openInfoWindow(map.getCenter(), document.createTextNode(infoText));
            }

            var markerPoint = new GLatLng(latitude, longitude);
            map.addOverlay(new GMarker(markerPoint));
        }      
    }  

</script>


</head>

<body onload="showMap()" style="margin:0px;">

  <xsl:attribute name="onunload">GUnload()</xsl:attribute>

  <div id="map" style="width:100%;height:100%;"></div>
</body>

</html>

</xsl:template>

</xsl:stylesheet>
