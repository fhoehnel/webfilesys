<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<!-- root node-->
<xsl:template match="/">

<html>
  <head>

    <meta http-equiv="expires" content="0" />

    <title resource="label.resizetitle"></title>
  
    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
    <link rel="stylesheet" type="text/css">
      <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/resizeParams/css" />.css</xsl:attribute>
    </link>

    <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/resizeImage.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/jquery/jquery.min.js"></script>
    <script src="/webfilesys/javascript/jcrop/jquery.Jcrop.js"></script>

    <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/resizeParams/language" /></xsl:attribute>
    </script>

    <xsl:if test="/resizeParams/cropEnabled">
      <script src="/webfilesys/javascript/crop.js" type="text/javascript"></script>
    </xsl:if>
  
    <script type="text/javascript">
      var thumbnailWidth = <xsl:value-of select="/resizeParams/thumbnailWidth" />
      var thumbnailHeight = <xsl:value-of select="/resizeParams/thumbnailHeight" />
    </script>
  </head>

  <body class="editPict">
    <div class="headline" resource="label.resizetitle" />
    
    <br/>

    <form accept-charset="utf-8" name="form1" method="get" action="/webfilesys/servlet">
      <input type="hidden" name="command" value="resizeImages" />
      
      <xsl:if test="/resizeParams/popup">
        <input type="hidden" name="popup" value="true" />      
      </xsl:if>
      
      <xsl:if test="/resizeParams/multiImage">
		<input type="hidden" name="actPath">
          <xsl:attribute name="value"><xsl:value-of select="/resizeParams/imageFolderPath" /></xsl:attribute>
        </input>
      </xsl:if>
            
      <xsl:if test="/resizeParams/singleImage">
        <input type="hidden" name="imgFile">
          <xsl:attribute name="value"><xsl:value-of select="/resizeParams/imageFilePath" /></xsl:attribute>
        </input>
      
        <xsl:if test="/resizeParams/cropEnabled">
          <input type="hidden" id="cropAreaLeft" name="cropAreaLeft" value="" />
          <input type="hidden" id="cropAreaTop" name="cropAreaTop" value="" />
          <input type="hidden" id="cropAreaWidth" name="cropAreaWidth" value="" />
          <input type="hidden" id="cropAreaHeight" name="cropAreaHeight" value="" />
        </xsl:if>
        
      </xsl:if>

      <table class="dataForm" border="0" width="100%">
        <tr>
          <td valign="top">

            <xsl:if test="/resizeParams/multiImage">
            
			  <table border="0">
			    <tr>
			      <td class="formParm1"><span resource="label.selectedpictures" /></td>
                </tr>
                
                <tr>
                  <td class="formParm2">
                    <xsl:value-of select="/resizeParams/shortPath" />
                  </td>
                </tr>
              </table>
            
            </xsl:if>

            <xsl:if test="/resizeParams/singleImage">
    
              <table border="0">
                <tr>
                  <td class="formParm1"><span resource="label.picturefile"></span>:</td>
                </tr>
                <tr>
                  <td class="formParm2">
                    <xsl:value-of select="/resizeParams/shortImgPath" />
                  </td>
                </tr>
                
                <xsl:if test="/resizeParams/cropEnabled">
                
                  <tr>
                    <td colspan="2" class="formParm1">
                      <input type="checkbox" name="crop" class="formParm1">
                        <xsl:attribute name="onclick">switchCrop()</xsl:attribute>
                      </input>
                      <span resource="label.cropArea" />
                    </td>
                  </tr>
                  <tr>
                    <td colspan="2" class="formParm1">
                      &#160;&#160;
                      <input type="checkbox" id="cropAreaQuadratic" name="quadratic" class="formParm1" disabled="disabled">
                        <xsl:attribute name="onclick">setCropQuadratic()</xsl:attribute>
                      </input>
                      <span resource="label.cropAreaQuadratic" />
                      &#160;&#160;
                      <input type="checkbox" id="cropAreaKeepAspectRatio" name="keepAspectRation" class="formParm1" disabled="disabled">
                        <xsl:attribute name="onclick">setCropKeepAspectRatio()</xsl:attribute>
                      </input>
                      <span resource="label.cropKeepAspectRatio" />
                    </td>
                  </tr>
                  <tr>
                    <td valign="top" style="padding-top:6px;padding-bottom:6px">
                      <img id="editPicture" class="thumb">
                        <xsl:attribute name="src"><xsl:value-of select="/resizeParams/imgSrc" /></xsl:attribute>
                        <xsl:attribute name="width"><xsl:value-of select="/resizeParams/thumbnailWidth" /></xsl:attribute>
                        <xsl:attribute name="height"><xsl:value-of select="/resizeParams/thumbnailHeight" /></xsl:attribute>
                      </img>
                    </td>
                  </tr>
                
                </xsl:if>
                
              </table>
            
            </xsl:if>
            
            <table border="0" cellspacing="0">
              <tr>
                <td colspan="2">&#160;</td>
              </tr>
              
              <tr>
                <td class="formParm1"><span resource="label.newsize"></span>:</td>
                <td class="formParm2">
                  <select name="newSize" size="1" style="width:140px;" onchange="handleTargetSizeSelection(this)">
                    <option value="0" selected="selected" resource="label.keepOrigSize"></option>
                    <option value="100">100</option>
                    <option value="200">200</option>
                    <option value="320">320</option>
                    <option value="400">400</option>
                    <option value="500">500</option>
                    <option value="640">640</option>
                    <option value="800">800</option>
                    <option value="1024">1024</option>
                    <option value="1280">1280</option>
                    <option value="1600">1600</option>
                    <option value="-1" resource="resizeDifferentSize"></option>
                  </select>
                </td>
              </tr>
              
              <tr id="targetSizeRow" style="display:none">
                <td></td>
                <td class="formParm2">
                  <input id="targetSize" name="targetSize" type="text" style="width:140px;"/>
                </td>
              </tr>

              <tr>
                <td colspan="2">&#160;</td>
              </tr>

              <tr>
                <td class="formParm1"><span resource="label.newformat"></span>:</td>
                <td class="formParm2">
                  <select name="format" size="1">
                    <option value="JPEG">JPEG</option>
                    <option value="PNG">PNG</option>
                    <option value="GIF">GIF</option>
                  </select>
                </td>
              </tr>

              <xsl:if test="/resizeParams/imageType = 1">
                <tr>
                  <td class="formParm1" colspan="2">
                    <input type="checkbox" name="keepExifData" class="formParm1" />
                    <span resource="keepExifData"></span>
                  </td>
                </tr>
              </xsl:if>

              <tr>
                <td colspan="2">
                  <table class="formSection">
                    <tr>
                      <td colspan="2" class="formParm1">
                        <input type="checkbox" name="stampText" class="formParm1" onclick="switchCopyRightFields()" />
                          <span resource="label.addCopyRight"></span>
                      </td>
                    </tr>
                    <tr>
                      <td class="formParm1" colspan="2"><span resource="label.copyRightText"></span>:</td>
                    </tr>
                    <tr>
                      <td class="formParm2" colspan="2">
                        <input type="text" name="copyRightText" maxlength="128" style="width:300px" disabled="disabled" />
                      </td>
                    </tr>
                    <tr>
                      <td class="formParm1"><span resource="label.copyRightPos"></span>:</td>
                      <td class="formParm2">
                        <select name="copyRightPos" size="1" disabled="disabled">
                          <option value="1" resource="label.posUpperLeft"></option>
                          <option value="2" resource="label.posUpperRight"></option>
                          <option value="3" resource="label.posLowerLeft"></option>
                          <option value="4" resource="label.posLowerRight"></option>
                        </select>
                      </td>
                    </tr>

                    <tr>
                      <td class="formParm1"><span resource="label.copyRightColor"></span>:</td>
                      <td class="formParm2">
                        <select name="copyRightColor" size="1" disabled="disabled">
                          <option value="000000" resource="label.colorBlack"></option>
                          <option value="0000ff" resource="label.colorBlue"></option>
                          <option value="ff0000" resource="label.colorRed"></option>
                          <option value="00ff00" resource="label.colorGreen"></option>
                          <option value="ffff00" resource="label.colorYellow"></option>
                          <option value="ffffff" resource="label.colorWhite"></option>
                        </select>
                      </td>
                    </tr>
                    
                    <tr>
                      <td class="formParm1"><span resource="label.copyRightFontSize"></span>:</td>
                      <td class="formParm2">
                        <select name="copyRightFontSize" size="1" disabled="disabled">
                          <option value="10">10</option>
                          <option value="12">12</option>
                          <option value="14">14</option>
                          <option value="16">16</option>
                          <option value="18">18</option>
                          <option value="20" selected="selected">20</option>
                          <option value="22">22</option>
                          <option value="24">24</option>
                          <option value="26">26</option>
                          <option value="28">28</option>
                          <option value="30">30</option>
                        </select>
                      </td>
                    </tr>
                  </table>
              
                </td>
              </tr>

              <tr>
                <td class="formParm1">
                  <input type="submit" name="start" class="formButton" resource="button.start" />
                </td>
                <td class="formParm2" style="float:right">
                  <input type="button" class="formButton" name="cancel" resource="button.cancel">
                    <xsl:if test="/resizeParams/popup">
                      <xsl:attribute name="onclick">self.close()</xsl:attribute>
                    </xsl:if>
                    <xsl:if test="not(/resizeParams/popup)">
                      <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet?command=listFiles'</xsl:attribute>
                    </xsl:if>
                  </input>
                </td>
              </tr>
            </table>

          </td>
        </tr>
      </table>
    </form>
  
  </body>
  
  <script type="text/javascript">
    setBundleResources();
  </script>
  
</html>

</xsl:template>

</xsl:stylesheet>

