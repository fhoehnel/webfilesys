<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="fileList file" />

<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/fileList/css" />.css</xsl:attribute>
</link>
<link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />

<xsl:if test="not(/fileList/browserXslEnabled)">
  <script src="/webfilesys/javascript/ajaxslt/util.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xmltoken.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/dom.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xpath.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xslt.js" type="text/javascript"></script>
</xsl:if>

<script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/thumbnail.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/viewMode.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/contextMenuCommon.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/contextMenuMouse.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/videoContextMenu.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajax.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajaxGraphics.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/videoAudio.js" type="text/javascript"></script>

<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
<script type="text/javascript">
  <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/fileList/language" /></xsl:attribute>
</script>

<script type="text/javascript">

  var pathForScript = '<xsl:value-of select="/fileList/pathForScript" />';
  
  var path = '<xsl:value-of select="/fileList/menuPath" />';

  var relativePath = '<xsl:value-of select="/fileList/relativePath" />';
  
  var lastScrollPos = 0;
  
</script>

<xsl:if test="/fileList/maintananceMode">
  <script language="javascript">
    alert(resourceBundle["alert.maintanance"]);
  </script>
</xsl:if>

<xsl:if test="/fileList/dirNotFound">
  <script language="javascript">
    alert(resourceBundle["alert.dirNotFound"]);
  </script>
</xsl:if>

<xsl:if test="/fileList/errorMsg">
  <script language="javascript">
    alert('<xsl:value-of select="/fileList/errorMsg" />');
  </script>
</xsl:if>

</head>

<body class="fileListNoMargin">
  <xsl:attribute name="onload">
    setThumbContHeight();
    <xsl:if test="/fileList/file">
      loadVideoThumbs();attachVideoScrollHandler();
    </xsl:if>
  </xsl:attribute>

  <xsl:apply-templates />

  <div id="contextMenu" class="contextMenuCont"></div>

  <div id="msg1" class="msgBox" style="visibility:hidden" />

  <div id="prompt" class="promptBox" style="visibility:hidden" />

</body>

<script type="text/javascript">
  setBundleResources();
</script>

</html>

</xsl:template>

