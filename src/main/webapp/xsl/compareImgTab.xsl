<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="compareImage" />

<xsl:template match="/">

<html>
  <head>

    <meta http-equiv="expires" content="0" />
    
    <title>WebFileSys Compare Images: <xsl:value-of select="/compareImage/relativePath" /></title>

    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />

    <link rel="stylesheet" type="text/css">
      <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/compareImage/css" />.css</xsl:attribute>
    </link>

    <script src="/webfilesys/javascript/jquery/jquery.min.js" type="text/javascript"></script>

    <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/ajax.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/compareImage.js" type="text/javascript"></script>

    <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/compareImage/language" /></xsl:attribute>
    </script>

    <script type="text/javascript">
        
        var newWinWidth = screen.availWidth - 20;
        var newWinHeight = screen.availHeight - 64;
        
        resizeViewPort(newWinWidth, newWinHeight);
        window.moveTo(screen.availWidth / 2 - (newWinWidth / 2), 1);
        
    </script>

  </head>

  <body class="compImg" onload="compareImgLoadInitial()">
  
    <div id="imgCompThumbCont" class="imgCompThumbCont">

      <xsl:for-each select="/compareImage/fileList/file">
      
        <xsl:if test="position() = 1">
          <script type="text/javascript">
            var firstImagePath = '<xsl:value-of select="/compareImage/pathForScript" /><xsl:value-of select="@nameForScript" />';
            var firstImageThumbContId = 'thumbCont-<xsl:value-of select="@nameForId" />';
          </script>
        </xsl:if>
      
        <div class="imgCompThumb">
          <xsl:attribute name="id">thumbCont-<xsl:value-of select="@nameForId" /></xsl:attribute>
        
          <a>
            <xsl:attribute name="onclick">compareShowImage('<xsl:value-of select="/compareImage/pathForScript" /><xsl:value-of select="@nameForScript" />', this);</xsl:attribute>
            <img class="imgCompThumb" border="0">
               <xsl:attribute name="src"><xsl:value-of select="imgPath" /></xsl:attribute>
               <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
               <xsl:attribute name="onload">resizeCompareThumb(this)</xsl:attribute>
            </img>
          </a>
          
          <div class="imgCompActionCont">
            <a class="icon-font icon-delete imgCompButton" titleResource="compImg.deleteTitle">
              <xsl:attribute name="href">javascript:compareImgDelete('<xsl:value-of select="/compareImage/pathForScript" />', '<xsl:value-of select="@nameForScript" />')</xsl:attribute>
            </a>
            <a class="icon-font icon-check imgCompButton" titleResource="compImg.closeTitle">
              <xsl:attribute name="href">javascript:compareImgClose('<xsl:value-of select="@nameForScript" />')</xsl:attribute>
            </a>
          </div>
        </div>  

      </xsl:for-each> 

    </div>
    
    <div id="imgCompPicCont" class="imgCompPicCont">
      <img id="picture" class="imgCompPic">
        <xsl:attribute name="src">/webfilesys/images/space.gif</xsl:attribute>
      </img>
    </div>

  </body>

  <script type="text/javascript">
    setBundleResources();
  </script>

</html>

</xsl:template>

</xsl:stylesheet>
