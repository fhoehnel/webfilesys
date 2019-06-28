<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<!-- root node-->
<xsl:template match="/">

<html>
  <head>

    <meta http-equiv="expires" content="0" />

    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
    <link rel="stylesheet" type="text/css">
      <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/videoParams/css" />.css</xsl:attribute>
    </link>

    <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/thumbnail.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>

    <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/videoParams/language" /></xsl:attribute>
    </script>
    
  </head>

  <body class="editVideo">
    <div class="headline" resource="titleSlideshowVideo" />
    
    <br/>

    <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet">
      <input type="hidden" name="command" value="video" />
      <input type="hidden" name="cmd" value="multiImgToVideo" />

      <table class="dataForm" border="0" width="100%">
                
          <tr>
            <td class="formParm1"><span resource="slideshowVideoPicCount"></span>:</td>
            <td class="formParm2"><xsl:value-of select="/videoParams/selectedPictureCount"/></td>
          </tr>
              
          <xsl:if test="not(/videoParams/picDimensionMissmatch)">
              
              <tr>
                <td class="formParm1"><span resource="slideshowVideoPicDimensions"></span>:</td>
                <td class="formParm2"><xsl:value-of select="/videoParams/pictureWidth" /> x <xsl:value-of select="/videoParams/pictureHeight" /> pix</td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="slideshowVideoResolution"></span>:</td>
                <td class="formParm2">
                  <select name="videoSize" size="1" style="width:240px;">
                    <option value="" selected="selected" resource="videoResolutionFromPic">
                      <xsl:attribute name="value"><xsl:value-of select="/videoParams/pictureWidth" />x<xsl:value-of select="/videoParams/pictureHeight" /></xsl:attribute>
                    </option>
                    <xsl:for-each select="/videoParams/targetResolution/option">
                      <option>
                        <xsl:attribute name="value"><xsl:value-of select="width" />x<xsl:value-of select="height" /></xsl:attribute>
                        <xsl:value-of select="width" /> x <xsl:value-of select="height" />
                      </option>
                    </xsl:for-each>
                  </select>
                </td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="slideshowVideoDelay"></span>:</td>
                <td class="formParm2">
                  <select name="delay" size="1" style="width:140px;">
                    <option value="1">1</option>
                    <option value="2">2</option>
                    <option value="3">3</option>
                    <option value="5" selected="selected">5</option>
                    <option value="10">10</option>
                    <option value="20">20</option>
                  </select>
                </td>
              </tr>
         
              <tr><td colspan="2">&#160;</td></tr>  
                
              <tr>
                <td class="formParm1">
                  <input type="button" class="formButton" resource="button.start" onclick="sendSlideshowVideoParams()" />
                </td>
                <td class="formParm2" style="text-align:right">
                  <input type="button" class="formButton" name="cancel" resource="button.cancel">
                    <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet?command=listFiles'</xsl:attribute>
                  </input>
                </td>
              </tr>
              
          </xsl:if>    
                
      </table>

    </form>
  
  </body>
  
  <script type="text/javascript">
    setBundleResources();
    
    <xsl:if test="/videoParams/picDimensionMissmatch">
        customAlert(resourceBundle['slideshowVideoErrorMissmatch'], resourceBundle['button.ok'], function() {
            window.location.href = '/webfilesys/servlet?command=listFiles';
        });
    </xsl:if>
    
  </script>
  
</html>

</xsl:template>

</xsl:stylesheet>

