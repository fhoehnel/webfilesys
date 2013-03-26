<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="fileList file" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/fileList/css" />.css</xsl:attribute>
</link>

<script language="JavaScript" src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
<SCRIPT language="JavaScript" src="javascript/fmweb.js" type="text/javascript"></SCRIPT>
<SCRIPT language="JavaScript" src="javascript/viewMode.js" type="text/javascript"></SCRIPT>
<SCRIPT language="JavaScript" src="javascript/contextMenuMouse.js" type="text/javascript"></SCRIPT>
<SCRIPT language="JavaScript" src="javascript/graphicsContextMenu.js" type="text/javascript"></SCRIPT>

<script language="javascript">
  
  function showImage(imgPath, width, height)
  {
      randNum = (new Date()).getTime();
      picWin = window.open('/webfilesys/servlet?command=showImg&amp;imgname=' + encodeURIComponent(imgPath) + '&amp;random=' + randNum,'picWin' + randNum,'status=no,toolbar=no,location=no,menu=no,width=' + width + ',height=' + (height + 55) + ',resizable=yes,left=1,top=1,screenX=1,screenY=1');
      picWin.focus();
  }

  <xsl:if test="not(/fileList/role = 'album')">

    <xsl:for-each select="//file">
      <xsl:if test="@link">
        function sli<xsl:value-of select="@id" />()
        {
            showImage('<xsl:value-of select="realPathForScript" />',<xsl:value-of select="fullScreenWidth" />,<xsl:value-of select="fullScreenHeight" />);
        }
      </xsl:if>
      <xsl:if test="not(@link)">
        function si<xsl:value-of select="@id" />()
        {
            showImage('<xsl:value-of select="/fileList/pathForScript" /><xsl:value-of select="@name" />',<xsl:value-of select="fullScreenWidth" />,<xsl:value-of select="fullScreenHeight" />);
        }

        function comm<xsl:value-of select="@id" />()
        {
            jsComments('<xsl:value-of select="/fileList/pathForScript" /><xsl:value-of select="@name" />');
        }
      </xsl:if>
    </xsl:for-each>  
  
  </xsl:if>
  
</script>

<xsl:if test="/fileList/resources/msg[@key='alert.maintanance']">
  <script language="javascript">
    alert('<xsl:value-of select="/fileList/resources/msg[@key='alert.maintanance']/@value" />');
  </script>
</xsl:if>

<xsl:if test="/fileList/resources/msg[@key='alert.dirNotFound']">
  <script language="javascript">
    alert('<xsl:value-of select="/fileList/resources/msg[@key='alert.dirNotFound']/@value" />');
  </script>
</xsl:if>


</head>

<body>

<xsl:apply-templates />

</body>
</html>

<div id="msg1" class="msgBox" style="visibility:hidden" />

</xsl:template>
<!-- end root node-->

