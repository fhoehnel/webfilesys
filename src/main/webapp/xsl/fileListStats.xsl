<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="fileList file" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="Content-Type" name="text/html; charset=UTF-8" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/fileList/css" />.css</xsl:attribute>
</link>

<script language="JavaScript" src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/viewMode.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/graphicsContextMenu.js" type="text/javascript"></script>

<script type="text/javascript" language="JavaScript">
  function setFileListHeight()
  {
      if (browserMSIE)
      {
          setTimeout('setHeightInternal()', 200);
      }
      else
      {
          setHeightInternal();
      }
  }
  
  function setHeightInternal()
  {
      var windowHeight;
      if (!browserFirefox)
      {
          windowHeight = document.body.clientHeight;
      }
      else
      {
          windowHeight = window.innerHeight;
      }
      
      <xsl:if test="/fileList/description">
        var padding = 220;
      </xsl:if>
      <xsl:if test="not(/fileList/description)">
        var padding = 180;
      </xsl:if>
      
      document.getElementById('fileListTable').style.height = windowHeight - padding + 'px';
  }

  function setSortField(sortBy)
  {
      window.location.href='/webfilesys/servlet?command=fileStats&amp;initial=true&amp;sortBy=' + sortBy;
  }
</script>

</head>

<body onload="setFileListHeight()">

  <xsl:call-template name="fileList" />

