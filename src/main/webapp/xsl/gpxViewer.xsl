<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="gpx" />

<xsl:template match="/">

<html>
  <head>

    <meta http-equiv="expires" content="0" />

    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

    <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/ajax.js" type="text/javascript"></script>

    <script src="/webfilesys/javascript/gpxTrack.js" type="text/javascript"></script>

    <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/gpx/language" /></xsl:attribute>
    </script>

    <script type="text/javascript">
      var trackNumber = <xsl:value-of select="count(/gpx/track)" />;
      
      var currentTrack = 0;
      
      var filePath = '<xsl:value-of select="/gpx/filePath" />';
      
    </script>
    
  </head>

  <body>
    <xsl:if test="/gpx/track">
      <xsl:attribute name="onload">loadGoogleMapsAPIScriptCode('<xsl:value-of select="/gpx/googleMapsAPIKey" />')</xsl:attribute>
    </xsl:if>

    <xsl:if test="not(/gpx/track)">
      <script type="text/javascript">
          customAlert("GPX file does not contain any track data");
      </script>
    </xsl:if>

    <xsl:if test="/gpx/track">
      <div id="mapCont" class="gpsTrackMapCont"></div>

      <div id="gpsTrackMetaInfo"></div>
      
    </xsl:if>

  </body>
  
  <script type="text/javascript">
    setBundleResources();
  </script>

</html>

</xsl:template>

</xsl:stylesheet>
