<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="slideShow" />

<!-- root node-->
<xsl:template match="/">

<html style="height:100%">

<head>

  <title resource="label.slideshow"></title>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/slideShow/css" />.css</xsl:attribute>
  </link>

  <script src="javascript/browserCheck.js" type="text/javascript" />

  <script src="javascript/ajaxCommon.js" type="text/javascript" />

  <script src="javascript/util.js" type="text/javascript" />
  
  <script src="javascript/slideshow.js" type="text/javascript"></script>
  <script src="javascript/slideShowActions.js" type="text/javascript"></script>
  
  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/slideShow/language" /></xsl:attribute>
  </script>
  
  <script type="text/javascript">
    var imageIdx = <xsl:value-of select="/slideShow/startIdx" />;

    var numberOfImages = <xsl:value-of select="/slideShow/numberOfImages" />;

    var autoForward = <xsl:value-of select="/slideShow/autoForward" />;
    
    var pauseGoTitle = resourceBundle["alt.continue"];
  
    var slideShowDelay = <xsl:value-of select="/slideShow/delay" />;
    
    var pauseTitle = resourceBundle["alt.pause"];
  
    var continueTitle = resourceBundle["alt.continue"];  
    
    var fadeEnabled = false;
    <xsl:if test="/slideShow/fadeInOut">
        fadeEnabled = true;
    </xsl:if>
    
  </script>
  
</head>

<body class="slideshowFullscreen">
  <xsl:attribute name="onload">setTimeout('self.focus()', 500); initSlideshow(); loadImage();if (autoForward!='true') {showActionButtons();};</xsl:attribute>

  <xsl:apply-templates />

</body>

<script type="text/javascript">
  setBundleResources();
</script>

</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="slideShow">

  <div id="centerDiv" class="slideshowCont">

    <xsl:if test="/slideShow/autoForward='false'">
      <xsl:attribute name="onClick">stopAndGo()</xsl:attribute>
    </xsl:if>

    <img id="slideShowImg0" style="opacity:1">
      <xsl:attribute name="src">/webfilesys/images/space.gif</xsl:attribute>
      <xsl:attribute name="onMouseOver">javascript:showActionButtons()</xsl:attribute>
      <xsl:if test="/slideShow/autoForward='true'">
        <xsl:attribute name="class">slideshowImg thumb</xsl:attribute>
      </xsl:if>
      <xsl:if test="/slideShow/autoForward='false'">
        <xsl:attribute name="class">slideshowImg thumb slideshowNextPointer</xsl:attribute>
      </xsl:if>
    </img>

    <img id="slideShowImg1" style="opacity:0">
      <xsl:attribute name="src">/webfilesys/images/space.gif</xsl:attribute>
      <xsl:attribute name="onMouseOver">javascript:showActionButtons()</xsl:attribute>
      <xsl:if test="/slideShow/autoForward='true'">
        <xsl:attribute name="class">slideshowImg thumb</xsl:attribute>
      </xsl:if>
      <xsl:if test="/slideShow/autoForward='false'">
        <xsl:attribute name="class">slideshowImg thumb slideshowNextPointer</xsl:attribute>
      </xsl:if>
    </img>
      
  </div>
  
  <!-- button div -->

  <div id="buttonDiv" 
       style="position:absolute;top:6px;left:6px;width=76px;height=20px;padding:5px;background-color:#ffffff;text-align:center;border-style:solid;border-width:1px;border-color:#000000;white-space:nowrap;visibility:hidden">
    
    <xsl:if test="/slideShow/autoForward='true'">
      <a href="javascript:hideActionButtons()">
        <img src="/webfilesys/images/winClose.gif" border="0" style="float:right;" />
      </a>

      <br/>
    </xsl:if>		
    
    <xsl:if test="/slideShow/autoForward='false'">
      <a href="javascript:goBack()">
        <img id="previous" src="/webfilesys/images/prev.png" class="slideshowControl" titleResource="alt.back"></img>
      </a>
    </xsl:if>

    <a href="javascript:self.close()" class="icon-font icon-cancel icon-cancel-slideshow" titleResource="alt.exitslideshow"></a>

    <xsl:if test="/slideShow/autoForward">
      <a id="stopAndGoLink" href="javascript:stopAndGo()">
        <xsl:if test="/slideShow/autoForward='true'">
          <img id="pauseGo" src="/webfilesys/images/pause.png" border="0" style="vertical-align:text-bottom" titleResource="alt.pause"></img>
        </xsl:if>
          
        <xsl:if test="/slideShow/autoForward='false'">
          <img id="pauseGo" src="/webfilesys/images/next.png" class="slideshowControl" titleResource="alt.continue"></img>
        </xsl:if>
      </a>
    </xsl:if>
  </div>
  
  <a id="fullScreenButton" href="javascript:void(0)" onclick="javascript:makeSlideshowFullscreen()" style="position:absolute;top:10px;right:10px;">
    <img src="/webfilesys/images/fullscreen.png" titleResource="fullScreenMode"></img>    
  </a>
  
</xsl:template>

</xsl:stylesheet>