</body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template name="fileList">

  <div class="headline">
    <xsl:value-of select="fileList/headLine" />
  </div>

  <xsl:if test="/fileList/description">
    <div class="fileListDesc">
      <font class="small">
        <xsl:value-of select="/fileList/description" disable-output-escaping="yes" />
      </font>
    </div>
  </xsl:if>

  <!-- tabs start -->
  <table class="tabs" cellspacing="0">
    <tr>
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="13" height="1" /></td>
      
      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeList()">
          <xsl:value-of select="/fileList/resources/msg[@key='label.modelist']/@value" />
        </a>
      </td>
 
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeThumbs()">
          <xsl:value-of select="/fileList/resources/msg[@key='label.modethumb']/@value" />
        </a>
      </td>
      
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeStory()">
          <xsl:value-of select="/fileList/resources/msg[@key='label.modestory']/@value" />
        </a>
      </td>
   
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeSlideshow()">
          <xsl:value-of select="/fileList/resources/msg[@key='label.modeSlideshow']/@value" />
        </a>
      </td>

      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabActive" nowrap="true">
        <xsl:value-of select="/fileList/resources/msg[@key='label.fileStats']/@value" />
      </td>

      <td class="bottomLine" width="90%">
        <img src="/webfilesys/images/space.gif" border="0" width="5" height="1" />
      </td>
    </tr>
  </table>
  <!-- tabs end -->
  
  <xsl:if test="/fileList/file">
  
  <form accept-charset="utf-8" name="resetForm" method="get" action="/webfilesys/servlet" style="margin:0">
    <input type="hidden" name="command" value="resetStatistics" />
    <input type="hidden" name="actPath">
      <xsl:attribute name="value"><xsl:value-of select="/fileList/currentPath" /></xsl:attribute>
    </input>
    
    <table class="fileListHead" width="100%">
      <tr>
        <td colspan="4" class="fileListFunct sepBot">&#160;</td>
      </tr>
      
      <tr>
        <th class="fileListHead" style="padding-left:30px;text-align:left">
          <xsl:if test="/fileList/sortBy = '1'">
            <xsl:value-of select="/fileList/resources/msg[@key='label.filename']/@value" />
          </xsl:if>
          <xsl:if test="not(/fileList/sortBy = '1')">
            <a href="javascript:setSortField('1')" class="listHead">
              <xsl:value-of select="/fileList/resources/msg[@key='label.filename']/@value" />
            </a>
          </xsl:if>
        </th>
        <th class="fileListHead" style="width:170px;padding-right:5px;text-align:right;white-space:nowrap;">
          <xsl:if test="/fileList/sortBy = '8'">
            <xsl:value-of select="/fileList/resources/msg[@key='label.downloads']/@value" />
          </xsl:if>
          <xsl:if test="not(/fileList/sortBy = '8')">
            <a href="javascript:setSortField('8')" class="listHead">
              <xsl:value-of select="/fileList/resources/msg[@key='label.downloads']/@value" />
            </a>
          </xsl:if>
          <xsl:if test="/fileList/lastResetDate">
            <br/>
            <span class="plaintext">
              <xsl:value-of select="/fileList/resources/msg[@key='label.since']/@value" /> 
              &#160;
              <xsl:value-of select="/fileList/lastResetDate" />
            </span>              
          </xsl:if>
        </th>
        <th class="fileListHead" style="width:90px;padding-left:5px;padding-right:5px;text-align:right;white-space:nowrap;">
          <xsl:if test="/fileList/sortBy = '7'">
            <xsl:value-of select="/fileList/resources/msg[@key='rating.count']/@value" />
          </xsl:if>
          <xsl:if test="not(/fileList/sortBy = '7')">
            <a href="javascript:setSortField('7')" class="listHead">
              <xsl:value-of select="/fileList/resources/msg[@key='rating.count']/@value" />
            </a>
          </xsl:if>
        </th>
        <th class="fileListHead" style="width:100px;padding-left:5px;padding-right:5px;text-align:right;white-space:nowrap;">
          <xsl:if test="/fileList/sortBy = '9'">
            <xsl:value-of select="/fileList/resources/msg[@key='label.comments']/@value" />
          </xsl:if>
          <xsl:if test="not(/fileList/sortBy = '9')">
            <a href="javascript:setSortField('9')" class="listHead">
              <xsl:value-of select="/fileList/resources/msg[@key='label.comments']/@value" />
            </a>
          </xsl:if>
        </th>
      </tr>
    </table>
    
    <div id="fileListTable" class="fileListScrollDiv">
      
      <table class="fileList" cellspacing="0" cellpadding="0">
      
        <xsl:for-each select="/fileList/file">
      
          <tr>
            <td class="fileList sepBot" style="padding-left:5px;width:22px">
              <img border="0" width="16" height="16">
                <xsl:if test="@icon">
                  <xsl:attribute name="src">/webfilesys/icons/<xsl:value-of select="@icon" /></xsl:attribute>
                </xsl:if>
                <xsl:if test="not(@icon)">
                  <xsl:attribute name="src">webfilesys/images/space.gif</xsl:attribute>
                </xsl:if>
              </img>
            </td>

            <td class="fileList sepBot" style="padding-right:5px">
              <a class="fn">
                <xsl:if test="@displayName">
                  <xsl:value-of select="@displayName" />
                </xsl:if>
                <xsl:if test="not(@displayName)">
                  <xsl:value-of select="@name" />
                </xsl:if>
              </a>
            </td>
            
            <td class="fileList sepBot" align="right" style="width:170px;padding-right:30px" nowrap="nowrap">
              <xsl:value-of select="viewCount"/>
            </td>
            
            <td class="fileList sepBot" align="right" style="width:90px;padding-right:10px" nowrap="nowrap">
              <xsl:value-of select="voteCount"/>
            </td>

            <td class="fileList sepBot" align="right" style="width:90px;padding-right:20px" nowrap="nowrap">
              <xsl:if test="commentCount != '0'">
                <a class="fn">
                  <xsl:attribute name="href">javascript:jsComments('<xsl:value-of select="pathForScript" />')</xsl:attribute>
                  <xsl:value-of select="commentCount"/>
                </a>
              </xsl:if>
              <xsl:if test="commentCount = '0'">
                <xsl:value-of select="commentCount"/>
              </xsl:if>
            </td>
          </tr>
      
        </xsl:for-each>
      
      </table>
      
    </div>
    
    <table class="topLess" border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
        <td class="fileListFunct">
          <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <tr>
              <td class="fileListFunct formButton" style="text-align:right">
                <input type="button" onclick="document.resetForm.submit()">
                  <xsl:attribute name="value"><xsl:value-of select="/fileList/resources/msg[@key='button.resetStats']/@value" /></xsl:attribute>
                </input>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    
  </form>
  
  </xsl:if>
  
  <xsl:if test="not(/fileList/file)">
    <table class="fileListHead" cellspacing="0" cellpadding="0">
      <tr>
        <td class="fileList sepBot" style="padding:10px">0 <xsl:value-of select="/fileList/resources/msg[@key='label.files']/@value" /></td>
      </tr>
    </table>
  </xsl:if>
  
</xsl:template>
  
</xsl:stylesheet>
