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

    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />

    <link rel="stylesheet" type="text/css">
      <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/compareImage/css" />.css</xsl:attribute>
    </link>

	<link rel="stylesheet" href="/webfilesys/styles/imgCompSlider.css" />

    <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/ajax.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/showImage.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/compareImage.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/jquery/jquery.min.js"></script>

    <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/compareImage/language" /></xsl:attribute>
    </script>

  </head>

  <body class="compImg">
    <xsl:attribute name="onload">sizeCompareImagesToFit()</xsl:attribute>
	<figure class="cd-image-container">
	    <xsl:attribute name="id">compImgCont</xsl:attribute>
	    <!--  
	    <xsl:attribute name="style">width:<xsl:value-of select="/compareImage/maxWidth" />px</xsl:attribute>
		-->
		
		<img>
		  <xsl:attribute name="id">picture1</xsl:attribute>
		  <xsl:attribute name="src"><xsl:value-of select="/compareImage/image1/path" /></xsl:attribute>
		  <xsl:attribute name="width">1</xsl:attribute>
		  <xsl:attribute name="height">1</xsl:attribute>
	      <xsl:attribute name="style">display:none</xsl:attribute>
		</img>
		
		<span class="cd-image-label" data-type="original"><xsl:value-of select="/compareImage/image1/name" /></span>
		
        <div class="deleteFromCompareIcon1">
          <a class="icon-font icon-delete deleteFromCompareIcon" titleResource="label.delete">
            <xsl:attribute name="href">javascript:confirmDelImg('<xsl:value-of select="/compareImage/image2/name" />')</xsl:attribute>
          </a>		
        </div>

		<div class="cd-resize-img">
	      <img>
		    <xsl:attribute name="id">picture2</xsl:attribute>
		    <xsl:attribute name="src"><xsl:value-of select="/compareImage/image2/path" /></xsl:attribute>
		    <xsl:attribute name="width">1</xsl:attribute>
		    <xsl:attribute name="height">1</xsl:attribute>
		    <xsl:attribute name="style">display:none</xsl:attribute>
		  </img>
		  
		  <span class="cd-image-label" data-type="modified"><xsl:value-of select="/compareImage/image2/name" /></span>
		</div>

        <div class="deleteFromCompareIcon2">
          <a class="icon-font icon-delete deleteFromCompareIcon" titleResource="label.delete">
            <xsl:attribute name="href">javascript:confirmDelImg('<xsl:value-of select="/compareImage/image1/name" />')</xsl:attribute>
          </a>		
        </div>

		<span class="cd-handle"></span>
	</figure>

    <!-- done later in onload Javascript  
    <script src="/webfilesys/javascript/imgCompSlider/imgCompSlider.js"></script>
    -->

  </body>

  <script type="text/javascript">
    setBundleResources();
  </script>

</html>

</xsl:template>

</xsl:stylesheet>
