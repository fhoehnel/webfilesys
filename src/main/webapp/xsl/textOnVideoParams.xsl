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
  </head>

  <body class="editVideo">

    <div class="headline" resource="titleTextOnVideo" />
    
    <br/>

    <form accept-charset="utf-8" name="textOnVideoForm" method="post" action="/webfilesys/servlet">
      <input type="hidden" name="command" value="video" />
      <input type="hidden" name="cmd" value="textOnVideo" />
      
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
                <td class="formParm1"><span resource="videoText"></span>:</td>
                <td class="formParm2">
                  <textarea name="videoText" style="width:200px;height:60px;"></textarea>
                </td>
              </tr>
                
              <tr>
                <td class="formParm1"><span resource="videoTextSize"></span>:</td>
                <td class="formParm2">
                  <select name="videoTextSize" size="1" style="width:60px;">
                    <option value="12">12</option>
                    <option value="16">16</option>
                    <option value="20">20</option>
                    <option value="30">30</option>
                    <option value="40">40</option>
                    <option value="50">50</option>
                    <option value="60">60</option>
                    <option value="80" selected="selected">80</option>
                    <option value="100">100</option>
                  </select>
                </td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="videoTextColor"></span>:</td>
                <td class="formParm2">
                  <input type="text" name="videoTextColor" style="width:80px" value="#ffffff" />
                  &#160;
                  <select name="videoTextColorName" size="1" style="width:130px;">
                    <option value="" resource="videoTextColorByName"></option>
                    <option value="white">white</option>
                    <option value="yellow">yellow</option>
                    <option value="orange">orange</option>
                    <option value="red">red</option>
                    <option value="blue">blue</option>
                    <option value="green">green</option>
                    <option value="brown">brown</option>
                    <option value="magenta">magenta</option>
                    <option value="cyan">cyan</option>
                    <option value="black">black</option>
                  </select>
                </td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="videoTextPosition"></span>:</td>
                <td class="formParm2">
                  <select name="videoTextPosition" size="1" style="width:100px;">
                    <option value="top" resource="videoTextPositionTop" />
                    <option value="center" resource="videoTextPositionCenter" selected="selected" />
                    <option value="bottom" resource="videoTextPositionBottom" />
                  </select>
                </td>
              </tr>
                
              <tr><td colspan="2">&#160;</td></tr>  
                
              <tr>
                <td class="formParm1">
                  <input type="button" class="formButton" resource="button.start" onclick="sendTextOnVideoForm()" />
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

