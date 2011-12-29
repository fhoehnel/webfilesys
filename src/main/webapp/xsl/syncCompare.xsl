<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="synchronize" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<xsl:if test="not(folderTree/browserXslEnabled)">
  <script language="JavaScript" src="/webfilesys/javascript/ajaxslt/util.js" type="text/javascript"></script>
  <script language="JavaScript" src="/webfilesys/javascript/ajaxslt/xmltoken.js" type="text/javascript"></script>
  <script language="JavaScript" src="/webfilesys/javascript/ajaxslt/dom.js" type="text/javascript"></script>
  <script language="JavaScript" src="/webfilesys/javascript/ajaxslt/xpath.js" type="text/javascript"></script>
  <script language="JavaScript" src="/webfilesys/javascript/ajaxslt/xslt.js" type="text/javascript"></script>
</xsl:if>

<script language="JavaScript" src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/ajaxFolder.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/synchronize.js" type="text/javascript"></script>

<script type="text/javascript">
  var nothingSelected = '<xsl:value-of select="/synchronize/resources/msg[@key='sync.nothingSelected']/@value" />';
  
  function startSynchronize() 
  {
      var confirmMsg = '<xsl:value-of select="/synchronize/resources/msg[@key='sync.confirmStartSync']/@value" />';
      
      <xsl:if test="(/synchronize/differencesList/difference/diffType='2') or (/synchronize/differencesList/difference/diffType='4')">
        removeExtraTargetCheckbox = document.getElementById('removeExtraTarget');
        
        if (removeExtraTargetCheckbox.checked)
        {
            confirmMsg = '<xsl:value-of select="/synchronize/resources/msg[@key='sync.confirmRemoveExtraTarget']/@value" />';
        }
      </xsl:if>
    
      if (confirm(confirmMsg))
      {
          document.form1.submit();
      }
  }
  
</script>

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/synchronize/css" />.css</xsl:attribute>
</link>

<title>
  <xsl:value-of select="/synchronize/resources/msg[@key='headline.syncCompare']/@value" />
</title>

</head>

