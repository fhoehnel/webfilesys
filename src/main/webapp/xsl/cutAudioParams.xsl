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
      <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/cutParams/css" />.css</xsl:attribute>
    </link>

    <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/videoAudio.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>

    <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/cutParams/language" /></xsl:attribute>
    </script>
    
    <script type="text/javascript">
        var durationSeconds = parseInt("<xsl:value-of select="/cutParams/audioInfo/durationSeconds" />");
    </script>    

  </head>

  <body class="editVideo">
    <xsl:if test="/cutParams/audioInfo/duration">
      <xsl:attribute name="onload">createVideoTimeRangeSelOptions()</xsl:attribute>
    </xsl:if>
  

    <div class="headline" resource="title.cutAudio" />
    
    <br/>

    <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet">
      <input type="hidden" name="command" value="cutAudio" />
      
      <input type="hidden" id="audioFileName" name="audioFileName">
        <xsl:attribute name="value"><xsl:value-of select="/cutParams/audioFileName" /></xsl:attribute>
      </input>

      <table class="dataForm" border="0" width="100%">
        <tr>
          <td valign="top">

            <table border="0">
              <tr>
                <td class="formParm1"><span resource="label.audioFile"></span>:</td>
                <td class="formParm2">
                  <xsl:value-of select="/cutParams/shortAudioFileName" />
                </td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="label.duration"></span>:</td>
                <td class="formParm2">
                  <xsl:value-of select="/cutParams/audioInfo/duration" /> hh:mm:ss
                </td>
              </tr>

              <xsl:if test="/cutParams/audioInfo/duration">
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
              </xsl:if>
                
              <tr><td colspan="2">&#160;</td></tr>  
                
              <tr>
                <td class="formParm1">
                  <input type="button" class="formButton" resource="button.start" onclick="sendCutAudioForm()" />
                </td>
                <td class="formParm2" style="text-align:right">
                  <input type="button" class="formButton" name="cancel" resource="button.cancel">
                    <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet?command=listFiles'</xsl:attribute>
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

