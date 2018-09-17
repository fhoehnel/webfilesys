<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead">
  <xsl:value-of select="/result/folderShortPath" />
</div>
    
<form>
  
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td colspan="2" class="formParm1">
      </td>
    </tr>

    <tr>
      <td colspan="2" class="formParm1">
        <xsl:value-of select="/result/deletePromptMsg" />
      </td>
    </tr>
    
    <xsl:if test="/result/error">
      <tr>
        <td colspan="2">
          <input type="button" resource="button.return" style="float:right">
            <xsl:attribute name="onclick">hidePrompt()</xsl:attribute>
          </input>
        </td>
      </tr>
    </xsl:if>
    
    <xsl:if test="not(/result/error)">
      <tr>
        <td align="left">
          <input type="button" resource="button.delete">
            <xsl:attribute name="onclick">hidePrompt();removeDir('<xsl:value-of select="/result/folderPath" />')</xsl:attribute>
          </input>
        </td>

        <td align="right">
          <input type="button" resource="button.cancel">
            <xsl:attribute name="onclick">hidePrompt()</xsl:attribute>
          </input>
        </td>
      </tr>
    </xsl:if>

  </table>
  
</form>

</xsl:template>

</xsl:stylesheet>
