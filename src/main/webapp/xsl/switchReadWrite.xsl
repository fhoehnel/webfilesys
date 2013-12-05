<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">
  
  <div class="promptHead">
    <xsl:value-of select="/readWriteStatus/shortPath" />
  </div>
  
  <br/>

  <form accept-charset="utf-8" name="swtichReadWriteForm" method="get" action="/webfilesys/servlet">
  
    <input type="hidden" name="command" value="switchReadWrite" />
    
    <input type="hidden" name="readonly">
      <xsl:if test="/readWriteStatus/readonly">
        <xsl:attribute name="value">no</xsl:attribute>
      </xsl:if>
      <xsl:if test="not(/readWriteStatus/readonly)">
        <xsl:attribute name="value">yes</xsl:attribute>
      </xsl:if>
    </input>

    <input type="hidden" name="filePath">
      <xsl:attribute name="value"><xsl:value-of select="/readWriteStatus/path" /></xsl:attribute>
    </input> 
 
    <table border="0" width="100%" cellpadding="10">
      <tr>
        <td class="formParm1">
	      <span resource="label.readWriteStatus" />:
        </td>
        <td class="formParm2">
          <xsl:if test="/readWriteStatus/readonly">
		    <xsl:attribute name="resource">label.statusReadOnly</xsl:attribute>
          </xsl:if>
          <xsl:if test="not(/readWriteStatus/readonly)">
		    <xsl:attribute name="resource">label.statusWritable</xsl:attribute>
          </xsl:if>
        </td>
      </tr>

      <tr>
      
        <td colspan="2">
	  
          <input type="button" style="float:left">
            <xsl:if test="/readWriteStatus/readonly">
		      <xsl:attribute name="resource">label.setrw</xsl:attribute>
		    </xsl:if>
            <xsl:if test="not(/readWriteStatus/readonly)">
		      <xsl:attribute name="resource">label.setro</xsl:attribute>
		    </xsl:if>
            <xsl:attribute name="onclick">javascript:submitSwitchReadWrite()</xsl:attribute>
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
