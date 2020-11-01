<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:template match="/">

<html>
  <head>

    <meta http-equiv="expires" content="0" />

    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
    <link rel="stylesheet" type="text/css">
      <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/editParams/css" />.css</xsl:attribute>
    </link>

    <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/videoAudio.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>

    <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/editParams/language" /></xsl:attribute>
    </script>
    
    <script type="text/javascript">
      var videoDuration = <xsl:value-of select="/editParams/videoInfo/durationSeconds" />;
    </script>
  </head>

  <body class="editVideo">

    <div class="headline" resource="titleTextOnVideo" />
    
    <br/>

    <form accept-charset="utf-8" name="fadeAudioForm" method="post" action="/webfilesys/servlet">
      <input type="hidden" name="command" value="video" />
      <input type="hidden" name="cmd" value="fadeAudio" />
      
      <input type="hidden" id="videoFileName" name="videoFileName">
        <xsl:attribute name="value"><xsl:value-of select="/editParams/videoFileName" /></xsl:attribute>
      </input>

      <table class="dataForm" border="0" width="100%">
        <tr>
          <td valign="top">

            <table border="0">
              <tr>
                <td class="formParm1"><span resource="label.videoFile"></span>:</td>
                <td class="formParm2">
                  <xsl:value-of select="/editParams/shortVideoFileName" />
                </td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="label.videoResolution"></span>:</td>
                <td class="formParm2">
                  <xsl:value-of select="/editParams/videoInfo/xpix" /> x <xsl:value-of select="/editParams/videoInfo/ypix" /> pix
                </td>
              </tr>
                
              <tr>
                <td class="formParm1"><span resource="label.fps"></span>:</td>
                <td class="formParm2">
                  <xsl:value-of select="/editParams/videoInfo/fps" /> fps
                </td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="label.duration"></span>:</td>
                <td class="formParm2">
                  <xsl:value-of select="/editParams/videoInfo/duration" /> hh:mm:ss
                </td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="label.codec"></span>:</td>
                <td class="formParm2">
                  <xsl:value-of select="/editParams/videoInfo/codec" />
                </td>
              </tr>
                
              <tr>
                <td colspan="2" valign="top" style="padding:6px 8px">
                  <img id="videoThumb" class="thumb">
                    <xsl:attribute name="src"><xsl:value-of select="/editParams/thumbnailSource" /></xsl:attribute>
                    <xsl:attribute name="width"><xsl:value-of select="/editParams/thumbnailWidth" /></xsl:attribute>
                    <xsl:attribute name="height"><xsl:value-of select="/editParams/thumbnailHeight" /></xsl:attribute>
                  </img>
                </td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="fadeInDuration"></span>:</td>
                <td class="formParm2">
                  <input type="text" name="fadeInDuration" style="width:40px"></input>
                  sec
                </td>
              </tr>
                
              <tr>
                <td class="formParm1"><span resource="fadeOutDuration"></span>:</td>
                <td class="formParm2">
                  <input type="text" name="fadeOutDuration" style="width:40px"></input>
                  sec
                </td>
              </tr>
                
              <tr><td colspan="2">&#160;</td></tr>  
                
              <tr>
                <td class="formParm1">
                  <input type="button" class="formButton" resource="button.start" onclick="sendFadeAudioForm()" />
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
  
  </body>
  
  <script type="text/javascript">
    setBundleResources();
  </script>
  
</html>

</xsl:template>

</xsl:stylesheet>

