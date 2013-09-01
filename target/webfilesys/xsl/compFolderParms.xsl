<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead">
  <xsl:value-of select="/compareFolder/resources/msg[@key='label.compSource']/@value" />
</div>
    
<form accept-charset="utf-8" name="compParmsForm" id="compParms" method="get" action="/webfilesys/servlet" style="display:inline;">
  <input type="hidden" name="command" value="compareFolders" />
  
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
        <xsl:value-of select="/compareFolder/resources/msg[@key='label.compSourceFolder']/@value" />:
      </td>
    </tr>
    <tr>      
      <td class="formParm2" style="text-align:left">
        <xsl:value-of select="/compareFolder/sourcePath" />
      </td>
    </tr>
    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
        <xsl:value-of select="/compareFolder/resources/msg[@key='label.compTargetFolder']/@value" />:
      </td>
    </tr>
    <tr>      
      <td class="formParm2" style="text-align:left">
        <xsl:value-of select="/compareFolder/targetPath" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
        <input type="checkbox" name="ignoreDate" class="cb3" checked="checked" />
        <xsl:value-of select="/compareFolder/resources/msg[@key='label.checkboxCompIgnoreDate']/@value" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
        <input type="checkbox" name="treeView" class="cb3" checked="checked" />
        <xsl:value-of select="/compareFolder/resources/msg[@key='checkboxCompShowAsTree']/@value" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
        <input type="checkbox" name="ignoreMetainf" class="cb3" checked="checked" />
        <xsl:value-of select="/compareFolder/resources/msg[@key='checkboxCompIgnoreMetainf']/@value" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
        <input type="checkbox" name="ignorePattern" class="cb3" onchange="enableDisablePatternInput()" />
        <xsl:value-of select="/compareFolder/resources/msg[@key='checkboxCompIgnorePattern']/@value" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap">
        <input id="excludePattern" type="text" name="excludePattern" disabled="disabled" value="*sample*exclude*" style="width:240px;margin-left:20px;" />
      </td>
    </tr>

    <tr>
      <td style="text-align:center">
        <input type="button">
          <xsl:attribute name="value"><xsl:value-of select="/compareFolder/resources/msg[@key='button.compare']/@value" /></xsl:attribute>
          <xsl:attribute name="onclick">openCompWindow();hidePrompt()</xsl:attribute>
        </input>
        &#160;&#160;&#160;
        <input type="button">
          <xsl:attribute name="value"><xsl:value-of select="/compareFolder/resources/msg[@key='button.cancel']/@value" /></xsl:attribute>
          <xsl:attribute name="onclick">cancelCompare();hidePrompt()</xsl:attribute>
        </input>
      </td>
    </tr>

  </table>
  
</form>

</xsl:template>

</xsl:stylesheet>
