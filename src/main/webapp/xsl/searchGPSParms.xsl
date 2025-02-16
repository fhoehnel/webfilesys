<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="searchParms" />

<xsl:template match="/searchParms">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/searchParms/css" />.css</xsl:attribute>
</link>

<link rel="stylesheet" href="javascript/jquery-ui-1.12.1.custom/jquery-ui.min.css" />
<link rel="stylesheet" href="javascript/jquery-ui-1.12.1.custom/jquery-ui.structure.min.css" />
<link rel="stylesheet" href="javascript/jquery-ui-1.12.1.custom/jquery-ui.theme.min.css" />

<title resource="label.searchTitle"></title>

<script type="text/javascript" src="javascript/jquery/jquery.min.js"></script>
<script type="text/javascript" src="javascript/jquery-ui-1.12.1.custom/jquery-ui.min.js"></script>

<script src="javascript/browserCheck.js" type="text/javascript"></script>
<script src="javascript/ajaxCommon.js" type="text/javascript"></script>
<script src="javascript/util.js" type="text/javascript"></script>
<script src="javascript/searchGPS.js" type="text/javascript"></script>
<script src="javascript/calendar/CalendarPopup.js" type="text/javascript"></script>
<script src="javascript/calendar/AnchorPosition.js" type="text/javascript"></script>
<script src="javascript/calendar/date.js" type="text/javascript"></script>
<script src="javascript/calendar/PopupWindow.js" type="text/javascript"></script>

<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
<script type="text/javascript">
  <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/searchParms/language" /></xsl:attribute>
</script>

<style id="calendarStyle">
</style>

</head>

<body class="search">
  <xsl:attribute name="onload">setBundleResources();loadGoogleMapsAPIScriptCode('<xsl:value-of select="/searchParms/googleMapsAPIKey" />')</xsl:attribute>

  <div class="headline" resource="label.searchGPSHead" />
  
  <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet">
  
    <input type="hidden" name="command" value="searchGPS" />
  
    <input type="hidden" name="actpath">
      <xsl:attribute name="value"><xsl:value-of select="currentPath" /></xsl:attribute>
    </input> 
 
    <table class="dataForm" width="100%">
      <tr>
        <td colspan="2" class="formParm1" resource="label.searchPath" />
      </tr>
      <tr>
        <td colspan="2" class="formParm2">
          <xsl:value-of select="relativePath" />
        </td>
      </tr>
      
      <tr>
          <td colspan="2" class="formParm2" width="80%">
            <ul style="list-style:none;margin:0;padding:0;">
              <li style="padding-bottom:5px;">
                <input name="latitude" style="width:80px;" />
                &#160;
                <span resource="label.latitude"></span>
              </li>

              <li style="padding-bottom:5px;">
                <input name="longitude" style="width:80px;"/>
                &#160;
                <span resource="label.longitude"></span>
              </li>
      
              <li style="padding-bottom:5px;">
                <input type="button" resource="button.selectFromMap">
                  <xsl:attribute name="onclick">showMap(true)</xsl:attribute>
                </input> 
              </li>
            </ul>
          </td>
      </tr>

      <tr>
        <td class="formParm1" resource="label.searchGPSDistance" />
        <td class="formParm2">
          <input type="text" id="distance" name="distance" style="width:80px" />
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm1">
          <input type="checkbox" name="includeSubdirs" checked="true" class="cb5" />
          &#160;
          <span resource="label.includeSubdirs" />
        </td>
      </tr>

      <tr>
        <td class="formParm1" resource="label.dateRangeFrom" />
        <td class="formParm2">
          <input type="text" id ="dateRangeFrom" name="dateRangeFrom" style="width:80px" readonly="readonly" />
          &#160;
          <a href="javascript:openFromDateSelection()">
            <img src="images/calendar.gif" border="0" />
          </a>
        </td>
      </tr>

      <tr>
        <td class="formParm1" resource="label.dateRangeUntil" />
        <td class="formParm2">
          <input type="text" id ="dateRangeUntil" name="dateRangeUntil" style="width:80px" readonly="readonly" />
          &#160;
          <a href="javascript:openUntilDateSelection()">
            <img src="images/calendar.gif" border="0" />
          </a>
        </td>
      </tr>

      <tr style="margin-top:10px">
        <td class="formButton">
          <input id="searchButton" type="button" onclick="submitIfValid()" resource="button.startsearch" />
        </td>
        
        <td class="formButton" align="right">
          <input id="cancelButton" type="button" onclick="self.close()" resource="button.cancel" />
        </td>

      </tr>
    </table>
  </form>

  <div id="calDiv" style="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></div>

</body>

  <div id="mapFrame" style="width:100%;height:100%;position:absolute;top:0px;left:0px;visibility:hidden;background-color:#d0d0d0;">
    <div id="map" style="width:100%;height:100%;position:absolute;top:0px;left:0px;"></div>
    
    <div style="position:absolute;bottom:15px;left:10px;"> 
      <form>
        <input id="closeButton" type="button" resource="button.closeMap" onclick="hideMap()" 
            style="font-size:13px;font-weight:bold;color:black;"/>

        <input id="selectButton" type="button" resource="button.save" onclick="javascript:selectLocation()" 
            style="visibility:hidden;font-size:13px;font-weight:bold;color:black;"/>
      </form>
    </div>
  </div>

</html>

</xsl:template>

</xsl:stylesheet>
