<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="emojiList" />

<!-- root node-->
<xsl:template match="/">

  <xsl:if test="/emojiList/emoji">
    
    <xsl:for-each select="/emojiList/emoji">

      <div class="selectEmoji">
        <a class="selectEmoji">
          <xsl:attribute name="href">javascript:insertEmoji('<xsl:value-of select="/emojiList/textareaId" />', '<xsl:value-of select="." />')</xsl:attribute>
          <img class="blogEmoticon" titleResource="blog.insertEmoji">
            <xsl:attribute name="src">/webfilesys/emoticons/<xsl:value-of select="." />.png</xsl:attribute> 
          </img>
        </a>
      </div>

    </xsl:for-each>
    
  </xsl:if>  

</xsl:template>
<!-- end root node-->

</xsl:stylesheet>
