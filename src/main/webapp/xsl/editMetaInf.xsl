<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="metaInf" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/metaInf/css" />.css</xsl:attribute>
</link>

<script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
<script type="text/javascript" src="/webfilesys/javascript/jscolor/jscolor.js"></script>

<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
<script type="text/javascript">
  <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/metaInf/language" /></xsl:attribute>
</script>

<title resource="label.editMetaInfo"></title>

<script language="javascript">
  var returnWin = '';

  function checkLengthAndSubmit()
  {  
      if (document.form1.description.value.length>1024)
      {  
          alert(resourceBundle["alert.descriptionTooLong"]);
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
  
    function loadGoogleMapsAPIScriptCode() {
        var script = document.createElement("script");
        script.type = "text/javascript";
        script.src = "http://maps.google.com/maps/api/js?sensor=false&amp;callback=handleGoogleMapsApiReady";
        document.body.appendChild(script);
    }
  
    function handleGoogleMapsApiReady() {
        // console.log("Google Maps API loaded");
    }
  
    var posMarker;
  
    function selectLocation()
    {
        var markerPos = posMarker.getPosition();
    
        document.form1.latitude.value = markerPos.lat(); 
        document.form1.longitude.value = markerPos.lng();
        
        hideMap();
    }
  
    function showMap(selectLocation)
    {
        var latitude = document.form1.latitude.value;

        var coordinatesNotYetSelected = false;

        if (latitude == '')
        {
            coordinatesNotYetSelected = true;
            
            if (selectLocation)
            {
                latitude = '51.1';
            }
            else
            {
                alert(resourceBundle["alert.missingLatitude"]);
                return;
            }
        }
  
        var longitude = document.form1.longitude.value;

        if (longitude == '')
        {
            coordinatesNotYetSelected = true;

            if (selectLocation)
            {
                longitude = '13.76';
            }
            else
            {
                alert(resourceBundle["alert.missingLongitude"]);
                return;
            }
        }

        var zoomFactor = parseInt(document.form1.zoomFactor[document.form1.zoomFactor.selectedIndex].value);
      
        var infoText;

        if (selectLocation)
        {
            infoText = resourceBundle["label.hintGoogleMapSelect"];
        }   
        else
        {
            infoText = document.form1.infoText.value;
        }        
      
        var mapCenter = new google.maps.LatLng(latitude, longitude);
    
        var myOptions = {
            zoom: zoomFactor,
            center: mapCenter,
            mapTypeId: google.maps.MapTypeId.HYBRID
        }
      
        var map = new google.maps.Map(document.getElementById("map"), myOptions);      
          
        if (selectLocation)
        {
            document.getElementById("selectButton").style.visibility = 'visible';
        }

        var markerPos = new google.maps.LatLng(latitude, longitude);

        posMarker = new google.maps.Marker({
            position: markerPos,
        });

        posMarker.setMap(map);
        
        if ((selectLocation &amp;&amp; coordinatesNotYetSelected) ||
            (!selectLocation &amp;&amp; (infoText != '')))
        {
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

    function hideMap()
    {
        document.getElementById("selectButton").style.visibility = 'hidden';

        document.getElementById("mapFrame").style.visibility = 'hidden';
    }
    
  </xsl:if>
</script>

</head>

<body>
  <xsl:if test="/metaInf/geoTag">
    <xsl:attribute name="onload">loadGoogleMapsAPIScriptCode()</xsl:attribute>
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
          <span resource="label.description"></span>
          
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
          <span resource="label.geoTag"></span>
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
              <span resource="label.latitude"></span>
            </li>

            <li style="padding-bottom:5px;">
              <input name="longitude" style="width:60px;">
                <xsl:if test="/metaInf/geoTag/longitude">
                  <xsl:attribute name="value"><xsl:value-of select="/metaInf/geoTag/longitude" /></xsl:attribute>
                </xsl:if>
              </input>
              &#160;
              <span resource="label.longitude"></span>
            </li>
              
            <xsl:if test="/metaInf/geoTag/mapSelection">
      
              <li style="padding-bottom:5px;">
                <table border="0">
                  <tr>
                    <td>
                      <input type="button" resource="button.selectFromMap">
                        <xsl:attribute name="onclick">javascript:showMap(true)</xsl:attribute>
                      </input> 
                    </td>
                    <td>
                      <input type="button" resource="button.preview">
                        <xsl:attribute name="onclick">javascript:showMap()</xsl:attribute>
                      </input> 
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
                <span resource="label.zoomFactor"></span>
              </li>
              
            </xsl:if>
            
          </ul>

        </td>
      </tr>
        
      <xsl:if test="/metaInf/geoTag/mapSelection">
        <tr>
          <td class="formParm1">
            &#160;
          </td>
          <td class="formParm2" width="80%">
            <textarea name="infoText" style="width:200;height:40px;vertical-align:top;" wrap="virtual"><xsl:value-of select="/metaInf/geoTag/infoText" /></textarea>
            &#160;
            <span resource="label.geoTagInfoText"></span>
          </td>
        </tr>
      </xsl:if>
      
      <xsl:if test="/metaInf/folder">
        <tr> 
          <td class="formParm1">
            <span resource="label.textColor"></span>
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
            
            <span resource="noCustomColor"></span>
          </td>
        </tr>
      </xsl:if>

      <xsl:if test="/metaInf/folder">
        <tr> 
          <td class="formParm1">
            <span resource="label.folderIcon"></span>
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
                    <span resource="noCustomIcon"></span>
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
        <td class="formButton">
          <input type="button" resource="button.save">
            <xsl:attribute name="onclick">javascript:checkLengthAndSubmit()</xsl:attribute>
          </input> 
        </td>

        <td class="formButton">
          <input type="button" resource="button.cancel" style="float:right">
            <xsl:if test="/metaInf/mobile">
              <xsl:attribute name="onclick">javascript:history.back()</xsl:attribute>
            </xsl:if>
            <xsl:if test="not(/metaInf/mobile)">
              <xsl:attribute name="onclick">javascript:window.close()</xsl:attribute>
            </xsl:if>
          </input> 
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
    
    <div style="position:absolute;bottom:15px;left:10px;"> 

      <form>
        <input id="closeButton" type="button" resource="button.closeMap" onclick="hideMap()" 
            style="font-size:13px;font-weight:bold;color:black;"/>

        <input id="selectButton" type="button" resource="button.save" onclick="javascript:selectLocation()" 
            style="visibility:hidden;font-size:13px;font-weight:bold;color:black;"/>
      </form>
      
    </div>

  </div>
</xsl:if>

<script type="text/javascript">
  setBundleResources();
</script>

</html>

</xsl:template>

</xsl:stylesheet>
