<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead" resource="label.renameFolderHead"></div>
    
<form accept-charset="utf-8" name="mkdirForm" id="mkdirForm" method="get" action="/webfilesys/servlet" style="display:inline;">
  <input type="hidden" name="command" value="renameDir" />
  <input type="hidden" name="path">
    <xsl:attribute name="value"><xsl:value-of select="/renameFolder/currentPath" /></xsl:attribute>
  </input>
  
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
	    <span resource="label.currentName" />:
      </td>
    </tr>
    <tr>      
      <td class="formParm2">
        <xsl:value-of select="/renameFolder/currentNameShort" />
      </td>
    </tr>
    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
	    <span resource="label.newDirName" />:
      </td>
    </tr>
    <tr>
      <td class="formParm2">
        <input type="text" name="NewDirName" maxlength="256" style="width:100%;" >
          <xsl:attribute name="value"><xsl:value-of select="/renameFolder/currentName" /></xsl:attribute>
        </input>
      </td>
    </tr>

    <tr>
      <td>
        <input type="button" resource="button.rename" style="float:left">
          <xsl:attribute name="onclick">javascript:validateNewFolderName(resourceBundle["alert.illegalCharInFilename"])</xsl:attribute>
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