<xsl:template match="fileList">

  <table border="0" width="100%" cellpadding="2" cellspacing="0">
    <tr>
      <th class="headline">
        <xsl:value-of select="headLine" disable-output-escaping="yes" />
      </th>
    </tr>
  </table>

  <!-- path for picture album -->
  
  <xsl:if test="role and (role='album')">
    <xsl:for-each select="currentPath">
      <xsl:call-template name="currentPath" />
    </xsl:for-each>
  </xsl:if>
  
  <!-- end path for picture album -->

  <xsl:if test="not(role) or (role!='album')">
    <br/>
  </xsl:if>

  <!-- tabs start -->
  <table border="0" width="100%" cellpadding="0" cellspacing="0">
    <tr>
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="13" height="1" /></td>
      
      <xsl:if test="not(role) or (role!='album')">
        <td class="tabInactive" nowrap="true">
          <a class="tab" href="javascript:viewModeList()">
            <xsl:value-of select="/fileList/resources/msg[@key='label.modelist']/@value" />
          </a>
        </td>

      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      </xsl:if>
 
      <td class="tabInactive" nowrap="true">
        <a class="tab">
          <xsl:if test="role and (role='album')">
            <xsl:attribute name="href">javascript:viewModeAlbum()</xsl:attribute>
          </xsl:if>
          <xsl:if test="not(role) or (role!='album')">
            <xsl:attribute name="href">javascript:viewModeThumbs()</xsl:attribute>
          </xsl:if>
          <xsl:value-of select="/fileList/resources/msg[@key='label.modethumb']/@value" />
        </a>
      </td>

      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabActive" nowrap="true">
        <xsl:value-of select="/fileList/resources/msg[@key='label.modestory']/@value" />
      </td>
      
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeSlideshow()">
          <xsl:value-of select="/fileList/resources/msg[@key='label.modeSlideshow']/@value" />
        </a>
      </td>

      <xsl:if test="not(role) or (role!='album')">
        <xsl:if test="not(/fileList/readonly)">
          <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

          <td class="tabInactive" nowrap="true">
            <a class="tab" href="javascript:fileStats()">
              <xsl:value-of select="/fileList/resources/msg[@key='label.fileStats']/@value" />
            </a>
          </td>
        </xsl:if>
      </xsl:if>

      <td class="bottomLine" width="90%">
        <img src="/webfilesys/images/space.gif" border="0" width="5" height="1" />
      </td>
    </tr>
  </table>
  <!-- tabs end -->

  <xsl:if test="/fileList/file">
  
    <form accept-charset="utf-8" name="sortform" method="get" action="/webfilesys/servlet" style="padding:0px;margin:0px;">
    
      <input type="hidden" name="command" value="storyInFrame" />
    
      <table class="topLess" border="0" cellpadding="0" cellspacing="0" width="100%" style="border-bottom-style:none">
 
        <tr>
          <td class="fileListFunct sep">

            <table border="0" cellpadding="2" width="100%">
              <tr>
            
                <xsl:if test="paging/currentPage &gt; 1">
                  <td class="fileListFunct" valign="center" nowrap="true">
                    <a href="/webfilesys/servlet?command=storyInFrame&amp;startIdx=0"><img src="/webfilesys/images/first.gif" border="0" /></a>
                    &#160;
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;startIdx=',paging/prevStartIdx)"/>
                      </xsl:attribute>
                      <img src="/webfilesys/images/previous.gif" border="0" />
                    </a>
                  </td>
                </xsl:if>
            
                <td class="fileListFunct" valign="center" nowrap="true">
                  <xsl:value-of select="resources/msg[@key='label.files']/@value" /> 
                  &#160;
                  <xsl:value-of select="paging/firstOnPage" />
                  ...
                  <xsl:value-of select="paging/lastOnPage" />
                  &#160;
                  <xsl:value-of select="resources/msg[@key='label.of']/@value" /> 
                  &#160;
                  <xsl:value-of select="fileNumber" />
                </td>
              
                <xsl:if test="fileNumber &gt; paging/pageSize">
              
                  <td class="fileListFunct" valign="center" nowrap="true">
                    <xsl:value-of select="resources/msg[@key='label.page']/@value" /> 

                    <xsl:for-each select="paging/page">
                      <img src="/webfilesys/images/space.gif" border="0" width="5" />
                      <xsl:if test="@num=../currentPage">
                        <xsl:value-of select="@num" />
                      </xsl:if>
                      <xsl:if test="not(@num=../currentPage)">
                        <a class="fn">
                          <xsl:attribute name="href">
                            <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;startIdx=',@startIdx)"/>
                          </xsl:attribute>
                          <xsl:value-of select="@num" />
                        </a>
                      </xsl:if>
                    </xsl:for-each>
                  </td>
                </xsl:if>

                <td class="fileListFunct" align="right" valign="center" nowrap="true">
                  <input type="text" name="pageSize" maxlength="4" style="width:35px;">
                    <xsl:attribute name="value">
                      <xsl:value-of select="paging/pageSize" />
                    </xsl:attribute>
                  </input>
                </td>
                  
                <td class="fileListFunct" valign="center" nowrap="true">
                  <input type="button">
                    <xsl:attribute name="onclick">javascript:document.sortform.submit()</xsl:attribute>
                    <xsl:attribute name="value"><xsl:value-of select="/fileList/resources/msg[@key='label.listPageSize']/@value" /></xsl:attribute>
                  </input> 
                </td>

                <xsl:if test="paging/nextStartIdx">
                  <td class="fileListFunct">
                    <img src="/webfilesys/images/space.gif" border="0" width="16" />
                  </td>
              
                  <td class="fileListFunct" align="right" valign="center" nowrap="true">
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;startIdx=',paging/nextStartIdx)"/>
                      </xsl:attribute>
                      <img src="/webfilesys/images/next.gif" border="0" />
                    </a>
                    &#160;
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;startIdx=',paging/lastStartIdx)"/>
                      </xsl:attribute>
                      <img src="/webfilesys/images/last.gif" border="0" />
                    </a>
                  </td>
                
                </xsl:if>
              </tr>
            </table>
          </td>
        </tr>
      
      </table>
    
    </form>
  </xsl:if>

  <xsl:if test="not(/fileList/file)">
    <table class="topLess" border="0" cellpadding="5" cellspacing="0" width="100%">
      <tr>
        <td class="fileListFunct" style="padding:10px 10px">
          <xsl:value-of select="resources/msg[@key='alert.nopictures']/@value" />
        </td>
      </tr>
    </table>
  </xsl:if>
      
  
  <table class="topLess" border="0" width="100%" cellspacing="0" cellpadding="5">

    <xsl:for-each select="file">
          
      <tr>
        <td class="story2" valign="top">
          <xsl:if test="/fileList/role = 'album'">
            <img class="thumb" border="0">
              <xsl:attribute name="src"><xsl:value-of select="imgPath" /></xsl:attribute>
              <xsl:attribute name="width"><xsl:value-of select="thumbnailWidth" /></xsl:attribute>
              <xsl:attribute name="height"><xsl:value-of select="thumbnailHeight" /></xsl:attribute>
              <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
              <xsl:if test="position() mod 2 = 0">
                <xsl:attribute name="align">right</xsl:attribute>
                <xsl:attribute name="style">margin-left:10px</xsl:attribute>
              </xsl:if>
              <xsl:if test="position() mod 2 = 1">
                <xsl:attribute name="align">left</xsl:attribute>
                <xsl:attribute name="style">margin-right:10px</xsl:attribute>
              </xsl:if>
            </img>
          </xsl:if>

          <xsl:if test="not(/fileList/role = 'album')">
            <a>
              <xsl:if test="@link">
                <xsl:attribute name="href">javascript:sli<xsl:value-of select="@id" />()</xsl:attribute>
              </xsl:if>
              <xsl:if test="not(@link)">
                <xsl:attribute name="href">javascript:si<xsl:value-of select="@id" />()</xsl:attribute>
              </xsl:if>

              <img class="thumb" border="0">
                <xsl:attribute name="src"><xsl:value-of select="imgPath" /></xsl:attribute>
                <xsl:attribute name="width"><xsl:value-of select="thumbnailWidth" /></xsl:attribute>
                <xsl:attribute name="height"><xsl:value-of select="thumbnailHeight" /></xsl:attribute>
                <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
                <xsl:if test="position() mod 2 = 0">
                  <xsl:attribute name="align">right</xsl:attribute>
                  <xsl:attribute name="style">margin-left:10px</xsl:attribute>
                </xsl:if>
                <xsl:if test="position() mod 2 = 1">
                  <xsl:attribute name="align">left</xsl:attribute>
                  <xsl:attribute name="style">margin-right:10px</xsl:attribute>
                </xsl:if>
              </img>
            </a>
          </xsl:if>
            
          <xsl:if test="description">
            <xsl:value-of select="description" />
          </xsl:if>
          <xsl:if test="not(description)">
            <xsl:value-of select="@name" />
          </xsl:if>
          
          <xsl:if test="not(/fileList/role = 'album')">
            <br/>
            <a class="dirtree">
              <xsl:if test="@link">
                <xsl:attribute name="href">javascript:jsComments('<xsl:value-of select="realPathForScript" />')</xsl:attribute>
              </xsl:if>
              <xsl:if test="not(@link)">
                <xsl:attribute name="href">javascript:comm<xsl:value-of select="@id" />()</xsl:attribute>
              </xsl:if>
              <xsl:attribute name="title"><xsl:value-of select="/fileList/resources/msg[@key='label.comments']/@value" /></xsl:attribute>
                (<xsl:value-of select="comments" />)
            </a>
          </xsl:if>
          
        </td>
      </tr>
      
      <xsl:if test="position() != last()">
        <tr>
          <td class="story2 sepTop">
            <img src="//webfilesys/images/space.gif" border="0" width="1" height="1" />
          </td>
        </tr>
      </xsl:if>  
        
    </xsl:for-each>
  </table>  
  
  <xsl:if test="/fileList/file">
  
    <table class="topLess" border="0" width="100%" cellpadding="4" cellspacing="0">
      <tr>
        <xsl:if test="paging/currentPage &gt; 1">
          <td class="fileListFunct" valign="center" nowrap="true">
            <a href="/webfilesys/servlet?command=storyInFrame&amp;startIdx=0"><img src="/webfilesys/images/first.gif" border="0" /></a>
              &#160;
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;startIdx=',paging/prevStartIdx)"/>
              </xsl:attribute>
              <img src="/webfilesys/images/previous.gif" border="0" />
            </a>
          </td>
        </xsl:if>

        <xsl:if test="paging/nextStartIdx">
          <td class="fileListFunct">
            <img src="/webfilesys/images/space.gif" border="0" width="16" />
          </td>
              
          <td class="fileListFunct" align="right" valign="center" nowrap="true">
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;startIdx=',paging/nextStartIdx)"/>
              </xsl:attribute>
              <img src="/webfilesys/images/next.gif" border="0" />
            </a>
            &#160;
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;startIdx=',paging/lastStartIdx)"/>
              </xsl:attribute>
              <img src="/webfilesys/images/last.gif" border="0" />
            </a>
          </td>
        </xsl:if>
                
      </tr>
    </table>
      
  </xsl:if>
  
</xsl:template>

<!-- ############################## path for picture album ################################ -->

<xsl:template name="currentPath">

  <div class="albumPath">
    <xsl:for-each select="pathElem">
      <a class="dirtree">
        <xsl:attribute name="href"><xsl:value-of select="concat('/webfilesys/servlet?command=album&amp;relPath=',@path)"/></xsl:attribute>
        <xsl:value-of select="@name"/> 
      </a>
      <xsl:if test="not(position()=last())"><font class="fixed"><b> &gt; </b></font></xsl:if>
    </xsl:for-each>
  </div>

</xsl:template>

<!-- ############################## end path for picture album ################################ -->

</xsl:stylesheet>