<body>

  <table border="0" width="100%" cellpadding="2" cellspacing="0">
    <tr>
      <th class="headline">
        <xsl:value-of select="/synchronize/resources/msg[@key='headline.syncCompare']/@value" />
      </th>
    </tr>
  </table>

  <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet" style="margin-top:20px">
  
    <input type="hidden" name="command" value="synchronize" />
  
    <table class="dataForm" width="100%">
    
      <tr>
        <td class="formParm1">
          <xsl:value-of select="/synchronize/resources/msg[@key='label.syncSource']/@value" />:
        </td>
        <td class="formParm2">
          <xsl:value-of select="/synchronize/syncSourcePath" />
        </td>
      </tr>

      <tr>
        <td class="formParm1">
          <xsl:value-of select="/synchronize/resources/msg[@key='label.syncTarget']/@value" />:
        </td>
        <td class="formParm2">
          <xsl:value-of select="/synchronize/syncTargetPath" />
        </td>
      </tr>

      <xsl:if test="/synchronize/differencesList/difference">
        <xsl:if test="(/synchronize/differencesList/difference/diffType='1') or (/synchronize/differencesList/difference/diffType='3')">
          <tr>
            <td colspan="2" class="formParm1">
              <input type="checkbox" class="cb3" name="createMissingTarget" />
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.action.createMissingTarget']/@value" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="(/synchronize/differencesList/difference/diffType='2') or (/synchronize/differencesList/difference/diffType='4')">
          <tr>
            <td colspan="2" class="formParm1">
              <input type="checkbox" class="cb3" name="createMissingSource" id="createMissingSource" onclick="checkExclusion(this,'removeExtraTarget')"/>
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.action.createMissingSource']/@value" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="(/synchronize/differencesList/difference/diffType='2') or (/synchronize/differencesList/difference/diffType='4')">
          <tr>
            <td colspan="2" class="formParm1">
              <input type="checkbox" class="cb3" name="removeExtraTarget" id="removeExtraTarget" onclick="checkExclusion(this,'createMissingSource')"/>
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.action.removeExtraTarget']/@value" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="(/synchronize/differencesList/difference/diffType='6') or (/synchronize/differencesList/difference/diffType='7')">
          <tr>
            <td colspan="2" class="formParm1">
              <input type="checkbox" class="cb3" name="copyNewerToTarget" />
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.action.copyNewerToTarget']/@value" />
            </td>
          </tr>
          <tr>
            <td colspan="2" class="formParm1">
              <input type="checkbox" class="cb3" name="copyNewerToSource" />
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.action.copyNewerToSource']/@value" />
            </td>
          </tr>
          <tr>
            <td colspan="2" class="formParm1">
              <input type="checkbox" class="cb3" name="copyDateChangeToTarget" />
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.action.copyDateChangeToTarget']/@value" />
            </td>
          </tr>
        </xsl:if>
        
        <xsl:if test="(/synchronize/differencesList/difference/diffType='5')">
          <tr>
            <td colspan="2" class="formParm1">
              <input type="checkbox" class="cb3" name="copySizeChangeToTarget" />
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.action.copySizeChangeToTarget']/@value" />
            </td>
          </tr>
        </xsl:if>

        <xsl:if test="(/synchronize/differencesList/difference/diffType='8')">
          <tr>
            <td colspan="2" class="formParm1">
              <input type="checkbox" class="cb3" name="copyAccessRights" />
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.action.copyAccessRights']/@value" />
            </td>
          </tr>
        </xsl:if>
      </xsl:if>

      <xsl:if test="not(/synchronize/differencesList/difference)">
        <tr>
          <td class="formParm2" colspan="2">
            <br/>
            <xsl:value-of select="/synchronize/resources/msg[@key='sync.noDifference']/@value" />
            <br/>
          </td>
        </tr>
      </xsl:if>
      
      <tr>
        <xsl:if test="/synchronize/differencesList/difference">
          <td class="formButton">
            <input type="button">
              <xsl:attribute name="value"><xsl:value-of select="/synchronize/resources/msg[@key='button.startSync']/@value" /></xsl:attribute>
              <xsl:attribute name="onclick">startSynchronize()</xsl:attribute>
            </input>
          </td>
        </xsl:if>

        <xsl:if test="/synchronize/differencesList/difference">
          <td class="formButton" style="text-align:right;">
            <input type="button">
              <xsl:attribute name="value"><xsl:value-of select="/synchronize/resources/msg[@key='button.cancel']/@value" /></xsl:attribute>
              <xsl:attribute name="onclick">deselectSyncFolders()</xsl:attribute>
            </input>
          </td>
        </xsl:if>

        <xsl:if test="not(/synchronize/differencesList/difference)">
          <td style="padding-top:5px;padding-bottom:10px;">
            <input type="button">
              <xsl:attribute name="value"><xsl:value-of select="/synchronize/resources/msg[@key='button.closewin']/@value" /></xsl:attribute>
              <xsl:attribute name="onclick">deselectSyncFolders()</xsl:attribute>
            </input>
          </td>
          <td>&#160;</td>
        </xsl:if>

      </tr>
      
      <xsl:if test="/synchronize/invisibleItems">
        <tr>
          <td class="formParm2" colspan="2">
            <font class="error">
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.invisibleItems']/@value" />
            </font>
          </td>
        </tr>
      </xsl:if>
      
    </table>
    
    <xsl:if test="/synchronize/differencesList/difference">
    
    <table class="topLess" border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
        <th class="syncListHead" align="left">
          <xsl:value-of select="/synchronize/resources/msg[@key='sync.tableHeadPath']/@value" />
        </th>
        <th class="syncListHead" align="left">
          <xsl:value-of select="/synchronize/resources/msg[@key='sync.tableHeadSource']/@value" />
        </th>
        <th class="syncListHead" align="left">
          <xsl:value-of select="/synchronize/resources/msg[@key='sync.tableHeadTarget']/@value" />
        </th>
      </tr>
    
      <xsl:for-each select="/synchronize/differencesList/difference">
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
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.missing']/@value" />
            </xsl:if>
            <xsl:if test="(diffType='3')">
              <img src="/webfilesys/images/checked.gif" width="18" height="15" border="0" />
            </xsl:if>
            <xsl:if test="(diffType='4')">
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.missing']/@value" />
            </xsl:if>
            <xsl:if test="(diffType='5')">
              <xsl:value-of select="source/size" />
              &#160;
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.bytes']/@value" />
            </xsl:if>
            <xsl:if test="(diffType='6')">
              <xsl:value-of select="source/modified" />
            </xsl:if>
            <xsl:if test="(diffType='7')">
              <xsl:value-of select="source/size" />
              &#160;
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.bytes']/@value" />
              <br/>
              <xsl:value-of select="source/modified" />
            </xsl:if>
            <xsl:if test="(diffType='8')">
              <xsl:if test="source/canRead='true'">
                <xsl:if test="source/canWrite='true'">
                  <xsl:value-of select="/synchronize/resources/msg[@key='sync.access.readwrite']/@value" />
                </xsl:if>
                <xsl:if test="not(source/canWrite='true')">
                  <xsl:value-of select="/synchronize/resources/msg[@key='sync.access.readonly']/@value" />
                </xsl:if>
              </xsl:if>
              <xsl:if test="not(source/canRead='true')">
                <xsl:value-of select="/synchronize/resources/msg[@key='sync.access.none']/@value" />
              </xsl:if>
            </xsl:if>
          </td>

          <td class="syncCompare">
            <xsl:if test="(diffType='1')">
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.missing']/@value" />
            </xsl:if>
            <xsl:if test="(diffType='2')">
              <img src="/webfilesys/images/checked.gif" width="18" height="15" border="0" />
            </xsl:if>
            <xsl:if test="(diffType='3')">
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.missing']/@value" />
            </xsl:if>
            <xsl:if test="(diffType='4')">
              <img src="/webfilesys/images/checked.gif" width="18" height="15" border="0" />
            </xsl:if>
            <xsl:if test="(diffType='5')">
              <xsl:value-of select="target/size" />
              &#160;
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.bytes']/@value" />
            </xsl:if>
            <xsl:if test="(diffType='6')">
              <xsl:value-of select="target/modified" />
            </xsl:if>
            <xsl:if test="(diffType='7')">
              <xsl:value-of select="target/size" />
              &#160;
              <xsl:value-of select="/synchronize/resources/msg[@key='sync.bytes']/@value" />
              <br/>
              <xsl:value-of select="target/modified" />
            </xsl:if>
            <xsl:if test="(diffType='8')">
              <xsl:if test="target/canRead='true'">
                <xsl:if test="target/canWrite='true'">
                  <xsl:value-of select="/synchronize/resources/msg[@key='sync.access.readwrite']/@value" />
                </xsl:if>
                <xsl:if test="not(target/canWrite='true')">
                  <xsl:value-of select="/synchronize/resources/msg[@key='sync.access.readonly']/@value" />
                </xsl:if>
              </xsl:if>
              <xsl:if test="not(target/canRead='true')">
                <xsl:value-of select="/synchronize/resources/msg[@key='sync.access.none']/@value" />
              </xsl:if>
            </xsl:if>
          </td>
        </tr>
      </xsl:for-each>
    </table>
    
    </xsl:if>
  
  </form>

</body>

</html>

</xsl:template>

</xsl:stylesheet>

