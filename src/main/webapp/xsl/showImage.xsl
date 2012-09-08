<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="imageData" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <title><xsl:value-of select="/imageData/relativePath" /></title>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/imageData/css" />.css</xsl:attribute>
  </link>

  <script language="JavaScript" src="javascript/titleToolTip.js" type="text/javascript" />
  <script language="JavaScript" src="javascript/jsFileMenu.js" type="text/javascript" />

  <xsl:if test="/imageData/geoTag">
    <script src="/webfilesys/javascript/geoMap.js" type="text/javascript"></script>
  </xsl:if>

  <script language="JavaScript">
    function deleteSelf()
    {
        if (confirm("<xsl:value-of select="/imageData/resources/msg[@key='confirm.delfile']/@value" />"))
        {
            location.href='/webfilesys/servlet?command=delImage&amp;imgName=<xsl:value-of select="/imageData/encodedPath" />';
        }
    }

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
                      <xsl:value-of select="/imageData/resources/msg[@key='rating.owner']/@value" />:
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
                        <xsl:value-of select="/imageData/resources/msg[@key='rating.notYetRated']/@value" />
                      </xsl:if>
                    </td>
                  </tr>
                </xsl:if>

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
                        <option value="-1">-- <xsl:value-of select="/imageData/resources/msg[@key='rating.rateNow']/@value" /> --</option>
                        <option value="1"><xsl:value-of select="/imageData/resources/msg[@key='rating.1star']/@value" /></option>
                        <option value="2"><xsl:value-of select="/imageData/resources/msg[@key='rating.2stars']/@value" /></option>
                        <option value="3"><xsl:value-of select="/imageData/resources/msg[@key='rating.3stars']/@value" /></option>
                        <option value="4"><xsl:value-of select="/imageData/resources/msg[@key='rating.4stars']/@value" /></option>
                        <option value="5"><xsl:value-of select="/imageData/resources/msg[@key='rating.5stars']/@value" /></option>
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
                      <xsl:value-of select="/imageData/resources/msg[@key='label.comments']/@value" />
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
                  <option value="0"><xsl:value-of select="/imageData/resources/msg[@key='selectMapType']/@value" /></option>
                  <option value="1"><xsl:value-of select="/imageData/resources/msg[@key='mapTypeOSM']/@value" /></option>
                  <xsl:if test="/imageData/googleMaps">
                    <option value="2"><xsl:value-of select="/imageData/resources/msg[@key='mapTypeGoogleMap']/@value" /></option>
                  </xsl:if>
                  <option value="3"><xsl:value-of select="/imageData/resources/msg[@key='mapTypeGoogleEarth']/@value" /></option>
                </select>
              </td>

              <td id="mapIcon" valign="top">
                <a href="javascript:showMapSelection()">
                  <img src="/webfilesys/images/geoTag.gif" width="30" height="30" border="0">
                    <xsl:attribute name="title"><xsl:value-of select="/imageData/resources/msg[@key='label.geoMapLink']/@value" /></xsl:attribute>
                  </img>
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
            
                    <xsl:value-of select="/imageData/resources/msg[@key='label.picture']/@value" />: 
                    <select name="imgAction">
                      <xsl:attribute name="onChange">actionSelected()</xsl:attribute>
                      <option value="-1"><xsl:value-of select="/imageData/resources/msg[@key='label.selectFunction']/@value" /></option>
                      <xsl:if test="scaled">
                        <option value="0"><xsl:value-of select="/imageData/resources/msg[@key='label.origSize']/@value" /></option>
                      </xsl:if>
                      <xsl:if test="not(readonly) or (readonly = 'false')">
                        <option value="1"><xsl:value-of select="/imageData/resources/msg[@key='label.delete']/@value" /></option>
                        <option value="2"><xsl:value-of select="/imageData/resources/msg[@key='label.editPicture']/@value" /></option>
                      </xsl:if>
                      <xsl:if test="imageType = '1'">
                        <option value="3"><xsl:value-of select="/imageData/resources/msg[@key='alt.cameradata']/@value" /></option>
                      </xsl:if>
                      <option value="4"><xsl:value-of select="/imageData/resources/msg[@key='alt.printpict']/@value" /></option>
                      <option value="5"><xsl:value-of select="/imageData/resources/msg[@key='label.comments']/@value" /></option>
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
