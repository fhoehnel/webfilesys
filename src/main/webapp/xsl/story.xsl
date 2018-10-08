<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="fileList file" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
<link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/fileList/css" />.css</xsl:attribute>
</link>

<script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
<script src="javascript/fmweb.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
<script src="javascript/viewMode.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
<script src="javascript/contextMenuCommon.js" type="text/javascript"></script>
<script src="javascript/graphicsContextMenu.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
<script type="text/javascript">
  <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/fileList/language" /></xsl:attribute>
</script>

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

  <xsl:if test="/fileList/maintananceMode">
    <script type="text/javascript">
      alert(resourceBundle["alert.maintanance"]);
    </script>
  </xsl:if>

  <xsl:if test="/fileList/dirNotFound">
    <script type="text/javascript">
      alert(resourceBundle["alert.dirNotFound"]);
    </script>
  </xsl:if>

</head>

<body class="fileListNoMargin">

<xsl:apply-templates />

</body>

<script type="text/javascript">
  setBundleResources();
</script>

</html>

<div id="msg1" class="msgBox" style="visibility:hidden" />

</xsl:template>
<!-- end root node-->

<xsl:template match="fileList">

  <div class="headline headlineBorderless">
    <xsl:value-of select="headLine" disable-output-escaping="yes" />
  </div>

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
      <td class="tabSpacer" style="min-width:13px;"></td>
      
      <xsl:if test="not(role) or (role!='album')">
        <td class="tabInactive" nowrap="true">
          <a class="tab" href="javascript:viewModeList()" resource="label.modelist">
          </a>
        </td>

        <td class="tabSpacer"></td>

      </xsl:if>
 
      <td class="tabInactive" nowrap="true">
        <a class="tab" resource="label.modethumb">
          <xsl:if test="role and (role='album')">
            <xsl:attribute name="href">javascript:viewModeAlbum()</xsl:attribute>
          </xsl:if>
          <xsl:if test="not(role) or (role!='album')">
            <xsl:attribute name="href">javascript:viewModeThumbs()</xsl:attribute>
          </xsl:if>
        </a>
      </td>

      <xsl:if test="/fileList/videoEnabled">

        <xsl:if test="not(role) or (role!='album')">
          <td class="tabSpacer"></td>

          <td class="tabInactive" nowrap="true">
            <a class="tab" href="javascript:viewModeVideo()" resource="label.modeVideo">
            </a>
          </td>

        </xsl:if>

      </xsl:if>

      <td class="tabSpacer"></td>

      <td class="tabActive" nowrap="true">
        <span resource="label.modestory"></span>
      </td>
      
      <td class="tabSpacer"></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeSlideshow()" resource="label.modeSlideshow">
        </a>
      </td>

      <xsl:if test="not(role) or (role!='album')">
        <xsl:if test="not(/fileList/readonly)">
          <td class="tabSpacer"></td>

          <td class="tabInactive" nowrap="true">
            <a class="tab" href="javascript:fileStats()" resource="label.fileStats">
            </a>
          </td>
        </xsl:if>
      </xsl:if>

      <td class="tabSpacer" style="width:90%"></td>
    </tr>
  </table>
  <!-- tabs end -->

  <xsl:if test="/fileList/file">
  
    <form accept-charset="utf-8" name="sortform" method="get" action="/webfilesys/servlet" style="padding:0px;margin:0px;">
    
      <input type="hidden" name="command" value="storyInFrame" />
    
      <table class="fileListFilterSort2">
 
        <tr>
          <td class="fileListFunctCont sep">

            <table border="0" cellpadding="2" width="100%">
              <tr>
            
                <xsl:if test="paging/currentPage &gt; 1">
                  <td class="fileListFunct" valign="center" nowrap="true">
                    <a href="/webfilesys/servlet?command=storyInFrame&amp;startIdx=0" class="icon-font icon-paging icon-page-first" style="margin-right:12px"></a>
                    <a class="icon-font icon-paging icon-page-prev">
                      <xsl:attribute name="href">/webfilesys/servlet?command=storyInFrame&amp;startIdx=<xsl:value-of select="paging/prevStartIdx"/></xsl:attribute>
                    </a>
                  </td>
                </xsl:if>
            
                <td class="fileListFunct" valign="center" nowrap="true">
                  <span resource="label.files"></span>
                  <xsl:text> </xsl:text>
                  <xsl:value-of select="paging/firstOnPage" />
                  ...
                  <xsl:value-of select="paging/lastOnPage" />
                  <xsl:text> </xsl:text>
                  <span resource="label.of"></span>
                  <xsl:text> </xsl:text>
                  <xsl:value-of select="fileNumber" />
                </td>
              
                <xsl:if test="fileNumber &gt; paging/pageSize">
              
                  <td class="fileListFunct" valign="center" nowrap="true">
                    <span resource="label.page"></span>

                    <xsl:for-each select="paging/page">
                      <span>
                        <xsl:if test="@num=../currentPage">
                          <div class="pagingPage pagingPageCurrent">
                            <xsl:value-of select="@num" />
                          </div>
                        </xsl:if>
                        <xsl:if test="not(@num=../currentPage)">
                          <div class="pagingPage pagingPageOther">
                            <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet?command=storyInFrame&amp;startIdx=<xsl:value-of select="@startIdx" />'</xsl:attribute>
                            <xsl:value-of select="@num" />
                          </div>
                        </xsl:if>
                      </span>
                    </xsl:for-each>
                  </td>
                </xsl:if>

                <td class="fileListFunct" align="right" nowrap="true">
                  <input type="text" name="pageSize" maxlength="4" style="width:36px;">
                    <xsl:attribute name="value">
                      <xsl:value-of select="paging/pageSize" />
                    </xsl:attribute>
                  </input>
                  <input type="button" style="margin-left:4px" resource="albumPageSize">
                    <xsl:attribute name="onclick">javascript:document.sortform.submit()</xsl:attribute>
                  </input> 
                </td>

                <xsl:if test="paging/nextStartIdx">
                  <td class="fileListFunct">
                    <img src="/webfilesys/images/space.gif" border="0" width="16" />
                  </td>
              
                  <td class="fileListFunct" align="right" valign="center">
                    <div>
                      <a class="icon-font icon-paging icon-page-last">
                        <xsl:attribute name="href">/webfilesys/servlet?command=storyInFrame&amp;startIdx=<xsl:value-of select="paging/lastStartIdx" /></xsl:attribute>
                      </a>
                      <a class="icon-font icon-paging icon-page-next" style="margin-right:12px">
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;startIdx=',paging/nextStartIdx)"/>
                        </xsl:attribute>
                      </a>
                    </div>
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
          <span resource="alert.nopictures"></span>
        </td>
      </tr>
    </table>
  </xsl:if>
  
  <table class="storyPicCont" cellspacing="0" cellpadding="5">

    <xsl:for-each select="file">
          
      <tr>
        <td class="story2">
        
          <xsl:if test="/fileList/role = 'album'">
            <img class="thumb" border="0">
              <xsl:attribute name="src"><xsl:value-of select="imgPath" /></xsl:attribute>
              <xsl:attribute name="width"><xsl:value-of select="thumbnailWidth" /></xsl:attribute>
              <xsl:attribute name="height"><xsl:value-of select="thumbnailHeight" /></xsl:attribute>
              <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
            </img>
          </xsl:if>

          <xsl:if test="not(/fileList/role = 'album')">
            <a>
              <xsl:if test="position() mod 2 = 1">
                <xsl:attribute name="style">float:right</xsl:attribute>
              </xsl:if>
              <xsl:if test="position() mod 2 = 0">
                <xsl:attribute name="style">float:left</xsl:attribute>
              </xsl:if>
              
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
              </img>
            </a>
          </xsl:if>
            
          <div>
            <xsl:if test="position() mod 2 = 1">
              <xsl:attribute name="style">float:right;margin-right:10px;max-width:45%</xsl:attribute>
            </xsl:if>
            <xsl:if test="position() mod 2 = 0">
              <xsl:attribute name="style">float:left;margin-left:10px;max-width:45%</xsl:attribute>
            </xsl:if>

            <xsl:if test="description">
              <xsl:value-of select="description" />
            </xsl:if>
            <xsl:if test="not(description)">
              <xsl:value-of select="@name" />
            </xsl:if>
          
            <xsl:if test="not(/fileList/role = 'album')">
              <br/>
              <a class="dirtree" titleResource="label.comments">
                <xsl:if test="@link">
                  <xsl:attribute name="href">javascript:jsComments('<xsl:value-of select="realPathForScript" />')</xsl:attribute>
                </xsl:if>
                <xsl:if test="not(@link)">
                  <xsl:attribute name="href">javascript:comm<xsl:value-of select="@id" />()</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="comments" />
                <xsl:text> </xsl:text>
                <span resource="label.comments"></span>
              </a>
            </xsl:if>
          </div>  
          
        </td>
      </tr>
      
    </xsl:for-each>
  </table>  
  
  <xsl:if test="/fileList/file">
  
    <table class="storyButtonCont" cellpadding="4" cellspacing="0">
      <tr>
        <xsl:if test="paging/currentPage &gt; 1">
          <td class="fileListFunct" valign="center" nowrap="true">
            <a href="/webfilesys/servlet?command=storyInFrame&amp;startIdx=0" class="icon-font icon-paging icon-page-first" style="margin-right:12px;"></a>
            <a class="icon-font icon-paging icon-page-prev">
              <xsl:attribute name="href">/webfilesys/servlet?command=storyInFrame&amp;startIdx=<xsl:value-of select="paging/prevStartIdx" /></xsl:attribute>
            </a>
          </td>
        </xsl:if>

        <xsl:if test="paging/nextStartIdx">
          <td class="fileListFunct">
            <img src="/webfilesys/images/space.gif" border="0" width="16" />
          </td>
              
          <td class="fileListFunct" align="right" valign="center" nowrap="true">
            <a class="icon-font icon-paging icon-page-last">
              <xsl:attribute name="href">/webfilesys/servlet?command=storyInFrame&amp;startIdx=<xsl:value-of select="paging/lastStartIdx" /></xsl:attribute>
            </a>
            <a class="icon-font icon-paging icon-page-next" style="margin-right:12px;">
              <xsl:attribute name="href">/webfilesys/servlet?command=storyInFrame&amp;startIdx=<xsl:value-of select="paging/nextStartIdx" /></xsl:attribute>
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
