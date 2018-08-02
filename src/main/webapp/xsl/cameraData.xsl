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

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/cameraData/css" />.css</xsl:attribute>
</link>

<title>
  <xsl:value-of select="/cameraData/resources/msg[@key='alt.cameradata']/@value" />:
  <xsl:value-of select="/cameraData/shortImgName" />
</title>

</head>

<body class="cameraData">

  <div class="headline">
    <xsl:value-of select="/cameraData/resources/msg[@key='alt.cameradata']/@value" />
  </div>

  <form accept-charset="utf-8" name="form1" style="padding-top:5px;">

    <table class="dataForm" width="100%">
   
      <tr>
        <td colspan="2" class="formParm1">
          <xsl:value-of select="/cameraData/resources/msg[@key='label.picturefile']/@value" />
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm2" style="padding-left:16px">
          <xsl:value-of select="/cameraData/shortImgName" />
        </td>
      </tr>

      <xsl:if test="not(/cameraData/exifData)">
        <script type="text/javascript">
          alert('<xsl:value-of select="/cameraData/resources/msg[@key='alert.nocameradata']/@value" />');
          self.close();
        </script>
      </xsl:if>

      <xsl:if test="/cameraData/exifData">

        <xsl:if test="/cameraData/exifData/manufacturer">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.manufacturer']/@value" />
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/manufacturer" />
            </td>
          </tr>
        </xsl:if>
        
        <xsl:if test="/cameraData/exifData/cameraModel">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.cameramodel']/@value" />
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/cameraModel" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/exposureDate">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.exposuredate']/@value" />
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/exposureDate" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/exposureTime">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.exposuretime']/@value" />
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/exposureTime" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/aperture">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.aperture']/@value" />
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/aperture" />
            </td>
          </tr>
        </xsl:if>
        
        <xsl:if test="/cameraData/exifData/isoValue">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.isoValue']/@value" />
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/isoValue" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/flashFired">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.flashfired']/@value" />
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/flashFired" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/exposureBias">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.exposureBias']/@value" />
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/exposureBias" />
              <xsl:text> </xsl:text>
              <xsl:value-of select="/cameraData/resources/msg[@key='apertureStops']/@value" />              
            </td>
          </tr>
        </xsl:if>
        
        <xsl:if test="/cameraData/exifData/gpsLatitude">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.gpsLatitude']/@value" />
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/gpsLatitude" />
            </td>
          </tr>
        </xsl:if>
        
        <xsl:if test="/cameraData/exifData/gpsLongitude">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.gpsLongitude']/@value" />
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/gpsLongitude" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/imgWidth">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.imgwidth']/@value" />
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/imgWidth" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/imgHeight">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.imgheight']/@value" />
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/imgHeight" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/orientation">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.imgOrientation']/@value" />
            </td>
            <td class="formParm2">
              <xsl:value-of select="/cameraData/exifData/orientation" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="/cameraData/exifData/thumbnailPath">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="/cameraData/resources/msg[@key='label.thumbexists']/@value" />
              
              <xsl:if test="/cameraData/exifData/thumbnailWidth">
                <br/>&#160;
                (<xsl:value-of select="/cameraData/exifData/thumbnailWidth" />
                x
                <xsl:value-of select="/cameraData/exifData/thumbnailHeight" />)
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
        <td class="formButton" colspan="2" style="text-align:center">
          <input type="button" onclick="window.close()">
            <xsl:attribute name="value"><xsl:value-of select="/cameraData/resources/msg[@key='button.closewin']/@value" /></xsl:attribute>
          </input>
        </td>
      </tr>
    </table>
    
  </form>

</body>

</html>

</xsl:template>

</xsl:stylesheet>
