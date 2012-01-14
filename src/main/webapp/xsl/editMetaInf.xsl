<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="metaInf" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/metaInf/css" />.css</xsl:attribute>
</link>

<xsl:if test="/metaInf/geoTag/googleMapsAPIKey">
  
  <script type="text/javascript">
    <xsl:attribute name="src">http://maps.google.com/maps?file=api&amp;v=2&amp;key=<xsl:value-of select="/metaInf/geoTag/googleMapsAPIKey" /></xsl:attribute>
  </script>
  
</xsl:if>

<script type="text/javascript" src="/webfilesys/javascript/jscolor/jscolor.js"></script>

<title>
  <xsl:value-of select="/metaInf/resources/msg[@key='label.editMetaInfo']/@value" />
</title>

<script language="javascript">
  var returnWin = '';

  function checkLengthAndSubmit()
  {  
      if (document.form1.description.value.length>1024)
      {  
          alert('<xsl:value-of select="/metaInf/resources/msg[@key='alert.descriptionTooLong']/@value" />');
      }
      else
      { 
          document.form1.submit();
      }
  } 
  
  function uncheckDefaultColor() 
  {
      document.getElementById("defaultColorCheckbox").checked = false;
  }
  
  <xsl:if test="/metaInf/geoTag">
  
    var posMarker;
  
    function setMarker(overlay, point)
    {
        posMarker.setPoint(point);
    }
  
    function selectLocation()
    {
        document.form1.latitude.value = posMarker.getPoint().lat(); 
        document.form1.longitude.value = posMarker.getPoint().lng();
        
        hideMap();
    }
  
    function showMap(selectLocation)
    {
        var latitude = document.form1.latitude.value;

        if (latitude == '')
        {
            if (selectLocation)
            {
                latitude = '51.1';
            }
            else
            {
                alert('<xsl:value-of select="/metaInf/resources/msg[@key='alert.missingLatitude']/@value" />');
                return;
            }
        }
  
        var longitude = document.form1.longitude.value;

        if (longitude == '')
        {
            if (selectLocation)
            {
                longitude = '13.76';
            }
            else
            {
                alert('<xsl:value-of select="/metaInf/resources/msg[@key='alert.missingLongitude']/@value" />');
                return;
            }
        }

        var zoomFactor = document.form1.zoomFactor[document.form1.zoomFactor.selectedIndex].value;
      
        var infoText;

        if (selectLocation)
        {
            infoText = '<xsl:value-of select="/metaInf/resources/msg[@key='label.hintGoogleMapSelect']/@value" />';
        }   
        else
        {
            infoText = document.form1.infoText.value;
        }        
      
        if (GBrowserIsCompatible()) 
        {
            var map = new GMap2(document.getElementById("map"));
            map.addControl(new GSmallMapControl());
            map.addControl(new GMapTypeControl());
            map.setCenter(new GLatLng(latitude, longitude), parseInt(zoomFactor));
            map.setMapType(G_HYBRID_MAP);
          
            if (selectLocation)
            {
                document.getElementById("selectButton").style.visibility = 'visible';
            }

            var markerPoint = new GLatLng(latitude, longitude);
            
            if (selectLocation)
            {
                posMarker = new GMarker(markerPoint, {draggable: true});
            }
            else
            {
                posMarker = new GMarker(markerPoint);
            }
            
            map.addOverlay(posMarker);
            
            if (selectLocation)
            {
                GEvent.addListener(map, "click", setMarker);            
            }
        }      

        document.getElementById("mapFrame").style.visibility = 'visible';
    }  

    function hideMap()
    {
        document.getElementById("selectButton").style.visibility = 'hidden';

        document.getElementById("mapFrame").style.visibility = 'hidden';
    }
    
  </xsl:if>
</script>


</head>

