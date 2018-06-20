<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead" resource="label.renameImage"></div>
    
<form accept-charset="utf-8" name="renameForm" id="renameForm" method="get" action="/webfilesys/servlet" style="display:inline;">
  <input type="hidden" name="command" value="renamePicture" />
  <input type="hidden" name="imageFile">
    <xsl:attribute name="value"><xsl:value-of select="/renameFile/oldFileName" /></xsl:attribute>
  </input>
  <input type="hidden" name="domId" value="" />
  
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td class="formParm1" nowrap="nowrap">
        <span resource="label.oldName"></span>
      </td>
      <td class="formParm2" width="70%">
        <xsl:value-of select="/renameFile/shortFileName" />
      </td>
    </tr>
    
    <tr>
      <td class="formParm1" nowrap="nowrap">
        <span resource="label.newname"></span>
      </td>
      <td class="formParm2" width="80%">
        <input type="text" name="newFileName" maxlength="256" style="width:100%;">
          <xsl:attribute name="value"><xsl:value-of select="/renameFile/oldFileName" /></xsl:attribute>
          <xsl:attribute name="onkeypress">return handleRenameKeyPress(event)</xsl:attribute>
        </input>
      </td>
    </tr>

    <tr>
      <td colspan="2">
        <a class="button" onclick="this.blur();"> 
          <xsl:attribute name="href">javascript:validateNewFileNameAndRename('<xsl:value-of select="/renameFile/oldFileNameForScript" />', resourceBundle["alert.destEqualsSource"], resourceBundle["alert.illegalCharInFilename"])</xsl:attribute>
          <span resource="button.rename"></span>
        </a>              

        <a class="button" onclick="this.blur();" style="float:right"> 
          <xsl:attribute name="href">javascript:hidePrompt()</xsl:attribute>
          <span resource="button.cancel"></span>
        </a>              
      </td>
    </tr>

  </table>
  
</form>

</xsl:template>

</xsl:stylesheet>
