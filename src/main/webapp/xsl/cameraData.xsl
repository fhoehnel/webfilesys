<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="cameraData" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/cameraData/css" />.css</xsl:attribute>
</link>

<title>
  <xsl:value-of select="/cameraData/resources/msg[@key='alt.cameradata']/@value" />:
  <xsl:value-of select="/cameraData/shortImgName" />
</title>

</head>

<body>

  <table border="0" width="100%" cellpadding="2" cellspacing="0">
    <tr>
      <th class="headline">
        <xsl:value-of select="/cameraData/resources/msg[@key='alt.cameradata']/@value" />
      </th>
    </tr>
  </table>

  <form accept-charset="utf-8" name="form1" style="padding-top:5px;">

    <table class="dataForm" width="100%">
   
      <tr>
        <td colspan="2" class="formParm1">
          <xsl:value-of select="/cameraData/resources/msg[@key='label.picturefile']/@value" />
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm2">
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
              <xsl:if test="(/cameraData/exifData/orientation = '1') or (/cameraData/exifData/orientation = '3')">
                <xsl:value-of select="/cameraData/resources/msg[@key='orientation.landscape']/@value" />
              </xsl:if>
              <xsl:if test="(/cameraData/exifData/orientation = '6') or (/cameraData/exifData/orientation = '8')">
                <xsl:value-of select="/cameraData/resources/msg[@key='orientation.portrait']/@value" />
              </xsl:if>
              (<xsl:value-of select="/cameraData/exifData/orientation" />)
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
        <td class="formButton" nowrap="nowrap">
          <a class="button" onclick="this.blur();" style="float:right"> 
            <xsl:attribute name="href">javascript:window.close()</xsl:attribute>
            <span><xsl:value-of select="/cameraData/resources/msg[@key='button.closewin']/@value" /></span>
          </a>              
        </td>
        <td>&#160;</td>
      </tr>
    </table>
    
  </form>

</body>

</html>

</xsl:template>

</xsl:stylesheet>
