<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="compareFolder" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/compareFolder/css" />.css</xsl:attribute>
  </link>

  <title>
    <xsl:value-of select="/compareFolder/resources/msg[@key='headline.compareFolders']/@value" />
  </title>

  <script language="JavaScript" src="/webfilesys/javascript/ajaxFolder.js" type="text/javascript"></script>

</head>

<body>

  <div class="headline">
        <xsl:value-of select="/compareFolder/resources/msg[@key='headline.compareFolders']/@value" />
  </div>

  <table class="dataForm" width="100%">
    
    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
        <xsl:value-of select="/compareFolder/resources/msg[@key='label.compSourceFolder']/@value" />:
      </td>
      <td class="formParm2">
        <xsl:value-of select="/compareFolder/sourcePath" />
      </td>
    </tr>
    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
        <xsl:value-of select="/compareFolder/resources/msg[@key='label.compTargetFolder']/@value" />:
      </td>
      <td class="formParm2">
        <xsl:value-of select="/compareFolder/targetPath" />
      </td>
    </tr>
    
    <xsl:if test="/compareFolder/invisibleItems">
      <tr>
        <td class="formParm2" colspan="2">
          <font class="error">
            <xsl:value-of select="/compareFolder/resources/msg[@key='sync.invisibleItems']/@value" />
          </font>
        </td>
      </tr>
    </xsl:if>
    
    <xsl:if test="not(/compareFolder/differencesList/difference)">
      <tr>
        <td class="formParm2" colspan="2">
          <br/>
          <xsl:value-of select="/compareFolder/resources/msg[@key='sync.noDifference']/@value" />
          <br/>
        </td>
      </tr>
    </xsl:if>
    
    <tr>
      <td colspan="2" style="text-align:center;padding:10px;">
        <input type="button">
          <xsl:attribute name="value"><xsl:value-of select="/compareFolder/resources/msg[@key='button.closewin']/@value" /></xsl:attribute>
          <xsl:attribute name="onclick">deselectCompFolders();</xsl:attribute>
        </input>
      </td>
    </tr>
  
  </table>
  
  <xsl:if test="/compareFolder/differencesList/difference">
    
    <table class="topLess" border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
        <th class="syncListHead" align="left">
          <xsl:value-of select="/compareFolder/resources/msg[@key='sync.tableHeadPath']/@value" />
        </th>
        <th class="syncListHead" align="left">
          <xsl:value-of select="/compareFolder/resources/msg[@key='sync.tableHeadSource']/@value" />
        </th>
        <th class="syncListHead" align="left">
          <xsl:value-of select="/compareFolder/resources/msg[@key='sync.tableHeadTarget']/@value" />
        </th>
      </tr>
    
      <xsl:for-each select="/compareFolder/differencesList/difference">
        <tr>
          <td class="syncCompare">
            <xsl:if test="(diffType='1') or (diffType='3') or (diffType='5') or (diffType='6') or (diffType='7') or (diffType='8')">
              <xsl:value-of select="source/displayPath" />
            </xsl:if>
            <xsl:if test="(diffType='2') or (diffType='4')">
              <xsl:value-of select="target/displayPath" />
            </xsl:if>
          </td>
          
          <td class="syncCompare">
            <xsl:if test="(diffType='1')">
              <img src="/webfilesys/images/checked.gif" width="18" height="15" border="0" />
            </xsl:if>
            <xsl:if test="(diffType='2')">
              <xsl:value-of select="/compareFolder/resources/msg[@key='sync.missing']/@value" />
            </xsl:if>
            <xsl:if test="(diffType='3')">
              <img src="/webfilesys/images/checked.gif" width="18" height="15" border="0" />
            </xsl:if>
            <xsl:if test="(diffType='4')">
              <xsl:value-of select="/compareFolder/resources/msg[@key='sync.missing']/@value" />
            </xsl:if>
            <xsl:if test="(diffType='5')">
              <xsl:value-of select="source/size" />
              &#160;
              <xsl:value-of select="/compareFolder/resources/msg[@key='sync.bytes']/@value" />
            </xsl:if>
            <xsl:if test="(diffType='6')">
              <xsl:value-of select="source/modified" />
            </xsl:if>
            <xsl:if test="(diffType='7')">
              <xsl:value-of select="source/size" />
              &#160;
              <xsl:value-of select="/compareFolder/resources/msg[@key='sync.bytes']/@value" />
              <br/>
              <xsl:value-of select="source/modified" />
            </xsl:if>
            <xsl:if test="(diffType='8')">
              <xsl:if test="source/canRead='true'">
                <xsl:if test="source/canWrite='true'">
                  <xsl:value-of select="/compareFolder/resources/msg[@key='sync.access.readwrite']/@value" />
                </xsl:if>
                <xsl:if test="not(source/canWrite='true')">
                  <xsl:value-of select="/compareFolder/resources/msg[@key='sync.access.readonly']/@value" />
                </xsl:if>
              </xsl:if>
              <xsl:if test="not(source/canRead='true')">
                <xsl:value-of select="/compareFolder/resources/msg[@key='sync.access.none']/@value" />
              </xsl:if>
            </xsl:if>
          </td>

          <td class="syncCompare">
            <xsl:if test="(diffType='1')">
              <xsl:value-of select="/compareFolder/resources/msg[@key='sync.missing']/@value" />
            </xsl:if>
            <xsl:if test="(diffType='2')">
              <img src="/webfilesys/images/checked.gif" width="18" height="15" border="0" />
            </xsl:if>
            <xsl:if test="(diffType='3')">
              <xsl:value-of select="/compareFolder/resources/msg[@key='sync.missing']/@value" />
            </xsl:if>
            <xsl:if test="(diffType='4')">
              <img src="/webfilesys/images/checked.gif" width="18" height="15" border="0" />
            </xsl:if>
            <xsl:if test="(diffType='5')">
              <xsl:value-of select="target/size" />
              &#160;
              <xsl:value-of select="/compareFolder/resources/msg[@key='sync.bytes']/@value" />
            </xsl:if>
            <xsl:if test="(diffType='6')">
              <xsl:value-of select="target/modified" />
            </xsl:if>
            <xsl:if test="(diffType='7')">
              <xsl:value-of select="target/size" />
              &#160;
              <xsl:value-of select="/compareFolder/resources/msg[@key='sync.bytes']/@value" />
              <br/>
              <xsl:value-of select="target/modified" />
            </xsl:if>
            <xsl:if test="(diffType='8')">
              <xsl:if test="target/canRead='true'">
                <xsl:if test="target/canWrite='true'">
                  <xsl:value-of select="/compareFolder/resources/msg[@key='sync.access.readwrite']/@value" />
                </xsl:if>
                <xsl:if test="not(target/canWrite='true')">
                  <xsl:value-of select="/compareFolder/resources/msg[@key='sync.access.readonly']/@value" />
                </xsl:if>
              </xsl:if>
              <xsl:if test="not(target/canRead='true')">
                <xsl:value-of select="/compareFolder/resources/msg[@key='sync.access.none']/@value" />
              </xsl:if>
            </xsl:if>
          </td>
        </tr>
      </xsl:for-each>
    </table>
    
    <table class="dataForm" width="100%">
      <tr>
        <td style="text-align:center;padding:10px;">
          <input type="button">
            <xsl:attribute name="value"><xsl:value-of select="/compareFolder/resources/msg[@key='button.closewin']/@value" /></xsl:attribute>
            <xsl:attribute name="onclick">deselectCompFolders();</xsl:attribute>
          </input>
        </td>
      </tr>
    </table>
    
  </xsl:if>

</body>

</html>

</xsl:template>

</xsl:stylesheet>

