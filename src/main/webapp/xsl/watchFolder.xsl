<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">
  
  <div class="promptHead">
    <xsl:value-of select="/folderWatch/shortPath" />
  </div>
  
  <br/>

  <form accept-charset="utf-8" name="switchFolderWatchForm" method="get" action="/webfilesys/servlet">
  
    <input type="hidden" name="command" value="switchFolderWatch" />

    <input type="hidden" name="path">
      <xsl:attribute name="value"><xsl:value-of select="/folderWatch/path" /></xsl:attribute>
    </input> 
 
    <table border="0" width="100%" cellpadding="10">
      <tr>
        <td class="formParm1" colspan="2">
          <xsl:if test="/folderWatch/watched">
            <xsl:value-of select="/folderWatch/resources/msg[@key='folderWatchStatusOn']/@value" />
          </xsl:if>
          <xsl:if test="not(/folderWatch/watched)">
            <xsl:value-of select="/folderWatch/resources/msg[@key='folderWatchStatusOff']/@value" />
          </xsl:if>
        </td>
      </tr>

      <tr>
      
        <td class="formButton" nowrap="nowrap">
          <xsl:if test="/folderWatch/watched">
            <input type="button">
              <xsl:attribute name="onclick">switchFolderWatch('<xsl:value-of select="/folderWatch/path" />')</xsl:attribute>
              <xsl:attribute name="value"><xsl:value-of select="/folderWatch/resources/msg[@key='button.stopWatch']/@value" /></xsl:attribute>
            </input>
          </xsl:if>
          <xsl:if test="not(/folderWatch/watched)">
            <input type="button">
              <xsl:attribute name="onclick">switchFolderWatch('<xsl:value-of select="/folderWatch/path" />')</xsl:attribute>
              <xsl:attribute name="value"><xsl:value-of select="/folderWatch/resources/msg[@key='button.startWatch']/@value" /></xsl:attribute>
            </input>
          </xsl:if>
        </td>

        <td class="formButton" nowrap="nowrap">
          <input type="button" onclick="hidePrompt()">
            <xsl:attribute name="value"><xsl:value-of select="/folderWatch/resources/msg[@key='button.cancel']/@value" /></xsl:attribute>
          </input>
        </td>
        
      </tr>
    </table>
  </form>

</xsl:template>

</xsl:stylesheet>
