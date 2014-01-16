<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead" resource="label.renameFile"></div>
    
<form accept-charset="utf-8" name="renameForm" id="renameForm" method="get" action="/webfilesys/servlet" style="display:inline;">
  <input type="hidden" name="command" value="renameFile" />
  <input type="hidden" name="fileName">
    <xsl:attribute name="value"><xsl:value-of select="/renameFile/oldFileName" /></xsl:attribute>
  </input>
  
  <xsl:if test="/renameFile/mobile">
    <input type="hidden" name="mobile" value="true" />
  </xsl:if>
  
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td class="formParm1" nowrap="nowrap">
	    <span resource="label.oldName" />:
      </td>
      <td class="formParm2" width="70%">
        <xsl:value-of select="/renameFile/shortFileName" />
      </td>
    </tr>
    <tr>
      <td class="formParm1" nowrap="nowrap">
	    <span resource="label.newname" />:
      </td>
      <td class="formParm2" width="80%">
        <input type="text" name="newFileName" maxlength="256" style="width:220px;">
          <xsl:attribute name="value"><xsl:value-of select="/renameFile/oldFileName" /></xsl:attribute>
        </input>
      </td>
    </tr>

    <tr>
      <td colspan="2">
	  
        <input type="button" resource="button.rename" style="float:left">
          <xsl:attribute name="onclick">javascript:validateNewFileName('<xsl:value-of select="/renameFile/oldFileName" />', '<xsl:value-of select="/renameFile/resources/msg[@key='alert.destEqualsSource']/@value" />', '<xsl:value-of select="/renameFile/resources/msg[@key='alert.illegalCharInFilename']/@value" />')</xsl:attribute>
        </input> 
	  
        <input type="button" resource="button.cancel" style="float:right">
          <xsl:attribute name="onclick">javascript:hidePrompt()</xsl:attribute>
        </input> 

	  </td>
    </tr>

  </table>
  
</form>

</xsl:template>

</xsl:stylesheet>
