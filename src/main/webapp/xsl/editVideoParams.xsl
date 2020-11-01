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
        var durationSeconds = parseInt("<xsl:value-of select="/editParams/videoInfo/durationSeconds" />");
        
        var oldCodec = '<xsl:value-of select="/editParams/videoInfo/codec" />';
    </script>    

  </head>

  <body class="editVideo">

    <xsl:if test="not(/editParams/videoInfo/duration)">
      <xsl:attribute name="onload">initialSetReencodeCheckboxState('<xsl:value-of select="/editParams/videoInfo/codec" />')</xsl:attribute>
    </xsl:if>
    <xsl:if test="/editParams/videoInfo/duration">
      <xsl:attribute name="onload">createVideoTimeRangeSelOptions();initialSetReencodeCheckboxState('<xsl:value-of select="/editParams/videoInfo/codec" />')</xsl:attribute>
    </xsl:if>
  
    <div class="headline" resource="titleEditVideo" />
    
    <br/>

    <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet">
      <input type="hidden" name="command" value="video" />
      <input type="hidden" name="cmd" value="editConvertVideo" />
      
      <input type="hidden" id="videoFileName" name="videoFileName">
        <xsl:attribute name="value"><xsl:value-of select="/editParams/videoFileName" /></xsl:attribute>
      </input>
      <input type="hidden" id="videoWidth" name="videoWidth">
        <xsl:attribute name="value"><xsl:value-of select="/editParams/videoInfo/xpix" /></xsl:attribute>
      </input>
      <input type="hidden" id="videoHeight" name="videoHeight">
        <xsl:attribute name="value"><xsl:value-of select="/editParams/videoInfo/ypix" /></xsl:attribute>
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
                <td class="formParm1"><span resource="label.newResolution"></span>:</td>
                <td class="formParm2">
                  <select name="newSize" size="1" style="width:140px;">
                    <option value="" selected="selected" resource="label.keepOrigSize"></option>
                    <xsl:for-each select="/editParams/targetResolution/option">
                      <option>
                        <xsl:attribute name="value"><xsl:value-of select="." /></xsl:attribute>
                        <xsl:value-of select="." />
                      </option>
                    </xsl:for-each>
                  </select>
                  &#160;
                  <select name="newHeight" size="1" style="width:260px;">
                    <option value="" selected="selected" resource="label.videoHeightAuto"></option>
                    <xsl:for-each select="/editParams/targetHeight/option">
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
                    <option value="" selected="selected" resource="keepOrigFrameRate"></option>
                    <option value="24">24</option>
                    <option value="25">25</option>
                    <option value="30">30</option>
                    <option value="50">50</option>
                    <option value="60">60</option>
                  </select>
                </td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="label.newCodec"></span>:</td>
                <td class="formParm2">
                  <select id="newCodec" name="newCodec" size="1" style="width:140px;" onchange="setReencodeCheckboxState()">
                    <option value="" selected="selected" resource="keepOrigCodec"></option>
                    <option value="h264">h264</option>
                    <option value="mpeg2video">mpeg2</option>
                  </select>
                  &#160;
                  <div id="re-encodeCont" style="display:none">
                    <input id="re-encode" name="re-encode" type="checkbox"/><label for="re-encode" resource="label.reencode"></label>
                  </div>
                </td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="label.newVideoContainerFormat"></span>:</td>
                <td class="formParm2">
                  <select name="newContainer" size="1" style="width:140px;">
                    <option value="" selected="selected" resource="keepOrigCodec"></option>
                    <option value="mp4">MP4</option>
                    <option value="mkv">MKV</option>
                  </select>
                </td>
              </tr>

              <xsl:if test="/editParams/videoInfo/duration">
                <tr>
                  <td class="formParm1"><span resource="label.timeRange"></span>:</td>
                  <td class="formParm2">
                    <select name="startHour" id="startHour" class="timeSel" /><b>:</b>&#160;
                    <select name="startMin" id="startMin" class="timeSel" /><b>:</b>&#160;
                    <select name="startSec" id="startSec" class="timeSel" />

                    &#160;<span style="font-size:20px;font-weight:bold">&#8211;</span>&#160;&#160;

                    <select name="endHour" id="endHour" class="timeSel" /><b>:</b>&#160; 
                    <select name="endMin" id="endMin" class="timeSel" /><b>:</b>&#160;
                    <select name="endSec" id="endSec" class="timeSel" />
                  </td>
                </tr>
                
                <tr>
                  <td></td>
                  <td style="text-align:center">
                    <input type="button" id="previewButton" resource="button.previewVideoExtract" onclick="videoEditStartPreview()" />
                  </td>
                </tr>
                
                <tr>
                  <td></td>
                  <td class="formParm2">
                    <img id="previewVideoStartFrame" class="thumb previewVideoFrame"/>
                    <img id="previewVideoEndFrame" class="thumb previewVideoFrame" style="float:right" />
                  </td>
                </tr>
                
              </xsl:if>
                
              <tr><td colspan="2">&#160;</td></tr>  
                
              <tr>
                <td class="formParm1">
                  <input type="button" class="formButton" resource="button.start" onclick="sendEditConvertForm()" />
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

