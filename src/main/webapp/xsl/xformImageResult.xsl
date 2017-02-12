<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:template match="/file">

  <div class="thumbnailCont">
    <xsl:attribute name="id">thumbCont-<xsl:value-of select="nameForId" /></xsl:attribute>
    
    <a>
      <xsl:attribute name="id">thumb-<xsl:value-of select="@id" /></xsl:attribute>

      <xsl:attribute name="href">javascript:showImage('<xsl:value-of select="pathForScript" />');hidePopupPicture()</xsl:attribute>

      <xsl:attribute name="oncontextmenu">picturePopupInFrame('<xsl:value-of select="pathForScript" />', '<xsl:value-of select="@id" />');return false;</xsl:attribute>
                  
      <img class="thumb">
        <xsl:attribute name="id">pic-<xsl:value-of select="@id" /></xsl:attribute>
        <xsl:attribute name="src"><xsl:value-of select="imgSrcPath" /></xsl:attribute>
        <xsl:attribute name="width"><xsl:value-of select="thumbWidth" /></xsl:attribute>
        <xsl:attribute name="height"><xsl:value-of select="thumbHeight" /></xsl:attribute>
        <xsl:attribute name="origWidth"><xsl:value-of select="xpix" /></xsl:attribute>
        <xsl:attribute name="origHeight"><xsl:value-of select="ypix" /></xsl:attribute>
        <xsl:attribute name="imgPath"><xsl:value-of select="imgPath" /></xsl:attribute>
        <xsl:attribute name="imgType"><xsl:value-of select="imgType" /></xsl:attribute>
        <xsl:if test="description">
          <xsl:attribute name="title"><xsl:value-of select="description" /></xsl:attribute>
        </xsl:if>
      </img>
    </a>
    
    <br/>
                
    <input type="checkbox" class="cb2">
      <xsl:attribute name="name">list-<xsl:value-of select="@name" /></xsl:attribute>
    </input>
              
    <a class="fn">
      <xsl:attribute name="id">fileName-<xsl:value-of select="@id" /></xsl:attribute>
      <xsl:attribute name="href">javascript:picContextMenu('<xsl:value-of select="@nameForScript" />','<xsl:value-of select="@id" />')</xsl:attribute>
      <xsl:attribute name="oncontextmenu">picturePopupInFrame('<xsl:value-of select="pathForScript" />', '<xsl:value-of select="@id" />');return false;</xsl:attribute>
      <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
      
      <xsl:value-of select="displayName" />
    </a>
              
    <div>
      <xsl:value-of select="@lastModified" />
    </div>
                
    <div>
      <xsl:value-of select="@size" /> KB
      &#160;
      <span>
        <xsl:attribute name="id">pixDim-<xsl:value-of select="@id" /></xsl:attribute>
        <xsl:attribute name="picFileName"><xsl:value-of select="@name" /></xsl:attribute>
        <xsl:value-of select="xpix" />
        <xsl:text> </xsl:text>
        x
        <xsl:text> </xsl:text>
        <xsl:value-of select="ypix" />
        <xsl:text> </xsl:text>
        px
      </span>
    </div>
               
    <div>
      <xsl:value-of select="comments" />
      <xsl:text> </xsl:text>
      <xsl:value-of select="resources/msg[@key='label.comments']/@value" />

      <xsl:if test="ownerRating or visitorRating">
        &#160;
        <a class="dirtree">
          <xsl:attribute name="title">
            <xsl:if test="ownerRating">Rating by Owner: <xsl:value-of select="ownerRating" /><xsl:if test="visitorRating"> / </xsl:if></xsl:if>
            <xsl:if test="visitorRating">Rating by <xsl:value-of select="numberOfVotes" /> Visitors: <xsl:value-of select="visitorRating" /></xsl:if> (5 = best)
          </xsl:attribute>
          <img src="images/star.gif" border="0" style="vertical-align:bottom" />
          <xsl:if test="ownerRating">
            <xsl:value-of select="ownerRating" />
            <xsl:if test="visitorRating">/</xsl:if>
          </xsl:if>
          <xsl:if test="visitorRating">
            <xsl:value-of select="visitorRating" />
          </xsl:if>
        </a>
        <xsl:if test="visitorRating">
          (<xsl:value-of select="numberOfVotes" />)
        </xsl:if>
      </xsl:if>
    </div>
                
  </div>
            
</xsl:template>

</xsl:stylesheet>
