<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead" resource="label.rotateFreeAngle"></div>
    
<form accept-charset="utf-8" name="rotateForm" id="rotateForm" method="post" action="/webfilesys/servlet" style="display:inline;">
  <input type="hidden" name="command" value="transformImage" />
  <input type="hidden" name="action" value="rotate" />
  <input type="hidden" name="imgName">
    <xsl:attribute name="value"><xsl:value-of select="/rotateImage/imagePath" /></xsl:attribute>
  </input>

  <p style="margin:10px">
    <span resource="rotationHelp" class="plaintext"></span>
  </p>
  
  <table border="0" width="100%" cellpadding="10">

    <tr>
      <td class="formParm1" nowrap="nowrap">
	    <span resource="rotationAngle" />:
      </td>
      <td class="formParm2" width="80%">
        <input type="text" id="rotationDegrees" name="degrees" maxlength="4" style="width:60px;" />
      </td>
    </tr>

    <tr>
      <td colspan="2">
	  
        <input type="button" resource="button.rotate" style="float:left">
          <xsl:attribute name="onclick">validateRotationDegrees()</xsl:attribute>
        </input> 
	  
        <input type="button" resource="button.cancel" style="float:right">
          <xsl:attribute name="onclick">hidePrompt()</xsl:attribute>
        </input> 

	  </td>
    </tr>

  </table>
  
</form>

</xsl:template>

</xsl:stylesheet>
