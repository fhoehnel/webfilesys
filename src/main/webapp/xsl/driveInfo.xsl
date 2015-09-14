<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

  <xsl:variable name="percentUsed" select="/driveInfo/percentUsed" />

  <div class="promptHead" resource="label.driveinfo"></div>
    
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td class="formParm1" nowrap="nowrap">
	    <span resource="label.drive" />:
      </td>
    </tr>
    <tr>
      <td class="formParm2">
        <xsl:value-of select="/driveInfo/drivePath" />
      </td>
    </tr>
    
    <xsl:if test="/driveInfo/drivePath">
      <tr>
        <td class="formParm1" nowrap="nowrap">
	      <span resource="label.driveType" />:
        </td>
      </tr>
      <tr>
        <td class="formParm2">
          <xsl:value-of select="/driveInfo/driveType" />
        </td>
      </tr>
    </xsl:if>

    <tr>
      <td class="formParm1" nowrap="nowrap">
	    <span resource="label.drivelabel" />:
      </td>
    </tr>
    <tr>
      <td class="formParm2">
        <xsl:value-of select="/driveInfo/driveLabel" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap">
	    <span resource="label.diskTotalSpace" />:
      </td>
    </tr>
    <tr>
      <td class="formParm2">
        <xsl:value-of select="/driveInfo/totalDiskSpace" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap">
	    <span resource="label.bytesfree" />:
      </td>
    </tr>
    <tr>
      <td class="formParm2">
        <xsl:value-of select="/driveInfo/freeDiskSpace" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap">
	    <xsl:value-of select="/driveInfo/percentUsed" /><xsl:text> </xsl:text><span resource="label.percentUsed" />
      </td>
    </tr>
    
    <tr>
      <td colspan="2">
        <div class="driveUsageBar">
          <img src="/webfilesys/images/bluedot.gif" height="20" border="0">
            <xsl:attribute name="width"><xsl:value-of select="$percentUsed * 2" /></xsl:attribute> 
          </img>
          <img src="/webfilesys/images/space.gif" height="20" border="0">
            <xsl:attribute name="width"><xsl:value-of select="(100 - $percentUsed) * 2" /></xsl:attribute> 
          </img>
        </div>
      </td>
    </tr>

    <tr>
      <td colspan="2" style="text-align:center">
	  
        <input type="button" resource="button.closewin">
          <xsl:attribute name="onclick">javascript:hidePrompt()</xsl:attribute>
        </input> 
	  
	  </td>
    </tr>

  </table>

</xsl:template>

</xsl:stylesheet>
