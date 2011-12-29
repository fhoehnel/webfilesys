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
          <xsl:value-of select="/readWriteStatus/resources/msg[@key='label.readWriteStatus']/@value" />:
        </td>
        <td class="formParm2">
          <xsl:if test="/readWriteStatus/readonly">
            <xsl:value-of select="/readWriteStatus/resources/msg[@key='label.statusReadOnly']/@value" />
          </xsl:if>
          <xsl:if test="not(/readWriteStatus/readonly)">
            <xsl:value-of select="/readWriteStatus/resources/msg[@key='label.statusWritable']/@value" />
          </xsl:if>
        </td>
      </tr>

      <tr>
      
        <td class="formButton" nowrap="nowrap">
          <a class="button" onclick="this.blur()"> 
            <xsl:attribute name="href">javascript:submitSwitchReadWrite()</xsl:attribute>
          
            <xsl:if test="/readWriteStatus/readonly">
              <span><xsl:value-of select="/readWriteStatus/resources/msg[@key='label.setrw']/@value" /></span>
            </xsl:if>

            <xsl:if test="not(/readWriteStatus/readonly)">
              <span><xsl:value-of select="/readWriteStatus/resources/msg[@key='label.setro']/@value" /></span>
            </xsl:if>
          </a>              
        </td>

        <td class="formButton" nowrap="nowrap">
          <a class="button" style="float:right" onclick="this.blur()"> 
            <xsl:attribute name="href">javascript:hidePrompt()</xsl:attribute>
            <span><xsl:value-of select="/readWriteStatus/resources/msg[@key='button.cancel']/@value" /></span>
          </a>              
        </td>
        
      </tr>
    </table>
  </form>

</xsl:template>

</xsl:stylesheet>
