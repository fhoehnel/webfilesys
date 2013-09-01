<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead">
  <xsl:value-of select="/renameFile/resources/msg[@key='label.renameImage']/@value" />
</div>
    
<form accept-charset="utf-8" name="renameForm" id="renameForm" method="get" action="/webfilesys/servlet" style="display:inline;">
  <input type="hidden" name="command" value="renameImage" />
  <input type="hidden" name="imagePath">
    <xsl:attribute name="value"><xsl:value-of select="/renameFile/imagePath" /></xsl:attribute>
  </input>
  
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/renameFile/resources/msg[@key='label.oldName']/@value" />:
      </td>
      <td class="formParm2" width="70%">
        <xsl:value-of select="/renameFile/shortFileName" />
      </td>
    </tr>
    
    <tr>
      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/renameFile/resources/msg[@key='label.newname']/@value" />:
      </td>
      <td class="formParm2" width="80%">
        <input type="text" name="newFileName" maxlength="256" style="width:100%;">
          <xsl:attribute name="value"><xsl:value-of select="/renameFile/shortFileName" /></xsl:attribute>
        </input>
      </td>
    </tr>

    <tr>
      <td colspan="2">
        <a class="button" onclick="this.blur();"> 
          <xsl:attribute name="href">javascript:validateNewFileName('<xsl:value-of select="/renameFile/oldFileName" />', '<xsl:value-of select="/renameFile/resources/msg[@key='alert.destEqualsSource']/@value" />', '<xsl:value-of select="/renameFile/resources/msg[@key='alert.illegalCharInFilename']/@value" />')</xsl:attribute>
          <span><xsl:value-of select="/renameFile/resources/msg[@key='button.rename']/@value" /></span>
        </a>              

        <a class="button" onclick="this.blur();" style="float:right"> 
          <xsl:attribute name="href">javascript:hidePrompt()</xsl:attribute>
          <span><xsl:value-of select="/renameFile/resources/msg[@key='button.cancel']/@value" /></span>
        </a>              
      </td>
    </tr>

  </table>
  
</form>

</xsl:template>

</xsl:stylesheet>
