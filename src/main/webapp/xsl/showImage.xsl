<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="imageData" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <title><xsl:value-of select="/imageData/relativePath" /></title>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/imageData/css" />.css</xsl:attribute>
  </link>

  <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/showImage.js" type="text/javascript"></script>
  <script src="javascript/jsFileMenu.js" type="text/javascript" />
  <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajax.js" type="text/javascript"></script>

  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/imageData/language" /></xsl:attribute>
  </script>

  <xsl:if test="/imageData/geoTag">
    <script src="/webfilesys/javascript/geoMap.js" type="text/javascript"></script>
  </xsl:if>

  <script language="JavaScript">
    function exifData()
    {
        exifWin=window.open('/webfilesys/servlet?command=exifData&amp;imgFile=<xsl:value-of select="/imageData/encodedPath" />','exifWin','scrollbars=yes,status=no,toolbar=no,location=no,menu=no,width=400,height=480,left=200,top=100,screenX=200,screenY=100,resizable=no');
        exifWin.focus();
    }
    
    function scale()
    {
        scaleWin = window.open('/webfilesys/servlet?command=resizeParms&amp;imgFile=<xsl:value-of select="/imageData/encodedPath" />&amp;popup=true','resizeWin' + (new Date()).getTime(),'status=no,toolbar=no,menu=no,width=400,height=560,resizable=no,scrollbars=yes,screenX=180,screenY=20,left=180,top=20');
        scaleWin.focus();
    }
    
    function actionSelected() {
        var imgAction = document.getElementById("imgAction");
        
        var selectedAction = imgAction[imgAction.selectedIndex].value;
    
        if (selectedAction == '5') {
            comments('<xsl:value-of select="/imageData/pathForScript" />');
        } else if (selectedAction == '1') {
            deleteSelf('<xsl:value-of select="/imageData/pathForScript" />');
        } else if (selectedAction == '3') {
            exifData();
        } else if (selectedAction == '2') {
            scale();
        } else if (selectedAction == '4') {
            printPage();
        } else if (selectedAction == '0') {
            detailWin = window.open('<xsl:value-of select="/imageData/imageSource" />', 'detailWin')
            detailWin.focus();
        }
        
        imgAction.selectedIndex = 0;
    }
    
  </script>

</head>

<body class="picFullScreen">
  <xsl:attribute name="onload">scaleImage(<xsl:value-of select="/imageData/imageWidth" />, <xsl:value-of select="/imageData/imageHeight" />)</xsl:attribute>

  <!--  
  <div id="toolTip" class="fullScreenToolTip"></div>
  -->  
    
  <div class="picFullScreenCont">
    
    <img id="picFullScreen" class="picFullScreen">
      <xsl:attribute name="src"><xsl:value-of select="/imageData/imageSource" /></xsl:attribute>
      <xsl:attribute name="width">1</xsl:attribute>
      <xsl:attribute name="height">1</xsl:attribute>
      <xsl:if test="/imageData/description">
        <xsl:attribute name="title"><xsl:value-of select="/imageData/description" /></xsl:attribute>
        <!--  
        <xsl:attribute name="onMouseOver">showToolTip('<xsl:value-of select="/imageData/description" />')</xsl:attribute>
        <xsl:attribute name="onMouseOut">hideToolTip()</xsl:attribute>
        -->
      </xsl:if>
    </img>
      
  </div>
  
  <div class="picInfoMenuIcon" onclick="showPicInfoMenu()" titleResource="pictureMenuIconTitle">
    <a href="javascript:showPicInfoMenu()" class="icon-font icon-menu" />
  </div>

</body>

