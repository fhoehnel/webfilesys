<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="cameraData" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

  <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxGraphics.js" type="text/javascript"></script>
  
  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/cameraData/language" /></xsl:attribute>
  </script>

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/cameraData/css" />.css</xsl:attribute>
  </link>

  <title>
    WebFileSys Exif Data: 
    <xsl:value-of select="/cameraData/shortImgName" />
  </title>

</head>

<body class="cameraData">

  <div class="headline" resource="alt.cameradata"></div>
  
  <form accept-charset="utf-8" name="form1" style="padding-top:5px;">

    <table class="dataForm" width="100%">
   
      <tr>
        <td colspan="2" class="formParm1">
          <span resource="label.picturefile"></span>
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm2" style="padding-left:16px">
          <xsl:value-of select="/cameraData/shortImgName" />
        </td>
      </tr>

      <xsl:if test="not(/cameraData/exifData)">
        <script type="text/javascript">
          toast(resourceBundle["alert.nocameradata"], 5000);
        </script>
      </xsl:if>

      <xsl:if test="/cameraData/exifData">

        <xsl:if test="/cameraData/exifData/manufacturer">
          <tr>
            <td class="formParm1">
              <span resource="label.manufacturer"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/manufacturer" />
            </td>
          </tr>
        </xsl:if>
        
        <xsl:if test="/cameraData/exifData/cameraModel">
          <tr>
            <td class="formParm1">
              <span resource="label.cameramodel"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/cameraModel" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/exposureDate">
          <tr>
            <td class="formParm1">
              <span resource="label.exposuredate"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/exposureDate" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/exposureTime">
          <tr>
            <td class="formParm1">
              <span resource="label.exposuretime"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/exposureTime" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/aperture">
          <tr>
            <td class="formParm1">
              <span resource="label.aperture"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/aperture" />
            </td>
          </tr>
        </xsl:if>
        
        <xsl:if test="/cameraData/exifData/isoValue">
          <tr>
            <td class="formParm1">
              <span resource="label.isoValue"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/isoValue" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/focalLength">
          <tr>
            <td class="formParm1">
              <span resource="label.focalLength"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/focalLength" /> mm
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/flashFired">
          <tr>
            <td class="formParm1">
              <span resource="label.flashfired"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/flashFired" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/exposureBias">
          <tr>
            <td class="formParm1">
              <span resource="label.exposureBias"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/exposureBias" />
              <xsl:text> </xsl:text>
              <span resource="apertureStops"></span>
            </td>
          </tr>
        </xsl:if>
        
        <xsl:if test="/cameraData/exifData/gpsLatitude">
          <tr>
            <td class="formParm1">
              <span resource="label.gpsLatitude"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/gpsLatitude" />
            </td>
          </tr>
        </xsl:if>
        
        <xsl:if test="/cameraData/exifData/gpsLongitude">
          <tr>
            <td class="formParm1">
              <span resource="label.gpsLongitude"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/gpsLongitude" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/imgWidth">
          <tr>
            <td class="formParm1">
              <span resource="label.imgwidth"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/imgWidth" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/imgHeight">
          <tr>
            <td class="formParm1">
              <span resource="label.imgheight"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/imgHeight" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/orientation">
          <tr>
            <td class="formParm1">
              <span resource="label.imgOrientation"></span>
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/orientation" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/thumbnailPath">
          <tr>
            <td class="formParm1">
              <span resource="label.thumbexists"></span>
              
              <xsl:if test="/cameraData/exifData/thumbnailWidth">
                <br/>&#160;
                (<xsl:value-of select="/cameraData/exifData/thumbnailWidth" />
                x
                <xsl:value-of select="/cameraData/exifData/thumbnailHeight" />)
              </xsl:if>
              <xsl:if test="/cameraData/exifData/thumbnailOrientation">
                <br/>&#160;
                <span resource="label.imgOrientation"></span>:
                <xsl:value-of select="/cameraData/exifData/thumbnailOrientation" />
              </xsl:if>
            </td>
            <td class="formParm2">
              <img border="0">
                <xsl:attribute name="src"><xsl:value-of select="/cameraData/exifData/thumbnailPath" /></xsl:attribute>
              </img>
            </td>
          </tr>
        </xsl:if>

      </xsl:if>

      <tr>
        <xsl:if test="/cameraData/exifData/orientation and (/cameraData/exifData/orientation != '1')">
          <td class="formButton" style="text-align:center">
            <input type="button" resource="resetExifOrientation">
              <xsl:attribute name="onclick">resetExifOrientation('<xsl:value-of select="/cameraData/exifData/imgPathForScript" />')</xsl:attribute>
            </input>
          </td>
        </xsl:if>
        <td class="formButton" style="text-align:center">
          <xsl:if test="not(/cameraData/exifData/orientation) or (/cameraData/exifData/orientation = '1')">
            <xsl:attribute name="colspan">2</xsl:attribute>
          </xsl:if>
          <input type="button" onclick="window.close()" resource="button.closewin"></input>
        </td>
      </tr>
    </table>
    
  </form>

</body>

<script type="text/javascript">
  setBundleResources();
</script>

</html>

</xsl:template>

</xsl:stylesheet>