<xsl:template match="fileList">

  <xsl:for-each select="/fileList/currentTrail">
    <div class="headline headlineBorderless">
      <xsl:call-template name="currentTrail" />
    </div>
  </xsl:for-each>

  <xsl:if test="description or geoTag">
    <table id="folderMetaInf" width="100%" border="0" cellpadding="2" cellspacing="0">
      <tr>
      
        <td style="width:90%">
          <xsl:if test="description">
            <font class="small">
              <xsl:value-of select="description" disable-output-escaping="yes" />
            </font>
          </xsl:if>
        </td>
        
      </tr>
    </table>
  </xsl:if>

  <!-- tabs start -->
  <table class="tabs" cellspacing="0">
    <tr>
      <td class="tabSpacer" style="min-width:13px;"></td>
      
      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeList()" resource="label.modelist" />
      </td>
 
      <td class="tabSpacer"></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeThumbs()" resource="label.modethumb" />
      </td>

      <td class="tabSpacer"></td>

      <td class="tabActive" nowrap="true" resource="label.modeVideo" />
      
      <td class="tabSpacer"></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeStory()" resource="label.modestory" />
      </td>
   
      <td class="tabSpacer"></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeSlideshow()" resource="label.modeSlideshow" />
      </td>

      <xsl:if test="not(/fileList/readonly)">
        <td class="tabSpacer"></td>

        <td class="tabInactive" nowrap="true">
          <a class="tab" href="javascript:fileStats()" resource="label.fileStats" />
        </td>
      </xsl:if>

      <td class="tabSpacer" style="width:90%"></td>
    </tr>
  </table>
  <!-- tabs end -->

  <form accept-charset="utf-8" name="sortform" method="get" action="/webfilesys/servlet" style="padding:0px;margin:0px;">
  
    <input type="hidden" name="command" value="listVideos" />
    
	    <table border="0" cellpadding="0" cellspacing="0" width="100%" style="border-bottom-style:none">
	      <input type="hidden" name="actpath">
	        <xsl:attribute name="value">
	          <xsl:value-of select="currentPath" />
	        </xsl:attribute>
	      </input>
	
	      <tr>
	        <td class="fileListFunctCont">
	            
	          <table border="0" cellpadding="2" width="100%">
	            <tr>
	              <td class="fileListFunct" nowrap="nowrap" style="vertical-align:middle;padding-right:20px;">
	                <label resource="label.mask"></label>:
	                <input id="fileMask" type="text" name="mask" size="8" maxlength="256">
	                  <xsl:attribute name="value">
	                    <xsl:value-of select="filter" />
	                  </xsl:attribute>
	                </input>
	              </td>
	                
	              <td style="vertical-align:middle;">
	                <input type="button" resource="label.refresh">
	                  <xsl:attribute name="onclick">javascript:document.sortform.submit()</xsl:attribute>
	                </input> 
	              </td>
	
	              <td width="30%">&#160;</td>  

	              <td class="fileListFunct" nowrap="nowrap">
	                <span id="fileNumber">
                      <xsl:attribute name="fileNumber"><xsl:value-of select="fileNumber" /></xsl:attribute>
                      <xsl:value-of select="fileNumber" />
                    </span>
                    <xsl:text> </xsl:text>
                    <label resource="label.videos"></label>
                    
                    <xsl:if test="sizeSumInt">
                      &#160;
                      <xsl:value-of select="sizeSumInt" />
                      <xsl:if test="sizeSumFract">
                        <label resource="decimalFractPoint"></label>
                        <xsl:value-of select="sizeSumFract" />
                      </xsl:if>
                      <label style="margin-left:5px"><xsl:value-of select="sizeSumUnit" /></label>
                    </xsl:if>
                  </td>
                  
	              <xsl:if test="not(/fileList/file)">
	                <td class="fileListFunct" style="text-align:right" resource="noVideosInFolder" />
	              </xsl:if>
	              
	            </tr>
	          </table>
	        </td>
	      </tr>
	      
	    </table>    
  </form>

  <form accept-charset="utf-8" name="form2" action="/webfilesys/servlet" method="post" style="padding:0px;margin:0px;">
    <input type="hidden" name="actpath">
      <xsl:attribute name="value">
        <xsl:value-of select="currentPath" />
      </xsl:attribute>
    </input>
    
    <input type="hidden" name="command" value="" />

    <div id="scrollAreaCont" class="pictureScrollContNoBorder sepTop" thumbnailType="video">

      <xsl:if test="/fileList/file">

            <xsl:for-each select="/fileList/file">
          
              <div class="thumbnailCont">
                <xsl:attribute name="id">thumbCont-<xsl:value-of select="@nameForId" /></xsl:attribute>
                <a>
                  <xsl:attribute name="id">thumb-<xsl:value-of select="@id" /></xsl:attribute>
                  <xsl:if test="@link">
                    <xsl:attribute name="href">javascript:playVideoMaxSize('<xsl:value-of select="realPathForScript" />', '<xsl:value-of select="@nameForScript" />', true);</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="not(@link)">
                    <xsl:attribute name="href">javascript:playVideoMaxSize('<xsl:value-of select="/fileList/pathForScript" /><xsl:value-of select="@nameForScript" />', '<xsl:value-of select="@nameForScript" />', false);</xsl:attribute>
                  </xsl:if>
                  <img class="thumb" border="0">
                    <xsl:attribute name="id">pic-<xsl:value-of select="@id" /></xsl:attribute>
                    <xsl:attribute name="src">/webfilesys/images/videoPlaceholder.png</xsl:attribute>
                    <xsl:attribute name="width">160</xsl:attribute>
                    <xsl:attribute name="height">120</xsl:attribute>
                    <xsl:attribute name="imgPath"><xsl:value-of select="imgPath" /></xsl:attribute>
                    <xsl:if test="description">
                      <xsl:attribute name="title"><xsl:value-of select="description" /></xsl:attribute>
                    </xsl:if>
                  </img>
                </a>
                <br/>
                <input type="checkbox" class="cb2">
                  <xsl:attribute name="name">list-<xsl:value-of select="@name" /></xsl:attribute>
                  <xsl:if test="@link">
                    <xsl:attribute name="disabled">true</xsl:attribute>
                  </xsl:if>
                </input>
              
                <xsl:if test="@link">
                  <a class="link">
                    <xsl:attribute name="href">javascript:videoLinkMenu('<xsl:value-of select="@nameForScript" />', '<xsl:value-of select="realPathForScript" />', '<xsl:value-of select="@id" />')</xsl:attribute>
                    <xsl:attribute name="title">
                      <xsl:value-of select="'--&gt; '"/>
                      <xsl:value-of select="realPath"/>
                    </xsl:attribute>
                    <xsl:value-of select="displayName" />
                  </a>
                </xsl:if>
  
                <xsl:if test="not(@link)">
                  <a class="fn">
                    <xsl:attribute name="id">fileName-<xsl:value-of select="@id" /></xsl:attribute>
                    <xsl:attribute name="href">javascript:videoContextMenu('<xsl:value-of select="@nameForScript" />', '<xsl:value-of select="@id" />')</xsl:attribute>
                    <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
                    <xsl:value-of select="displayName" />
                  </a>
                </xsl:if>
              
                <div>
                  <xsl:value-of select="@lastModified" />
                </div>
                
                <div>
                  <xsl:value-of select="@size" /> KB
                  &#160;
                  <span>
                    <xsl:attribute name="id">pixDim-<xsl:value-of select="@id" /></xsl:attribute>
                    <xsl:attribute name="picFileName"><xsl:value-of select="@name" /></xsl:attribute>
                    <xsl:if test="@link">
                      <xsl:attribute name="picIsLink">true</xsl:attribute>
                    </xsl:if>
                  </span>
                </div>

                <div>
                  <span>
                    <xsl:attribute name="id">codec-<xsl:value-of select="@id" /></xsl:attribute>
                  </span>
                  &#160;
                  <span>
                    <xsl:attribute name="id">duration-<xsl:value-of select="@id" /></xsl:attribute>
                  </span>
                  &#160;
                  <span>
                    <xsl:attribute name="id">fps-<xsl:value-of select="@id" /></xsl:attribute>
                  </span>
                </div>
                
              </div>
            
            </xsl:for-each>
      </xsl:if>
         
    </div>

    <table id="buttonCont" cellspacing="0" class="fileListButtonCont2">

      <tr>
        <td class="fileListButton sepTop">
        
          <div class="buttonCont">

            <xsl:if test="not(/fileList/readonly)">

              <input type="button" resource="button.upload">
                <xsl:attribute name="onclick">javascript:window.location.href='/webfilesys/servlet?command=uploadParms&amp;actpath='+encodeURIComponent('<xsl:value-of select="/fileList/menuPath" />');</xsl:attribute>
              </input> 
                                 
              <input type="button" resource="button.paste" id="pasteButton">
                <xsl:attribute name="onclick">checkPasteOverwrite()</xsl:attribute>
                <xsl:if test="/fileList/clipBoardEmpty">
                  <xsl:attribute name="style">display:none</xsl:attribute>
                </xsl:if>
              </input> 
        
              <input type="button" resource="button.pasteLink" id="pasteLinkButton">
                <xsl:attribute name="onclick">javascript:pasteLinks();</xsl:attribute>
                <xsl:if test="not(/fileList/copyOperation) or (/fileList/clipBoardEmpty)">
                  <xsl:attribute name="style">display:none</xsl:attribute>
                </xsl:if>
              </input> 
                    
            </xsl:if>
                  
          </div>

        </td>

        <xsl:if test="file">
          <xsl:if test="not(/fileList/readonly)">

          <td class="fileListButton sepTop" style="text-align:right">
            <label resource="label.selectedFiles"></label>:
            &#160;
            <select name="cmd" size="1" onchange="javascript:multiVideoFunction()">
              <option resource="label.selectFunction" />
              <option value="delete" resource="button.delete" />
              <option value="copy" resource="label.copyToClip" />
              <option value="move" resource="label.cutToClip" />
              <option value="concat" resource="concatVideos" />
            </select>
          </td>

          </xsl:if>
        </xsl:if>
        
      </tr>

    </table>

  </form>
  
  <xsl:if test="/fileList/file">
  
    <script type="text/javascript">
      var thumbnails = new Array();
      var loadedThumbs = new Array();
        
      <xsl:for-each select="/fileList/file">
        thumbnails.push("<xsl:value-of select="@id" />");
      </xsl:for-each>
    </script>
      
  </xsl:if>

</xsl:template>

<xsl:include href="currentTrail.xsl" />

</xsl:stylesheet>