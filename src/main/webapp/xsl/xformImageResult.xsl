<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="xml" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:template match="/">

  <div class="thumbnailCont">
    <xsl:attribute name="id">thumbCont-<xsl:value-of select="file/nameForId" /></xsl:attribute>
    
    <a>
      <xsl:attribute name="id">thumb-<xsl:value-of select="file/@id" /></xsl:attribute>

      <xsl:attribute name="href">javascript:showImgFromThumb('<xsl:value-of select="file/pathForScript" />');hidePopupPicture()</xsl:attribute>

      <xsl:attribute name="oncontextmenu">picturePopupInFrame('<xsl:value-of select="file/pathForScript" />', '<xsl:value-of select="file/file/@id" />');return false;</xsl:attribute>
                  
      <img class="thumb">
        <xsl:attribute name="id">pic-<xsl:value-of select="file/@id" /></xsl:attribute>
        <xsl:attribute name="src"><xsl:value-of select="file/imgSrcPath" /></xsl:attribute>
        <xsl:attribute name="width"><xsl:value-of select="file/thumbWidth" /></xsl:attribute>
        <xsl:attribute name="height"><xsl:value-of select="file/thumbHeight" /></xsl:attribute>
        <xsl:attribute name="origWidth"><xsl:value-of select="file/xpix" /></xsl:attribute>
        <xsl:attribute name="origHeight"><xsl:value-of select="file/ypix" /></xsl:attribute>
        <xsl:attribute name="imgType"><xsl:value-of select="file/imgType" /></xsl:attribute>
        <xsl:if test="file/description">
          <xsl:attribute name="title"><xsl:value-of select="file/description" /></xsl:attribute>
        </xsl:if>
      </img>
    </a>
    
    <br/>
                
    <input type="checkbox" class="big">
      <xsl:attribute name="name">list-<xsl:value-of select="file/@name" /></xsl:attribute>
    </input>
              
    <a class="fn">
      <xsl:attribute name="id">fileName-<xsl:value-of select="file/@id" /></xsl:attribute>
      <xsl:attribute name="href">javascript:picContextMenu('<xsl:value-of select="file/@nameForScript" />','<xsl:value-of select="file/@id" />')</xsl:attribute>
      <xsl:attribute name="oncontextmenu">picturePopupInFrame('<xsl:value-of select="file/pathForScript" />', '<xsl:value-of select="file/@id" />');return false;</xsl:attribute>
      <xsl:attribute name="title"><xsl:value-of select="file/@name" /></xsl:attribute>
      
      <xsl:value-of select="file/displayName" />
    </a>
              
    <div>
      <xsl:value-of select="file/@lastModified" />
    </div>
                
    <div>
      <xsl:value-of select="file/@size" /> KB
      &#160;
      <span>
        <xsl:attribute name="id">pixDim-<xsl:value-of select="file/@id" /></xsl:attribute>
        <xsl:attribute name="picFileName"><xsl:value-of select="file/@name" /></xsl:attribute>
        <xsl:value-of select="file/xpix" />
        <xsl:text> </xsl:text>
        x
        <xsl:text> </xsl:text>
        <xsl:value-of select="file/ypix" />
        <xsl:text> </xsl:text>
        px
      </span>
    </div>

    <xsl:if test="file/comments != '0' or file/ownerRating or file/visitorRating">
      <div>
        <xsl:value-of select="file/comments" />
        <xsl:text> </xsl:text>
        <xsl:value-of select="file/resources/msg[@key='label.comments']/@value" />

        <xsl:if test="file/ownerRating or file/visitorRating">
          &#160;
          <a class="dirtree">
            <xsl:attribute name="title">
              <xsl:if test="file/ownerRating">Rating by Owner: <xsl:value-of select="file/ownerRating" /><xsl:if test="file/visitorRating"> / </xsl:if></xsl:if>
              <xsl:if test="file/visitorRating">Rating by <xsl:value-of select="file/numberOfVotes" /> Visitors: <xsl:value-of select="file/visitorRating" /></xsl:if> (5 = best)
            </xsl:attribute>
            <img src="images/star.gif" border="0" style="vertical-align:bottom" />
            <xsl:if test="file/ownerRating">
              <xsl:value-of select="file/ownerRating" />
              <xsl:if test="file/visitorRating">/</xsl:if>
            </xsl:if>
            <xsl:if test="file/visitorRating">
              <xsl:value-of select="file/visitorRating" />
            </xsl:if>
          </a>
          <xsl:if test="file/visitorRating">
            (<xsl:value-of select="file/numberOfVotes" />)
          </xsl:if>
        </xsl:if>
      </div>
    </xsl:if>
                
  </div>
            
</xsl:template>

</xsl:stylesheet>
