<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="treeStats" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <title>        
    <xsl:value-of select="/imageData/resources/msg[@key='label.albumTitle']/@value" />:
    <xsl:value-of select="/imageData/userid" />
    (<xsl:value-of select="/imageData/imageName" />)
  </title>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/imageData/css" />.css</xsl:attribute>
  </link>

  <style type="text/css"> 
    div.albumPath {padding-top:8px;padding-bottom:10px;text-align:left;}
  </style>

  <script src="/webfilesys/javascript/fileMenu.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>

  <xsl:if test="/imageData/geoTag">
    <script src="/webfilesys/javascript/geoMap.js" type="text/javascript"></script>
  </xsl:if>

  <script type="text/javascript">
  
    function printPage() 
    {
        if (confirm('<xsl:value-of select="/imageData/resources/msg[@key='confirm.print']/@value" />'))
        {
            window.print();
        }
    }
    
    function exifData()
    {
        exifWin=window.open('/webfilesys/servlet?command=exifData&amp;imgFile=<xsl:value-of select="/imageData/encodedPath" />','exifWin','scrollbars=yes,status=no,toolbar=no,location=no,menu=no,width=400,height=480,left=200,top=100,screenX=200,screenY=100,resizable=no');
        exifWin.focus();
    }
    
    function rate()
    {
        if (navigator.appName=='Netscape')
        {
            windowWidth = window.innerWidth;
            windowHeight = window.innerHeight;
        }    
        else
        {
            windowWidth = document.body.clientWidth;
            windowHeight = document.body.clientHeight;
        }

        document.form1.windowWidth.value = windowWidth;
        document.form1.windowHeight.value = windowHeight;
        
        document.form1.submit();
    }
  
    function addComment()
    {
        if (navigator.appName=='Netscape')
        {
            windowWidth = window.innerWidth;
            windowHeight = window.innerHeight;
        }    
        else
        {
            windowWidth = document.body.clientWidth;
            windowHeight = document.body.clientHeight;
        }
        
        document.form2.windowWidth.value = windowWidth;
        document.form2.windowHeight.value = windowHeight;
        
        document.form2.submit();
    }
  
    function limitText()
    {
        if (document.form2.newComment.value.length > 2048)
        {  
            document.form2.newComment.value=document.form1.newComment.value.substring(0,2048);
        }
    }
    
    <xsl:if test="/imageData/geoTag">
    
      function showGoogleMap()
      {
         mapWin=window.open('/webfilesys/servlet?command=googleMap&amp;path=<xsl:value-of select="/imageData/encodedPath" />','mapWin','status=no,toolbar=no,location=no,menu=no,width=600,height=400,resizable=yes,left=20,top=20,screenX=20,screenY=20');
         mapWin.focus();
      }
    </xsl:if>
    
    function showOriginalSize()
    {
        var detailWin = window.open('<xsl:value-of select="/imageData/imageSource" />', 'detailWin')
        detailWin.focus();
    }
    
  </script>
  
</head>

<body topmargin="0" marginheight="0" leftmargin="0" marginwidth="0">
  <xsl:if test="/imageData/voteAccepted">
    <xsl:attribute name="onload">showMsgCentered('<xsl:value-of select="/imageData/resources/msg[@key='rating.confirm']/@value" />', 260, 100, 4000);</xsl:attribute>
  </xsl:if>

  <div id="toolTip" style="position:absolute;top:200px;left:100px;width=200px;height=20px;padding:5px;background-color:ivory;border-style:solid;border-width:1px;border-color:#000000;visibility:hidden"></div>

  <xsl:apply-templates />

</body>
</html>

<div id="msg1" class="msgBox" style="visibility:hidden;position:absolute;top:0px;left:0px;" />

</xsl:template>
<!-- end root node-->

<!-- ############################## path ################################ -->

