<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="imageData" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <title><xsl:value-of select="/imageData/relativePath" /></title>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/imageData/css" />.css</xsl:attribute>
  </link>

  <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
  <script src="javascript/titleToolTip.js" type="text/javascript" />
  <script src="javascript/jsFileMenu.js" type="text/javascript" />

  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/imageData/language" /></xsl:attribute>
  </script>

  <xsl:if test="/imageData/geoTag">
    <script src="/webfilesys/javascript/geoMap.js" type="text/javascript"></script>
  </xsl:if>

  <script language="JavaScript">
    function deleteSelf()
    {
        if (confirm(resourceBundle["confirm.delfile"]))
        {
            location.href='/webfilesys/servlet?command=delImage&amp;imgName=<xsl:value-of select="/imageData/encodedPath" />';
        }
    }

    function printPage() 
    {
        if (confirm(resourceBundle["confirm.print"]))
        {
            window.print();
        }
    }
    
    function exifData()
    {
        exifWin=window.open('/webfilesys/servlet?command=exifData&amp;imgFile=<xsl:value-of select="/imageData/encodedPath" />','exifWin','scrollbars=yes,status=no,toolbar=no,location=no,menu=no,width=400,height=480,left=200,top=100,screenX=200,screenY=100,resizable=no');
        exifWin.focus();
    }
    
    function scale()
    {
        scaleWin = window.open('/webfilesys/servlet?command=resizeParms&amp;imgFile=<xsl:value-of select="/imageData/encodedPath" />&amp;popup=true','resizeWin' + (new Date()).getTime(),'status=no,toolbar=no,menu=no,width=400,height=560,resizable=no,scrollbars=yes,screenX=180,screenY=20,left=180,top=20');
        scaleWin.focus();
    }
    
    function rate()
    {
        document.form1.submit();
    }
    
    function actionSelected()
    {
        if (document.form1.imgAction[document.form1.imgAction.selectedIndex].value == '5')
        {
            comments('<xsl:value-of select="/imageData/pathForScript" />');
        }
        else if (document.form1.imgAction[document.form1.imgAction.selectedIndex].value == '1')
        {
            deleteSelf();
        }
        else if (document.form1.imgAction[document.form1.imgAction.selectedIndex].value == '3')
        {
            exifData();
        }
        else if (document.form1.imgAction[document.form1.imgAction.selectedIndex].value == '2')
        {
            scale();
        }
        else if (document.form1.imgAction[document.form1.imgAction.selectedIndex].value == '4')
        {
            printPage();
        }
        else if (document.form1.imgAction[document.form1.imgAction.selectedIndex].value == '0')
        {
            detailWin = window.open('<xsl:value-of select="/imageData/imageSource" />', 'detailWin')
            detailWin.focus();
        }
        
        document.form1.imgAction.selectedIndex = 0;
    }
    
  </script>
  
  <script language="JavaScript" src="javascript/fileMenu.js" type="text/javascript"></script>


</head>

<body style="margin:0px;border:0px;">

  <div id="toolTip" style="position:absolute;top:200px;left:100px;width=200px;height=20px;padding:5px;background-color:ivory;border-style:solid;border-width:1px;border-color:#000000;visibility:hidden"></div>

  <xsl:apply-templates />

</body>

<script type="text/javascript">
  setBundleResources();
</script>

