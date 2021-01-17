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
      <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/concatParams/css" />.css</xsl:attribute>
    </link>

    <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/videoAudio.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>

    <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/concatParams/language" /></xsl:attribute>
    </script>
    
  </head>

  <body class="editVideo">

    <div class="headline" resource="titleConcatVideoParams" />
    
    <br/>

    <xsl:if test="/concatParams/missingAudio">
      <span resource="videoConcatErrorNoAudio"></span>:
      <ul>
        <xsl:for-each select="/concatParams/missingAudio/file">
          <li>
            <xsl:value-of select="."/>
          </li>
        </xsl:for-each>
      </ul>
      
      <input type="button" class="formButton" name="cancel" resource="button.cancel">
        <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet?command=listVideos'</xsl:attribute>
      </input>
    </xsl:if>

    <xsl:if test="not(/concatParams/missingAudio)">

    <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet">
      <input type="hidden" name="command" value="video" />
      <input type="hidden" name="cmd" value="multiVideoJoin" />

      <table class="dataForm" border="0" width="100%">
        <tr>
          <td valign="top">

            <table border="0">
              <tr>
                <td class="formParm1"><span resource="maxVideoWidth"></span>:</td>
                <td class="formParm2">
                  <xsl:value-of select="/concatParams/maxVideoWidth" /> pix
                </td>
              </tr>
                
              <tr>
                <td class="formParm1"><span resource="maxVideoHeight"></span>:</td>
                <td class="formParm2">
                  <xsl:value-of select="/concatParams/maxVideoHeight" /> pix
                </td>
              </tr>
                
              <tr>
                <td class="formParm1"><span resource="label.newResolution"></span>:</td>
                <td class="formParm2">
                  <select name="newWidth" size="1" style="width:140px;">
                    <xsl:for-each select="/concatParams/targetWidth/option">
                      <option>
                        <xsl:attribute name="value"><xsl:value-of select="." /></xsl:attribute>
                        <xsl:value-of select="." />
                      </option>
                    </xsl:for-each>
                  </select>
                  &#160;
                  <select name="newHeight" size="1" style="width:140px;">
                    <xsl:for-each select="/concatParams/targetHeight/option">
                      <option>
                        <xsl:attribute name="value"><xsl:value-of select="." /></xsl:attribute>
                        <xsl:value-of select="." />
                      </option>
                    </xsl:for-each>
                  </select>
                </td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="label.newFps"></span>:</td>
                <td class="formParm2">
                  <select name="newFps" size="1" style="width:140px;">
                    <option value="24">24</option>
                    <option value="25">25</option>
                    <option value="30">30</option>
                    <option value="50">50</option>
                    <option value="60">60</option>
                  </select>
                </td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="label.newVideoContainerFormat"></span>:</td>
                <td class="formParm2">
                  <select name="newContainer" size="1" style="width:140px;">
                    <option value="mp4">MP4</option>
                    <option value="mkv">MKV</option>
                  </select>
                </td>
              </tr>
                
              <tr><td colspan="2">&#160;</td></tr>  
                
              <tr>
                <td class="formParm1">
                  <input type="button" class="formButton" resource="button.start" onclick="sendConcatForm()" />
                </td>
                <td class="formParm2" style="text-align:right">
                  <input type="button" class="formButton" name="cancel" resource="button.cancel">
                    <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet?command=listVideos'</xsl:attribute>
                  </input>
                </td>
              </tr>
                
            </table>

          </td>
        </tr>
      </table>
      
    </form>

    </xsl:if>
  
  </body>
  
  <script type="text/javascript">
    setBundleResources();
  </script>
  
</html>

</xsl:template>

</xsl:stylesheet>

