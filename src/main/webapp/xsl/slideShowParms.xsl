<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="slideShowParms" />

<xsl:template match="/slideShowParms">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/slideShowParms/css" />.css</xsl:attribute>
  </link>

  <title resource="label.slideparmhead"></title>

  <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/slideShowParms/language" /></xsl:attribute>
  </script>
  
  <script type="text/javascript">
    function startShow() {
        if (document.form1.extraWin.checked) {
            var slideshowURL = '/webfilesys/servlet?command=slideShow&amp;imageIdx=0&amp;delay=' + 
                               document.form1.delay.options[document.form1.delay.selectedIndex].value + 
                               '&amp;recurse=' + document.form1.recurse.checked + 
                               '&amp;autoForward=' + document.form1.autoForward.checked +
                               '&amp;fadeInOut=' + document.form1.fadeInOut.checked +
                               '&amp;randomize=' + document.form1.randomize.checked;

            <xsl:if test="startPath">
              slideshowURL = slideshowURL + '&amp;startFilePath=' + encodeURIComponent('<xsl:value-of select="encodedStartPath"/>');
            </xsl:if>
     
            var showWin = window.open(slideshowURL,'thumbwin','status=no,toolbar=no,menu=no,width=' + (screen.availWidth-10) + ',height=' + (screen.availHeight-50) + ',resizable=yes,scrollbars=yes,left=0,top=0,screenX=0,screenY=0');
            
            showWin.focus();

            window.location.href = '/webfilesys/servlet?command=listFiles';
        } else {
            var windowWidth;
            var windowHeigth;

            if (browserMSIE) {  
                windowWidth = document.body.clientWidth;
                windowHeight = document.body.clientHeight;
            } else {  
                windowWidth = self.innerWidth;
                windowHeight = self.innerHeight;
            }

            document.form1.windowWidth.value = windowWidth;
            document.form1.windowHeight.value = windowHeight;
            
            document.form1.submit();            
        }
    }
    
    function setCrossfade(fullscreenCheckbox) {
        crossfadeCheckbox = document.getElementById('crossfade');
				
	    if (crossfadeCheckbox) {
	        if (fullscreenCheckbox.checked) {
		        crossfadeCheckbox.disabled = false;
            } else {
		        crossfadeCheckbox.checked = false;
		        crossfadeCheckbox.disabled = true;
            }
        }
    }
    
</script>

</head>

<body>

  <div class="headline" resource="label.slideparmhead"></div>

  <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet">
  
    <input type="hidden" name="command" value="slideShowInFrame" />
    <input type="hidden" name="imageIdx" value="" />
    <input type="hidden" name="windowWidth" value="" />
    <input type="hidden" name="windowHeight" value="" />
    
    <xsl:if test="startPath">
      <input type="hidden" name="startFilePath">
        <xsl:attribute name="value"><xsl:value-of select="startPath"/></xsl:attribute>
      </input>
    </xsl:if>

    <table class="dataForm" width="100%">
      
      <tr>
        <td colspan="2" class="formParm1">
          <span resource="label.directory"></span>
        </td>
      </tr>
     
      <tr>
        <td colspan="2" class="formParm2">
          <xsl:value-of select="shortPath" />
        </td>
      </tr>
     
      <xsl:if test="startFile">
        <tr>
          <td colspan="2" class="formParm1">
            <span resource="label.startPic"></span>
          </td>
        </tr>
        <tr>
          <td colspan="2" class="formParm2">
            <xsl:value-of select="startFile"/>
          </td>
        </tr>
      </xsl:if>

      <tr>
        <td colspan="2">&#160;</td>
      </tr>
       
      <tr>
        <td colspan="2" class="formParm1">
          <input type="checkbox" class="cb3" name="recurse" />
          &#160;
          <span resource="label.recurse"></span>
        </td>
      </tr>
     
      <tr>
        <td colspan="2" class="formParm1">
          <input type="checkbox" class="cb3" name="autoForward" checked="checked" />
          &#160;
          <span resource="label.autoForward"></span>
        </td>
      </tr>
     
      <tr>
        <td colspan="2" class="formParm1">
          <span style="padding-left:25px;">&#160;</span>
          <span resource="label.delay"></span>
          &#160;

          <select name="delay" size="1">
            <option value="1">1</option>
            <option value="3">3</option>
            <option value="5" selected="selected">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
            <option value="30">30</option>
            <option value="60">60</option>
            <option value="120">120</option>
            <option value="300">300</option>
          </select>
        </td>
      </tr>
     
      <tr>
        <td colspan="2" class="formParm1">
          <input type="checkbox" class="cb3" name="extraWin" onclick="setCrossfade(this);" />
          &#160;
          <span resource="label.fullScreen"></span>
        </td>
      </tr>
     
      <tr>
        <td colspan="2" class="formParm1">
          <input id="crossfade" type="checkbox" class="cb3" name="fadeInOut" disabled="true" />
          &#160;
          <span resource="label.crossfade"></span>
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm1">
          <input id="randomize" type="checkbox" class="cb3" name="randomize" />
          &#160;
          <span resource="label.randomize"></span>
        </td>
      </tr>
     
      <tr>
        <td colspan="2">&#160;</td>
      </tr>

      <tr>
        <td class="formButton">
          <input type="button" name="start" onclick="javascript:startShow();" resource="button.startshow"></input>
        </td>
       
        <td class="formButton" align="right">
          <input type="button" name="cancel" onclick="window.location.href = '/webfilesys/servlet?command=listFiles';" resource="button.cancel"></input>
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
