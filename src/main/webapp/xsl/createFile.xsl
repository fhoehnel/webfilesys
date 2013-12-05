<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead" resource="label.createfile"></div>
    
<form accept-charset="utf-8" name="mkfileForm" id="mkfileForm" method="get" action="/webfilesys/servlet" style="display:inline;">
  <input type="hidden" name="command" value="mkfile" />
  <input type="hidden" name="actpath">
    <xsl:attribute name="value"><xsl:value-of select="/createFile/baseFolder" /></xsl:attribute>
  </input>
  
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
	    <span resource="label.directory" />:
      </td>
    </tr>
    <tr>
      <td class="formParm2">
        <xsl:value-of select="/createFile/baseFolderShort" />
      </td>
    </tr>
    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
	    <span resource="label.newFileName" />:
      </td>
    </tr>
    <tr>
      <td class="formParm2">
        <input type="text" name="NewFileName" maxlength="256" value="new-file.txt" style="width:100%;" />
      </td>
    </tr>

    <tr>
      <td>
	  
        <input type="button" resource="button.create" style="float:left">
          <xsl:attribute name="onclick">javascript:validateCreateFileName('<xsl:value-of select="/createFile/resources/msg[@key='alert.illegalCharInFilename']/@value" />')</xsl:attribute>
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