<body>

  <xsl:if test="/metaInf/geoTag/googleMapsAPIKey">
    <xsl:attribute name="onunload">GUnload()</xsl:attribute>
  </xsl:if>

  <div class="headline">
    <xsl:value-of select="/metaInf/shortPath" />
  </div>  

  <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet">
  
    <input type="hidden" name="command" value="editMetaInf" />
    
    <input type="hidden" name="path">
      <xsl:attribute name="value"><xsl:value-of select="/metaInf/path" /></xsl:attribute>
    </input> 

    <xsl:if test="/metaInf/mobile">
      <input type="hidden" name="mobile" value="true" />
    </xsl:if>
    
    <table class="dataForm" width="100%">
    
      <xsl:if test="/metaInf/error">
        <tr>
          <td colspan="2" class="formParm2">
            <span style="color:red">
              <xsl:value-of select="/metaInf/error" />
            </span>
          </td>
        </tr>
      </xsl:if>
    
      <tr>
        <td class="formParm1" nowrap="nowrap">
          <xsl:value-of select="/metaInf/resources/msg[@key='label.description']/@value" />:
          
          <xsl:if test="/metaInf/thumbnail">
            <div style="padding:0;padding-top:10px;">
              <img class="thumb" border="0">
                <xsl:attribute name="src"><xsl:value-of select="/metaInf/thumbnail/imgPath" /></xsl:attribute>
                <xsl:attribute name="width"><xsl:value-of select="/metaInf/thumbnail/thumbnailWidth" /></xsl:attribute>
                <xsl:attribute name="height"><xsl:value-of select="/metaInf/thumbnail/thumbnailHeight" /></xsl:attribute>
              </img>
            </div>
          </xsl:if>
          
        </td>
        <td class="formParm2" width="80%">
          <textarea name="description" style="width:100%;height:140px;" wrap="virtual"><xsl:value-of select="/metaInf/description" /></textarea>
        </td>
      </tr>

      <tr>
        <td class="formParm1" nowrap="nowrap">
          <xsl:value-of select="/metaInf/resources/msg[@key='label.geoTag']/@value" />:
        </td>
        <td class="formParm2" width="80%">
          <ul style="list-style:none;margin:0;padding:0;">
            <li style="padding-bottom:5px;">
              <input name="latitude" style="width:60px;">
                <xsl:if test="/metaInf/geoTag/latitude">
                  <xsl:attribute name="value"><xsl:value-of select="/metaInf/geoTag/latitude" /></xsl:attribute>
                </xsl:if>
              </input>
              &#160;
              <xsl:value-of select="/metaInf/resources/msg[@key='label.latitude']/@value" />
            </li>

            <li style="padding-bottom:5px;">
              <input name="longitude" style="width:60px;">
                <xsl:if test="/metaInf/geoTag/longitude">
                  <xsl:attribute name="value"><xsl:value-of select="/metaInf/geoTag/longitude" /></xsl:attribute>
                </xsl:if>
              </input>
              &#160;
              <xsl:value-of select="/metaInf/resources/msg[@key='label.longitude']/@value" />
            </li>
              
            <xsl:if test="/metaInf/geoTag/googleMapsAPIKey">
      
              <li style="padding-bottom:5px;">
                <table border="0">
                  <tr>
                    <td>
                  <a class="button" onclick="this.blur()"> 
                    <xsl:attribute name="href">javascript:showMap(true)</xsl:attribute>
                    <span><xsl:value-of select="/metaInf/resources/msg[@key='button.selectFromMap']/@value" /></span>
                  </a>   
                    </td>
                    <td>
                  <a class="button" onclick="this.blur()"> 
                    <xsl:attribute name="href">javascript:showMap()</xsl:attribute>
                    <span><xsl:value-of select="/metaInf/resources/msg[@key='button.test']/@value" /></span>
                  </a>
                    </td> 
                  </tr>
                </table>
              </li>
              
              <li style="padding-bottom:5px;">
                <select name="zoomFactor" style="width:45px;">
                  <xsl:for-each select="/metaInf/geoTag/zoomLevel/zoomFactor">
                    <option>
                      <xsl:if test="@current">
                        <xsl:attribute name="selected">selected</xsl:attribute>
                      </xsl:if>
                      <xsl:attribute name="value"><xsl:value-of select="." /></xsl:attribute>
                      <xsl:value-of select="." />
                    </option>
                  </xsl:for-each>
                </select>
                &#160;
                <xsl:value-of select="/metaInf/resources/msg[@key='label.zoomFactor']/@value" />
              </li>
              
            </xsl:if>
            
          </ul>

        </td>
      </tr>
        
      <xsl:if test="/metaInf/geoTag/googleMapsAPIKey">
        <tr>
          <td class="formParm1">
            &#160;
          </td>
          <td class="formParm2" width="80%">
            <textarea name="infoText" style="width:200;height:40px;vertical-align:top;" wrap="virtual"><xsl:value-of select="/metaInf/geoTag/infoText" /></textarea>
            &#160;
            <xsl:value-of select="/metaInf/resources/msg[@key='label.geoTagInfoText']/@value" />
          </td>
        </tr>
      </xsl:if>
      
      <xsl:if test="/metaInf/resources/msg[@key='label.textColor']">
        <tr> 
          <td class="formParm1">
            <xsl:value-of select="/metaInf/resources/msg[@key='label.textColor']/@value" />
          </td>
          <td class="formParm2">
            <input name="textColor" class="color" onblur="uncheckDefaultColor()">
              <xsl:if test="/metaInf/textColor">
                <xsl:attribute name="value"><xsl:value-of select="/metaInf/textColor" /></xsl:attribute>
              </xsl:if>
              <xsl:if test="not(/metaInf/textColor)">
                <xsl:attribute name="value">000000</xsl:attribute>
              </xsl:if>
            </input>

            &#160;

            <input type="checkbox" name="defaultColor" id="defaultColorCheckbox" class="cb3">
              <xsl:if test="not(/metaInf/textColor)">
                <xsl:attribute name="checked">checked</xsl:attribute>
              </xsl:if>
            </input>
            
            <xsl:value-of select="/metaInf/resources/msg[@key='noCustomColor']/@value" />
          </td>
        </tr>
      </xsl:if>

      <xsl:if test="/metaInf/resources/msg[@key='label.folderIcon']">
        <tr> 
          <td class="formParm1">
            <xsl:value-of select="/metaInf/resources/msg[@key='label.folderIcon']/@value" />
          </td>
          <td class="formParm2">
            <div style="width:300px;height:80px;overflow-x:auto;overflow-y:auto;border:1px solid #a0a0a0">
              <table border="0">
                <tr>
                  <td>
                    <input type="radio" name="icon" value="none" class="cb3">
                      <xsl:if test="not(/metaInf/icon)">
                        <xsl:attribute name="checked">checked</xsl:attribute>
                      </xsl:if>
                    </input>
                  </td>
                  <td>
                  </td>
                  <td class="formParm1">
                    <xsl:value-of select="/metaInf/resources/msg[@key='noCustomIcon']/@value" />
                  </td>
                </tr>
                <xsl:for-each select="/metaInf/availableIcons/icon">
                  <tr>
                    <td>
                      <input type="radio" name="icon" class="cb3">
                        <xsl:attribute name="value"><xsl:value-of select="." /></xsl:attribute>
                        <xsl:if test=". = /metaInf/icon">
                          <xsl:attribute name="checked">checked</xsl:attribute>
                        </xsl:if>
                      </input>
                    </td>
                    <td>
                      <img border="0" width="16" height="16">
                        <xsl:attribute name="src">/webfilesys/icons/<xsl:value-of select="." /></xsl:attribute>
                      </img>
                    </td>
                    <td class="formParm1">
                      <xsl:value-of select="." />
                    </td>
                  </tr>
                </xsl:for-each>
              </table>
            </div>
          </td>
        </tr>
      </xsl:if>

      <tr>
        <td class="formButton" nowrap="nowrap">
          <a class="button" onclick="this.blur()"> 
            <xsl:attribute name="href">javascript:document.form1.submit()</xsl:attribute>
              <span><xsl:value-of select="/metaInf/resources/msg[@key='button.save']/@value" /></span>
          </a>              
        </td>

        <td class="formButton" nowrap="nowrap">
          <a class="button" style="float:right" onclick="this.blur()"> 
            <xsl:if test="/metaInf/mobile">
              <xsl:attribute name="href">javascript:history.back()</xsl:attribute>
            </xsl:if>
            <xsl:if test="not(/metaInf/mobile)">
              <xsl:attribute name="href">javascript:window.close()</xsl:attribute>
            </xsl:if>
            <span><xsl:value-of select="/metaInf/resources/msg[@key='button.cancel']/@value" /></span>
          </a>              
        </td>
      </tr>

    </table>
  </form>
  
  <script language="javascript">
    document.form1.description.focus();
  </script>

</body>

<xsl:if test="/metaInf/geoTag">
  <div id="mapFrame" style="width:100%;height:100%;position:absolute;top:0px;left:0px;visibility:hidden;background-color:#d0d0d0;">
    <div id="map" style="width:100%;height:100%;position:absolute;top:0px;left:0px;"></div>
    
    <div style="width:16px;height:14px;position:absolute;bottom:10px;right:10px;">
      <a href="javascript:hideMap()">
        <img src="/webfilesys/images/winClose.gif" border="0" width="16" height="14">
          <xsl:attribute name="title"><xsl:value-of select="/metaInf/resources/msg[@key='button.cancel']/@value" /></xsl:attribute>
        </img>
      </a>
    </div>

    <div id="selectButton" style="width:42px;height:15px;position:absolute;bottom:10px;left:10px;visibility:hidden;">
      <form>
        <input type="button" value="OK" onclick="javascript:selectLocation()" style="width:40px;font-size:10pt;color:black;"/>
      </form>
    </div>
  </div>
</xsl:if>

</html>

</xsl:template>

</xsl:stylesheet>
