<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:template match="/">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/osmap.css" />

  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/javascript/gpxOSM.js</xsl:attribute>
  </script>

  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/javascript/openStreetMaps/OpenLayers.js</xsl:attribute>
  </script>

  <title>
    <xsl:value-of select="/geoTag/shortPath" />
  </title>

</head>

<body style="margin:0px;" class="pictureLocations">
  <xsl:attribute name="onload">showLocationsOnOSMMap('<xsl:value-of select="/geoTag/pathForScript" />')</xsl:attribute>

  <div id="mapdiv"></div>

</body>

</html>

</xsl:template>

</xsl:stylesheet>