<div id="picInfoMenuCont" class="picInfoMenu">
  <div class="picInfoMenuClose" onclick="hidePicInfoMenu()">
    <img width="16" height="14" border="0" src="images/winClose.gif" onclick="hidePicInfoMenu()" />
  </div>
  
  <table>
    <tr>
      <td><label resource="label.picDimensions" />:</td>
      <td class="picInfoMenuVal"><xsl:value-of select="/imageData/imageWidth" /> x <xsl:value-of select="/imageData/imageHeight" /> pix</td>
    </tr>
    
    <tr>
      <td>
        <span resource="rating.owner"></span>:
      </td>

      <td class="plaintext" nowrap="nowrap">
        <xsl:if test="/imageData/ownerRating">
          <xsl:if test="/imageData/ownerRating = 5">
            <img src="images/5-stars.gif" border="0" />
          </xsl:if>
          <xsl:if test="/imageData/ownerRating = 4">
            <img src="images/4-stars.gif" border="0" />
          </xsl:if>
          <xsl:if test="/imageData/ownerRating = 3">
            <img src="images/3-stars.gif" border="0" />
          </xsl:if>
          <xsl:if test="/imageData/ownerRating = 2">
            <img src="images/2-stars.gif" border="0" />
          </xsl:if>
          <xsl:if test="/imageData/ownerRating = 1">
            <img src="images/1-stars.gif" border="0" />
          </xsl:if>
        </xsl:if>
        <xsl:if test="not(/imageData/ownerRating)">
          <span resource="rating.notYetRated"></span>
        </xsl:if>
      </td>
    </tr>
      
    <tr>
      <td>
        <span resource="rating.visitor"></span><xsl:if test="/imageData/voteCount"> (<xsl:value-of select="/imageData/voteCount" />)</xsl:if>:
      </td>
      
      <td class="plaintext" nowrap="nowrap">
        <xsl:if test="/imageData/visitorRating">
          <xsl:if test="/imageData/visitorRating = 5">
            <img src="images/5-stars.gif" border="0" />
          </xsl:if>
          <xsl:if test="/imageData/visitorRating = 4">
            <img src="images/4-stars.gif" border="0" />
          </xsl:if>
          <xsl:if test="/imageData/visitorRating = 3">
            <img src="images/3-stars.gif" border="0" />
          </xsl:if>
          <xsl:if test="/imageData/visitorRating = 2">
            <img src="images/2-stars.gif" border="0" />
          </xsl:if>
          <xsl:if test="/imageData/visitorRating = 1">
            <img src="images/1-stars.gif" border="0" />
          </xsl:if>
        </xsl:if>
        <xsl:if test="not(/imageData/visitorRating)">
          <span resource="rating.notYetRated"></span>
        </xsl:if>
      </td>
    </tr>
    
    <xsl:if test="not(/imageData/readonly) or (/imageData/readonly = 'false') or (/imageData/ratingAllowed and (/imageData/ratingAllowed = 'true'))">
      <tr>
        <td><span resource="rating.rateNow"></span>:</td>
        <td>
          <form accept-charset="utf-8" name="form1" method="get" action="/webfilesys/servlet" style="margin:0px;padding:0px;padding-top:4px;">
            <input type="hidden" name="command" value="rate" />
            <input type="hidden" name="imagePath">
              <xsl:attribute name="value"><xsl:value-of select="/imageData/imagePath" /></xsl:attribute>
            </input>
        
            <select name="rating">
              <xsl:attribute name="onChange">javascript:rate()</xsl:attribute>
              <option value="-1"> - </option>
              <option value="1" resource="rating.1star" />
              <option value="2" resource="rating.2stars" />
              <option value="3" resource="rating.3stars" />
              <option value="4" resource="rating.4stars" />
              <option value="5" resource="rating.5stars" />
            </select>
          </form>
        </td>
      </tr>
    </xsl:if>    
      
    <tr>
      <td><label resource="label.comments" />:</td>
      <td>
        <a>
          <xsl:attribute name="href">javascript:comments('<xsl:value-of select="/imageData/pathForScript" />')</xsl:attribute>
          <xsl:if test="/imageData/commentCount">
            <xsl:value-of select="/imageData/commentCount" />
          </xsl:if>
          <xsl:if test="not(/imageData/commentCount)">
            0
          </xsl:if>
        </a>
      </td>
    </tr>
    
    <xsl:if test="/imageData/geoTag">
      <tr>
        <td>
          <label resource="label.geoLocation" />:
        </td>            
      
        <td valign="top">
          <select id="geoLocSel" style="width:150px;">
            <xsl:attribute name="onchange">geoMapFileSelected('<xsl:value-of select="/imageData/pathForScript" />')</xsl:attribute>
            <option value="0" resource="selectMapType" />
            <option value="1" resource="mapTypeOSM" />
            <xsl:if test="/imageData/googleMaps">
              <option value="2" resource="mapTypeGoogleMap" />
            </xsl:if>
            <option value="3" resource="mapTypeGoogleEarth" />
          </select>
        </td>
      </tr>
      
    </xsl:if>
    
    <tr>
      <td>
        <span resource="label.action" />:
      </td>
      <td>
        <select id="imgAction" name="imgAction">
          <xsl:attribute name="onChange">actionSelected()</xsl:attribute>
          <option value="-1" resource="label.selectFunction" />
          <option value="0" id="origSizeOption" resource="label.origSize" style="display:none" />
          <xsl:if test="not(/imageData/readonly) or (/imageData/readonly = 'false')">
            <option value="1" resource="label.delete" />
            <option value="2" resource="label.editPicture" />
          </xsl:if>
          <xsl:if test="/imageData/imageType = '1'">
            <option value="3" resource="alt.cameradata" />
          </xsl:if>
          <option value="4" resource="alt.printpict" />
          <option value="5" resource="label.comments" />
        </select>
      </td>
    </tr>
    
  </table>
</div>

<script type="text/javascript">
  setBundleResources();
</script>

</html>

</xsl:template>

</xsl:stylesheet>
