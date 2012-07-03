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
  <xsl:attribute name="src">http://www.openlayers.org/api/OpenLayers.js</xsl:attribute>
</script>

<title>
  <xsl:value-of select="/geoTag/shortPath" />
</title>

<script language="javascript">
  
    function showMap()
    {
        var latitude = 0;
        var longitude = 0;
        var zoomFactor = 1;
  
        var map = new OpenLayers.Map("mapdiv");
        map.addLayer(new OpenLayers.Layer.OSM());
 
        var pois = new OpenLayers.Layer.Text("My Points",
                                             {
                                                 location:"/webfilesys/servlet?command=osmFilesPOIList&amp;path=" + encodeURIComponent('<xsl:value-of select="/geoTag/pathForScript" />'),
                                                 projection: map.displayProjection
                                             });
        map.addLayer(pois);
 
        var lonLat = new OpenLayers.LonLat(longitude, latitude);
        lonLat.transform(new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
                         map.getProjectionObject()); // to Spherical Mercator Projection
        map.setCenter(lonLat, zoomFactor);      
    }  

</script>

</head>

<body onload="showMap()" style="margin:0px;">

  <div id="mapdiv"></div>

</body>

</html>

</xsl:template>

</xsl:stylesheet>
