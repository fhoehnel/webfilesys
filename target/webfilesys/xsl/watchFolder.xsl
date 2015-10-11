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
            <span resource="folderWatchStatusOn"></span>
          </xsl:if>
          <xsl:if test="not(/folderWatch/watched)">
            <span resource="folderWatchStatusOff"></span>
          </xsl:if>
        </td>
      </tr>

      <tr>
      
        <td class="formButton" nowrap="nowrap" style="text-align:left">
          <xsl:if test="/folderWatch/watched">
            <input type="button" resource="button.stopWatch">
              <xsl:attribute name="onclick">switchFolderWatch('<xsl:value-of select="/folderWatch/path" />')</xsl:attribute>
            </input>
          </xsl:if>
          <xsl:if test="not(/folderWatch/watched)">
            <input type="button" resource="button.startWatch">
              <xsl:attribute name="onclick">switchFolderWatch('<xsl:value-of select="/folderWatch/path" />')</xsl:attribute>
            </input>
          </xsl:if>
        </td>

        <td class="formButton" nowrap="nowrap" style="text-align:right">
          <input type="button" onclick="hidePrompt()" resource="button.cancel" />
        </td>
        
      </tr>
    </table>
  </form>

</xsl:template>

</xsl:stylesheet>
