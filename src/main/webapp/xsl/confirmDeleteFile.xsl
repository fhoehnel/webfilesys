<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

  <div class="promptHead">
    <xsl:value-of select="/result/headlinePath" />
  </div>
    
  <table border="0" width="100%" cellpadding="10" style="margin-top:20px">
  
    <tr>
      <td colspan="2" class="formParm1">
        <xsl:value-of select="/result/deletePromptMsg" />
      </td>
    </tr>
    
    <xsl:if test="/result/error">
      <tr>
        <td colspan="2">
          <a class="button" onclick="this.blur();" style="float:right"> 
            <xsl:attribute name="href">javascript:hidePrompt()</xsl:attribute>
            <span><xsl:value-of select="/result/resources/msg[@key='button.return']/@value" /></span>
          </a>              
        </td>
      </tr>
    </xsl:if>
    
    <xsl:if test="not(/result/error)">
      <tr>
        <td>
          <a class="button" href="javascript:void(0)"> 
            <xsl:if test="/result/writeProtected">
              <xsl:attribute  name="onclick">delFileAjax('<xsl:value-of select="/result/pathForScript" />', true)</xsl:attribute>
            </xsl:if>
            <xsl:if test="not(/result/writeProtected)">
              <xsl:attribute  name="onclick">delFileAjax('<xsl:value-of select="/result/pathForScript" />')</xsl:attribute>
            </xsl:if>
            <span><xsl:value-of select="/result/resources/msg[@key='button.delete']/@value" /></span>
          </a>              

          <a class="button" href="javascript:void(0)" style="float:right"> 
            <xsl:attribute name="onclick">hidePrompt()</xsl:attribute>
            <span><xsl:value-of select="/result/resources/msg[@key='button.cancel']/@value" /></span>
          </a>              
        </td>
      </tr>
    </xsl:if>

  </table>
  
</xsl:template>

</xsl:stylesheet>