</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="imageData">

  <center>
    
    <div width="100%" style="padding:0px">
    
      <img border="0" class="thumb">
        <xsl:attribute name="src"><xsl:value-of select="imageSource" /></xsl:attribute>
        <xsl:attribute name="width"><xsl:value-of select="displayWidth" /></xsl:attribute>
        <xsl:attribute name="height"><xsl:value-of select="displayHeight" /></xsl:attribute>
        <xsl:if test="description">
          <xsl:attribute name="onMouseOver">showToolTip('<xsl:value-of select="description" />')</xsl:attribute>
          <xsl:attribute name="onMouseOut">hideToolTip()</xsl:attribute>
        </xsl:if>
      </img>
      
      <form accept-charset="utf-8" name="form1" method="get" action="/webfilesys/servlet" style="margin:0px;padding:0px;padding-top:4px;">
        <input type="hidden" name="command" value="rate" />
        <input type="hidden" name="imagePath">
          <xsl:attribute name="value"><xsl:value-of select="imagePath" /></xsl:attribute>
        </input>
      
        <table border="0" cellpadding="0" cellspacing="0">
          <xsl:if test="displayWidth &gt; 299">
            <xsl:attribute name="width"><xsl:value-of select="displayWidth" /></xsl:attribute>
          </xsl:if>
          <xsl:if test="displayWidth &lt; 300">
            <xsl:attribute name="width">300</xsl:attribute>
          </xsl:if>
          <tr>
            <td valign="top" style="padding-right:10px;">
              <table border="0" cellspacing="0" style="padding-top:1px;padding-bottom:1px;">
              
                <xsl:if test="not(readonly) or (readonly = 'false')">
                  <tr>
                    <td class="plaintext" nowrap="nowrap" style="padding-right:4px">
                      <span  resource="rating.owner"></span>:
                    </td>
                    <td class="plaintext" nowrap="nowrap">
                      <xsl:if test="ownerRating">
                        <xsl:if test="ownerRating = 5">
                          <img src="images/5-stars.gif" border="0" />
                        </xsl:if>
                        <xsl:if test="ownerRating = 4">
                          <img src="images/4-stars.gif" border="0" />
                        </xsl:if>
                        <xsl:if test="ownerRating = 3">
                          <img src="images/3-stars.gif" border="0" />
                        </xsl:if>
                        <xsl:if test="ownerRating = 2">
                          <img src="images/2-stars.gif" border="0" />
                        </xsl:if>
                        <xsl:if test="ownerRating = 1">
                          <img src="images/1-stars.gif" border="0" />
                        </xsl:if>
                      </xsl:if>
                      <xsl:if test="not(ownerRating)">
                        <span resource="rating.notYetRated"></span>
                      </xsl:if>
                    </td>
                  </tr>
                </xsl:if>

                <tr>
                  <td class="plaintext" nowrap="nowrap" style="padding-right:4px">
                    <span resource="rating.visitor"></span><xsl:if test="voteCount"> (<xsl:value-of select="voteCount" />)</xsl:if>:
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
                      <span resource="rating.notYetRated"></span>
                    </xsl:if>
                  </td>
                </tr>

              </table>
            </td>
            
            <td align="center" valign="top">
            
              <table border="0" cellspacing="0" cellpadding="0">
                <xsl:if test="not(readonly) or (readonly = 'false') or (ratingAllowed and (ratingAllowed = 'true'))">
                  <tr>
                    <td>
                      <select name="rating">
                        <xsl:attribute name="onChange">javascript:rate()</xsl:attribute>
                        <option value="-1">-- <span resource="rating.rateNow"></span> --</option>
                        <option value="1" resource="rating.1star" />
                        <option value="2" resource="rating.2stars" />
                        <option value="3" resource="rating.3stars" />
                        <option value="4" resource="rating.4stars" />
                        <option value="5" resource="rating.5stars" />
                      </select>
                    </td>
                  </tr>
                </xsl:if>
            
                <tr>
                  <td nowrap="nowrap">
                    <a class="dirtree">
                      <xsl:attribute name="href">javascript:comments('<xsl:value-of select="pathForScript" />')</xsl:attribute>
                      <xsl:if test="commentCount">
                        <xsl:value-of select="commentCount" />&#160;
                      </xsl:if>
                      <xsl:if test="not(commentCount)">
                        0
                      </xsl:if>
                      <span resource="label.comments"></span>
                    </a>
                  </td>
                </tr>
              </table>
            </td>
            
            <td width="45%" style="padding-right:10px">
              &#160;
            </td>
            
            <xsl:if test="/imageData/geoTag">
            
              <td valign="top">
                <select id="geoLocSel" style="width:150px;display:none">
                  <xsl:attribute name="onchange">geoMapFileSelected('<xsl:value-of select="/imageData/pathForScript" />')</xsl:attribute>
                  <option value="0" resource="selectMapType" />
                  <option value="1" resource="mapTypeOSM" />
                  <xsl:if test="/imageData/googleMaps">
                    <option value="2" resource="mapTypeGoogleMap" />
                  </xsl:if>
                  <option value="3" resource="mapTypeGoogleEarth" />
                </select>
              </td>

              <td id="mapIcon" valign="top">
                <a href="javascript:showMapSelection()">
                  <img src="/webfilesys/images/geoTag.gif" width="30" height="30" border="0" titleResource="label.geoMapLink" />
                </a>
              </td>
            
            </xsl:if>
            
            <td width="45%" style="padding-right:10px">
              &#160;
            </td>
            
            <td align="right" valign="top">
              <table border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td align="right" class="plaintext" nowrap="nowrap">
                    <span resource="label.picture"></span>
                    <xsl:text>: </xsl:text>
                    <select name="imgAction">
                      <xsl:attribute name="onChange">actionSelected()</xsl:attribute>
                      <option value="-1" resource="label.selectFunction" />
                      <xsl:if test="scaled">
                        <option value="0" resource="label.origSize" />
                      </xsl:if>
                      <xsl:if test="not(readonly) or (readonly = 'false')">
                        <option value="1" resource="label.delete" />
                        <option value="2" resource="label.editPicture" />
                      </xsl:if>
                      <xsl:if test="imageType = '1'">
                        <option value="3" resource="alt.cameradata" />
                      </xsl:if>
                      <option value="4" resource="alt.printpict" />
                      <option value="5" resource="label.comments" />
                    </select>
                   
                  </td>
                </tr>
                
                <tr>
                  <td align="right" class="plaintext" nowrap="nowrap">              
                    <xsl:value-of select="imageWidth" /> x <xsl:value-of select="imageHeight" /> pix
                  </td>
                </tr>
                
              </table>

            </td>
          </tr>
        </table>
        
      </form>
      
    </div>
  
  </center>
</xsl:template>

</xsl:stylesheet>