<xsl:template name="currentPath">

  <table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
      <td>
      
        <div class="albumPath">
          <xsl:for-each select="pathElem">
            <a class="dirtree">
              <xsl:attribute name="href"><xsl:value-of select="concat('/webfilesys/servlet?command=album&amp;relPath=',@path)"/></xsl:attribute>
              <xsl:value-of select="@name"/> 
            </a>
            <font class="fixed"><b> &gt; </b></font>
          </xsl:for-each>
    
          <a class="dirtree" href="#">
            <xsl:value-of select="/imageData/imageName"/> 
          </a>
    
        </div>
        
      </td>
      
      <td align="right">
        <a href="#" onclick="window.open('/webfilesys/servlet?command=versionInfo','infowindow','status=no,toolbar=no,location=no,menu=no,width=300,height=220,resizable=no,left=250,top=150,screenX=250,screenY=150')">
          <img src="images/infoSmall.gif" border="0" width="18" height="18">
            <xsl:attribute name="title"><xsl:value-of select="/imageData/resources/msg[@key='label.about']/@value" /></xsl:attribute>
          </img></a>
      </td>
      
    </tr>
  </table>
  
</xsl:template>

<!-- ############################## end path ################################ -->

<xsl:template match="imageData">

  <table border="0" width="100%" cellpadding="2" cellspacing="0">
    <tr>
      <th class="headline">
        <xsl:value-of select="resources/msg[@key='label.albumTitle']/@value" />:
        <xsl:value-of select="userid" />
        (<xsl:value-of select="imageName" />)
      </th>
    </tr>
  </table>

  <xsl:for-each select="currentPath">
    <xsl:call-template name="currentPath" />
  </xsl:for-each>
 
  <center>
    
    <div width="100%" style="border-style:solid;border-color:black;border-width:1px;padding:5px;background-color:ivory;">
    
      <a href="javascript:showOriginalSize()">
        <img border="0" class="thumb">
            <xsl:attribute name="src"><xsl:value-of select="imageSource" /></xsl:attribute>
            <xsl:attribute name="width"><xsl:value-of select="displayWidth" /></xsl:attribute>
            <xsl:attribute name="height"><xsl:value-of select="displayHeight" /></xsl:attribute>
            <xsl:attribute name="title"><xsl:value-of select="resources/msg[@key='showPictureOrigSize']/@value" /></xsl:attribute>
        </img>
      </a>

      <!-- ################# rating ############### -->

      <form accept-charset="utf-8" name="form1" method="get" action="/webfilesys/servlet" style="margin:0px;padding:0px;padding-top:4px;">
        <input type="hidden" name="command" value="rate" />
        <input type="hidden" name="imagePath">
          <xsl:attribute name="value"><xsl:value-of select="imagePath" /></xsl:attribute>
        </input>
        <input type="hidden" name="relPath">
          <xsl:attribute name="value"><xsl:value-of select="relativePath" /></xsl:attribute>
        </input>
        <input type="hidden" name="imgName">
          <xsl:attribute name="value"><xsl:value-of select="imageName" /></xsl:attribute>
        </input>
        <input type="hidden" name="windowWidth" value="" />
        <input type="hidden" name="windowHeight" value="" />
      
        <table border="0" cellpadding="0" cellspacing="0">
          <xsl:if test="displayWidth &gt; 299">
            <xsl:attribute name="width"><xsl:value-of select="displayWidth" /></xsl:attribute>
          </xsl:if>
          <xsl:if test="displayWidth &lt; 300">
            <xsl:attribute name="width">300</xsl:attribute>
          </xsl:if>

          <xsl:if test="description">
            <tr>
              <td class="story" colspan="2">
                <xsl:value-of select="description" />
              </td>
            </tr>
          </xsl:if>
      
          <tr>
            <td colspan="2">&#160;</td>
          </tr>

          <tr>
            <td valign="middle" width="90%">
              <table border="0" cellspacing="0" cellpadding="0">

                <tr>
                  <td class="plaintext" nowrap="nowrap" style="padding-right:4px">
                    <xsl:value-of select="/imageData/resources/msg[@key='rating.visitor']/@value" /><xsl:if test="voteCount"> (<xsl:value-of select="voteCount" />)</xsl:if>:
                  </td>
                  <td class="plaintext" nowrap="nowrap">
                    <xsl:if test="visitorRating">
                      <xsl:if test="visitorRating = 5">
                        <img src="images/5-stars.gif" border="0" />
                      </xsl:if>
                      <xsl:if test="visitorRating = 4">
                        <img src="images/4-stars.gif" border="0" />
                      </xsl:if>
                      <xsl:if test="visitorRating = 3">
                        <img src="images/3-stars.gif" border="0" />
                      </xsl:if>
                      <xsl:if test="visitorRating = 2">
                        <img src="images/2-stars.gif" border="0" />
                      </xsl:if>
                      <xsl:if test="visitorRating = 1">
                        <img src="images/1-stars.gif" border="0" />
                      </xsl:if>
                    </xsl:if>
                    <xsl:if test="not(visitorRating)">
                      <xsl:value-of select="/imageData/resources/msg[@key='rating.notYetRated']/@value" />
                    </xsl:if>
                  </td>
                  
                  <xsl:if test="not(readonly) or (readonly = 'false') or (ratingAllowed and (ratingAllowed = 'true'))">

                    <td>&#160;&#160;</td>
                  
                    <td align="center" valign="top">
                      <select name="rating">
                        <xsl:attribute name="onChange">javascript:rate()</xsl:attribute>
                        <option value="-1">-- <xsl:value-of select="/imageData/resources/msg[@key='rating.rateNow']/@value" /> --</option>
                        <option value="1"><xsl:value-of select="/imageData/resources/msg[@key='rating.1star']/@value" /></option>
                        <option value="2"><xsl:value-of select="/imageData/resources/msg[@key='rating.2stars']/@value" /></option>
                        <option value="3"><xsl:value-of select="/imageData/resources/msg[@key='rating.3stars']/@value" /></option>
                        <option value="4"><xsl:value-of select="/imageData/resources/msg[@key='rating.4stars']/@value" /></option>
                        <option value="5"><xsl:value-of select="/imageData/resources/msg[@key='rating.5stars']/@value" /></option>
                      </select>
                    </td>
                  </xsl:if>
                  
                </tr>

              </table>
            </td>
            
            <td align="right" valign="top" nowrap="nowrap">
              <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                
                  <xsl:if test="/imageData/geoTag">
                   
                    <td style="padding-right:10px">
                      <select id="geoLocSel" style="width:150px;display:none">
                        <xsl:attribute name="onchange">geoMapFileSelected('<xsl:value-of select="/imageData/pathForScript" />')</xsl:attribute>
                        <option value="0"><xsl:value-of select="/imageData/resources/msg[@key='selectMapType']/@value" /></option>
                        <option value="1"><xsl:value-of select="/imageData/resources/msg[@key='mapTypeOSM']/@value" /></option>
                        <xsl:if test="/imageData/googleMaps">
                          <option value="2"><xsl:value-of select="/imageData/resources/msg[@key='mapTypeGoogleMap']/@value" /></option>
                        </xsl:if>
                        <option value="3"><xsl:value-of select="/imageData/resources/msg[@key='mapTypeGoogleEarth']/@value" /></option>
                      </select>
                    </td>

                    <td id="mapIcon" style="vertical-align:top;padding-right:10px;">
                      <a href="javascript:showMapSelection()">
                        <img src="/webfilesys/images/geoTag.gif" width="30" height="30" border="0">
                          <xsl:attribute name="title"><xsl:value-of select="/imageData/resources/msg[@key='label.geoMapLink']/@value" /></xsl:attribute>
                        </img>
                      </a>
                    </td>
                   
                  </xsl:if>

                  <td nowrap="nowrap">
                    <a class="button" onclick="this.blur();"> 
                      <xsl:attribute name="href">javascript:window.location.href='/webfilesys/servlet?command=album';</xsl:attribute>
                      <span><xsl:value-of select="/imageData/resources/msg[@key='button.returnToAlbum']/@value" /></span>
                    </a>
                  </td>
                  
                </tr>
              </table>              
            </td>
            
          </tr>
        </table>
        
      </form>
      
      <br />
      
      <!-- ################# comments ############### -->
      
      <form accept-charset="utf-8" name="form2" method="post" action="/webfilesys/servlet" style="margin:0;padding:0;">

        <input type="hidden" name="command" value="addComment" />
      
        <input type="hidden" name="actPath">
          <xsl:attribute name="value"><xsl:value-of select="imagePath" /></xsl:attribute>
        </input>
        <input type="hidden" name="imgName">
          <xsl:attribute name="value"><xsl:value-of select="imageName" /></xsl:attribute>
        </input>
        <input type="hidden" name="windowWidth" value="" />
        <input type="hidden" name="windowHeight" value="" />
      
        <table border="0" cellpadding="0" cellspacing="0" style="border-style:solid;border-color:#808080;border-width:1px;">
          <xsl:if test="displayWidth &gt; 299">
            <xsl:attribute name="width"><xsl:value-of select="displayWidth" /></xsl:attribute>
          </xsl:if>
          <xsl:if test="displayWidth &lt; 300">
            <xsl:attribute name="width">300</xsl:attribute>
          </xsl:if>

          <tr>
            <td class="formParm1" style="font-weight:bold">
              <xsl:attribute name="href">javascript:comments2('<xsl:value-of select="pathForScript" />')</xsl:attribute>
              <xsl:if test="comments">
                <xsl:value-of select="comments/@count" />&#160;
              </xsl:if>
              <xsl:if test="not(comments)">
                0
              </xsl:if>
              <xsl:value-of select="/imageData/resources/msg[@key='label.comments']/@value" />
              <xsl:if test="comments">:</xsl:if>
            </td>
          </tr>

          <xsl:if test="comments">
            
            <tr><td class="formParm2">&#160;</td></tr>
            
            <xsl:for-each select="comments/comment">
              <tr>
                <td class="formParm1">
                  <xsl:value-of select="user" />
                  (<xsl:value-of select="date" />)
                </td>
              </tr>
              
              <tr>
                <td class="formParm2">
                  <xsl:value-of select="msg" />
                </td>
              </tr>
    
              <tr><td class="formParm1">&#160;</td></tr>
    
            </xsl:for-each>
          </xsl:if>
          
          <xsl:if test="addCommentsAllowed">
          
            <tr>
              <td class="formParm1">
                <xsl:value-of select="resources/msg[@key='label.addcomment']/@value" />:
              </td>
            </tr>

            <tr>
              <td class="formParm2">
                <textarea name="newComment" cols="100" rows="4" wrap="virtual" style="width:100%" onKeyup="limitText()" onChange="limitText()"></textarea>
	      </td>
	    </tr>

            <tr>
              <td class="formParm1">
                <xsl:value-of select="resources/msg[@key='label.commentAuthor']/@value" />:
                &#160;
                <input type="text" name="author" style="width:150px" />
	      </td>
	    </tr>
	  
            <tr>
              <td class="formParm1" style="padding-top:10px;padding-bottom:10px">
                <a class="button" onclick="this.blur();"> 
                  <xsl:attribute name="href">javascript:addComment();</xsl:attribute>
                  <span><xsl:value-of select="resources/msg[@key='button.addComment']/@value" /></span>
                </a>              
              </td>
            </tr>

          </xsl:if>

        </table>
      </form>
      
    </div>
  
  </center>
  <br/>
</xsl:template>

</xsl:stylesheet>
