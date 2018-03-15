<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="slideShow" />

<!-- root node-->
<xsl:template match="/">

<html>

<head>

  <title><xsl:value-of select="/slideShow/resources/msg[@key='label.slideshow']/@value" /></title>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/slideShow/css" />.css</xsl:attribute>
  </link>

  <script src="javascript/browserCheck.js" type="text/javascript" />

  <script src="javascript/ajaxCommon.js" type="text/javascript" />

  <script src="javascript/util.js" type="text/javascript" />
  
  <script src="javascript/slideshow.js" type="text/javascript"></script>
  <script src="javascript/slideShowActions.js" type="text/javascript"></script>
  
  <script type="text/javascript">
    var imageIdx = <xsl:value-of select="/slideShow/startIdx" />;

    var numberOfImages = <xsl:value-of select="/slideShow/numberOfImages" />;

    var autoForward = <xsl:value-of select="/slideShow/autoForward" />;
    
    var pauseGoTitle = '<xsl:value-of select="/slideShow/resources/msg[@key='alt.continue']/@value" />';
  
    var slideShowDelay = <xsl:value-of select="/slideShow/delay" />;
    
    var pauseTitle = '<xsl:value-of select="/slideShow/resources/msg[@key='alt.pause']/@value" />';
  
    var continueTitle = '<xsl:value-of select="/slideShow/resources/msg[@key='alt.continue']/@value" />';
    
    var fadeEnabled = false;
    <xsl:if test="/slideShow/fadeInOut">
        fadeEnabled = true;
    </xsl:if>
    
  </script>
  
</head>

<body style="margin:0px;border:0px;background-color:#c0c0c0;padding:0px;">
  <xsl:attribute name="onload">setTimeout('self.focus()', 500); initSlideshow(); loadImage();if (autoForward!='true') {showActionButtons();};</xsl:attribute>

  <xsl:apply-templates />

</body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="slideShow">

  <div id="centerDiv" width="100%" style="margin:0px;padding:0px;text-align:center;">
    
    <img id="slideShowImg0" border="0" class="thumb" style="position:absolute;opacity:1">
      <xsl:attribute name="src">/webfilesys/images/space.gif</xsl:attribute>
      <xsl:attribute name="onMouseOver">javascript:showActionButtons()</xsl:attribute>
    </img>

    <img id="slideShowImg1" border="0" class="thumb" style="position:absolute;opacity:0">
      <xsl:attribute name="src">/webfilesys/images/space.gif</xsl:attribute>
      <xsl:attribute name="onMouseOver">javascript:showActionButtons()</xsl:attribute>
    </img>
      
  </div>
  
  <!-- button div -->

  <div id="buttonDiv" 
       style="position:absolute;top:6px;left:6px;width=76px;height=20px;padding:5px;background-color:ivory;text-align:center;border-style:solid;border-width:1px;border-color:#000000;visibility:hidden">
    
    <xsl:if test="/slideShow/autoForward='true'">
      <a href="javascript:hideActionButtons()">
        <img src="/webfilesys/images/winClose.gif" border="0" style="float:right;" />
      </a>

      <br/>
    </xsl:if>		
    
    <xsl:if test="/slideShow/autoForward='false'">
      <a href="javascript:goBack()">
        <img id="privious" src="/webfilesys/images/prev.png" border="0" width="22" height="22">
          <xsl:attribute name="title"><xsl:value-of select="/slideShow/resources/msg[@key='alt.back']/@value" /></xsl:attribute>
        </img>
      </a>
    </xsl:if>

    <a href="javascript:self.close()">
      <img src="/webfilesys/images/exit.gif" border="0">
        <xsl:attribute name="title"><xsl:value-of select="/slideShow/resources/msg[@key='alt.exitslideshow']/@value" /></xsl:attribute>
      </img>
    </a>

    <xsl:if test="/slideShow/autoForward">
      <a id="stopAndGoLink" href="javascript:stopAndGo()">
        <xsl:if test="/slideShow/autoForward='true'">
          <img id="pauseGo" src="/webfilesys/images/pause.gif" border="0">
            <xsl:attribute name="title"><xsl:value-of select="/slideShow/resources/msg[@key='alt.pause']/@value" /></xsl:attribute>
          </img>
        </xsl:if>
          
        <xsl:if test="/slideShow/autoForward='false'">
          <img id="pauseGo" src="/webfilesys/images/next.png" border="0" width="22" height="22">
            <xsl:attribute name="title"><xsl:value-of select="/slideShow/resources/msg[@key='alt.continue']/@value" /></xsl:attribute>
          </img>
        </xsl:if>
      </a>
    </xsl:if>
  </div>
  
  <a id="fullScreenButton" href="javascript:void(0)" onclick="javascript:makeSlideshowFullscreen()" style="position:absolute;top:10px;right:10px;">
    <img src="/webfilesys/images/fullscreen.png">
      <xsl:attribute name="title"><xsl:value-of select="/slideShow/resources/msg[@key='fullScreenMode']/@value" /></xsl:attribute>
    </img>    
  </a>
  
</xsl:template>

</xsl:stylesheet>
