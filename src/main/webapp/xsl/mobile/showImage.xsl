<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="imageData" />

<xsl:template match="/">

<html>
<head>

  <title><xsl:value-of select="/imageData/relativePath" /></title>

  <meta http-equiv="expires" content="0" />

  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=yes" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/mobile.css" />.css</xsl:attribute>
  </link>

  <script src="javascript/titleToolTip.js" type="text/javascript" />
  
  <script type="text/javascript">
    function showNavigation()
    {
        document.getElementById('navigationPrevDiv').style.visibility = 'visible';
        document.getElementById('navigationNextDiv').style.visibility = 'visible';
        document.getElementById('navigationReturn').style.visibility = 'visible';
    }
    
    function prevImage() 
    {
        window.location.href = '/webfilesys/servlet?command=mobile&amp;cmd=showImg&amp;before=<xsl:value-of select="/imageData/imagePath" />';
    }

    function nextImage() 
    {
        window.location.href = '/webfilesys/servlet?command=mobile&amp;cmd=showImg&amp;after=<xsl:value-of select="/imageData/imagePath" />';
    }

    function back() 
    {
        window.location.href = '/webfilesys/servlet?command=mobile&amp;cmd=folderFileList';
    }
  </script>

</head>

<body style="margin:0px;border:0px;" onclick="showNavigation()">

    <div style="width:100%;height:100%;padding:0px;text-align:center;vertical-align:middle;">
    
      <img border="0" style="border-width:0;">
        <xsl:attribute name="src"><xsl:value-of select="/imageData/imageSource" /></xsl:attribute>
        <xsl:attribute name="width"><xsl:value-of select="/imageData/displayWidth" /></xsl:attribute>
        <xsl:attribute name="height"><xsl:value-of select="/imageData/displayHeight" /></xsl:attribute>
        <xsl:if test="/imageData/description">
          <xsl:attribute name="onMouseOver">showToolTip('<xsl:value-of select="/imageData/description" />')</xsl:attribute>
          <xsl:attribute name="onMouseOut">hideToolTip()</xsl:attribute>
        </xsl:if>
      </img>
      
    </div>

</body>

<div id="toolTip" style="position:absolute;top:200px;left:100px;width=200px;height=20px;padding:5px;background-color:ivory;border-style:solid;border-width:1px;border-color:#000000;visibility:hidden"></div>

<div id="navigationPrevDiv" onclick="prevImage()"
    style="position:absolute;top:5px;left:5px;width=20px;height=22px;padding:10px;background-color:ivory;border-style:solid;border-width:1px;border-color:#000000;visibility:hidden">
  <a>
    <xsl:attribute name="href">javascript:prevImage()</xsl:attribute>
    <img src="/webfilesys/images/previous.gif" width="7" height="12" border="0" />
  </a>
</div>

<div id="navigationNextDiv" onclick="nextImage()" 
    style="position:absolute;top:5px;right:5px;width=20px;height=22px;padding:10px;background-color:ivory;border-style:solid;border-width:1px;border-color:#000000;visibility:hidden">
  <a>
    <xsl:attribute name="href">javascript:nextImage()</xsl:attribute>
    <img src="/webfilesys/images/next.gif" width="7" height="12" border="0" />
  </a>
</div>

<div id="navigationReturn" onclick="back()"
    style="position:absolute;bottom:5px;left:5px;width=20px;height=21px;padding:10px;background-color:ivory;border-style:solid;border-width:1px;border-color:#000000;visibility:hidden">
  <a>
    <xsl:attribute name="href">javascript:back()</xsl:attribute>
    <img src="/webfilesys/images/ascii.gif" width="9" height="11" border="0" />
  </a>
</div>

</html>

</xsl:template>

</xsl:stylesheet>
