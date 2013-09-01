<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead">
  <xsl:value-of select="/createFolder/resources/msg[@key='label.mkdir']/@value" />
</div>
    
<form accept-charset="utf-8" name="mkdirForm" id="mkdirForm" method="get" action="/webfilesys/servlet" style="display:inline;">
  <input type="hidden" name="command" value="mkdir" />
  <input type="hidden" name="actpath">
    <xsl:attribute name="value"><xsl:value-of select="/createFolder/baseFolder" /></xsl:attribute>
  </input>
  
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
        <xsl:value-of select="/createFolder/resources/msg[@key='label.parentDir']/@value" />:
      </td>
    </tr>
    <tr>      
      <td class="formParm2">
        <xsl:value-of select="/createFolder/baseFolderShort" />
      </td>
    </tr>
    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
        <xsl:value-of select="/createFolder/resources/msg[@key='label.newDirName']/@value" />:
      </td>
    </tr>
    <tr>
      <td class="formParm2">
        <input type="text" name="NewDirName" maxlength="256" value="new-dir" style="width:100%;" />
      </td>
    </tr>

    <tr>
      <td>
        <a class="button" onclick="this.blur();"> 
          <xsl:attribute name="href">javascript:validateNewFolderName('<xsl:value-of select="/createFolder/resources/msg[@key='alert.illegalCharInFilename']/@value" />')</xsl:attribute>
          <span><xsl:value-of select="/createFolder/resources/msg[@key='button.create']/@value" /></span>
        </a>              

        <a class="button" onclick="this.blur();" style="float:right"> 
          <xsl:attribute name="href">javascript:hidePrompt()</xsl:attribute>
          <span><xsl:value-of select="/createFolder/resources/msg[@key='button.cancel']/@value" /></span>
        </a>              
      </td>
    </tr>

  </table>
  
</form>

</xsl:template>

</xsl:stylesheet>
