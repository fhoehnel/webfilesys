<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8"/>

<xsl:template name="insDoubleBackslash">
  <xsl:param name="string" />
    <xsl:if test="contains($string, '%5C')">
      <xsl:value-of select="substring-before($string, '%5C')" />%5C%5C<xsl:call-template name="insDoubleBackslash"><xsl:with-param name="string"><xsl:value-of select="substring-after($string, '%5C')" /></xsl:with-param></xsl:call-template>
  </xsl:if>
  <xsl:if test="not(contains($string, '%5C'))">
    <xsl:value-of select="$string" />
  </xsl:if>
</xsl:template>

<xsl:template match="/"> 

  <xsl:for-each select="parentFolder">
    <xsl:call-template name="parentFolder" />
  </xsl:for-each>

</xsl:template>

<xsl:template name="parentFolder"> 

  <div class="last">

      <xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>

      <xsl:attribute name="path"><xsl:value-of select="@path" /></xsl:attribute>

      <xsl:variable name="pathForScript"><xsl:call-template name="insDoubleBackslash"><xsl:with-param name="string"><xsl:value-of select="@path" /></xsl:with-param></xsl:call-template></xsl:variable>
      
      <xsl:if test="folder">
        <a>
          <xsl:attribute name="href">javascript:col('<xsl:value-of select="@id" />', '<xsl:value-of select="@lastInLevel='true'" />')</xsl:attribute>

          <xsl:if test="@lastInLevel='true'">
            <img src="/webfilesys/images/minusLast.gif" border="0" width="15" height="17" />
          </xsl:if>
          <xsl:if test="not(@lastInLevel='true')">
            <img src="/webfilesys/images/minusMore.gif" border="0" width="15" height="17" />
          </xsl:if>
        </a>
      </xsl:if>
      
      <xsl:if test="not(folder)">
        <xsl:if test="not(@leaf)">

          <a>
            <xsl:attribute name="href">javascript:exp('<xsl:value-of select="@id" />', '<xsl:value-of select="@lastInLevel='true'" />')</xsl:attribute>

            <xsl:if test="@lastInLevel='true'">
              <img src="/webfilesys/images/plusLast.gif" border="0" width="15" height="17"/>
            </xsl:if>
            <xsl:if test="not(@lastInLevel='true')">
              <img src="/webfilesys/images/plusMore.gif" border="0" width="15" height="17"/>
            </xsl:if>
          </a>

        </xsl:if>
        
        <xsl:if test="@leaf">
          <xsl:if test="@lastInLevel='true'">
            <img src="/webfilesys/images/branchLast.gif" border="0" width="15" height="17" />
          </xsl:if>
          <xsl:if test="not(@lastInLevel='true')">
            <img src="/webfilesys/images/branch.gif" border="0" width="15" height="17" />
          </xsl:if>
        </xsl:if>
      </xsl:if>

    <a>
      <xsl:attribute name="href">javascript:dirContextMenu('<xsl:value-of select="@id" />')</xsl:attribute>
      
      <xsl:if test="@type='drive'">
        <img src="/webfilesys/images/miniDisk.gif" border="0" width="17" height="14">
          <xsl:if test="@label">
            <xsl:attribute name="title"><xsl:value-of select="@label"/></xsl:attribute>
          </xsl:if>
        </img>
      </xsl:if>

      <xsl:if test="@type='floppy'">
        <img src="/webfilesys/images/miniFloppy.gif" border="0" width="18" height="16">
          <xsl:if test="@label">
            <xsl:attribute name="title"><xsl:value-of select="@label"/></xsl:attribute>
          </xsl:if>
        </img>
      </xsl:if>

      <xsl:if test="not(@type)">
        <xsl:if test="@icon">
          <img border="0" width="16" height="16" style="margin-right:1px">
            <xsl:attribute name="src">/webfilesys/icons/<xsl:value-of select="@icon"/></xsl:attribute>
          </img>
        </xsl:if>
        <xsl:if test="not(@icon)">
          <xsl:if test="@current">
            <img src="/webfilesys/images/folder1.gif" border="0" width="17" height="14"/>
          </xsl:if>
          <xsl:if test="not(@current)">
            <img src="/webfilesys/images/folder.gif" border="0" width="17" height="14"/>
          </xsl:if>
        </xsl:if>
      </xsl:if>
    </a>

    <img src="/webfilesys/images/space.gif" border="0" width="4" height="1"/>
    <a>
      <xsl:attribute name="href">javascript:listFiles('<xsl:value-of select="@id"/>')</xsl:attribute>
      <xsl:attribute name="oncontextmenu">dirContextMenu('<xsl:value-of select="@id" />');return false;</xsl:attribute>

      <xsl:if test="@link">
        <xsl:attribute name="class">link</xsl:attribute>

        <xsl:attribute name="title">
          <xsl:value-of select="'--&gt; '"/>
          <xsl:value-of select="@linkDir"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:if test="not(@link)">
        <xsl:attribute name="class">dirtree</xsl:attribute>
        <xsl:if test="@textColor">
          <xsl:attribute name="style">color:<xsl:value-of select="@textColor" /></xsl:attribute>
        </xsl:if>
      </xsl:if>
    
      <xsl:value-of select="@name"/>
    </a>

    <xsl:if test="folder">
      <xsl:if test="@lastInLevel='true'">
        <div class="indent">
          <xsl:for-each select="folder">
            <xsl:call-template name="folder" />
          </xsl:for-each>
        </div>
      </xsl:if>

      <xsl:if test="not(@lastInLevel='true')">
        <div class="indent">
          <div class="more">
            <xsl:for-each select="folder">
              <xsl:call-template name="folder" />
            </xsl:for-each>
          </div>
        </div>
      </xsl:if>
    </xsl:if>
  
  </div>
  
</xsl:template>


<xsl:template name="folder"> 

  <div class="last">

    <xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>

    <xsl:attribute name="path"><xsl:value-of select="@path" /></xsl:attribute>

    <xsl:variable name="pathForScript"><xsl:call-template name="insDoubleBackslash"><xsl:with-param name="string"><xsl:value-of select="@path" /></xsl:with-param></xsl:call-template></xsl:variable>
      
    <xsl:if test="not(@leaf)">

      <a>
         <xsl:attribute name="href">javascript:exp('<xsl:value-of select="@id" />', '<xsl:value-of select="@lastInLevel='true'" />')</xsl:attribute>

         <xsl:if test="position()=last()">
          <img src="/webfilesys/images/plusLast.gif" border="0" width="15" height="17"/>
        </xsl:if>
        <xsl:if test="position()!=last()">
          <img src="/webfilesys/images/plusMore.gif" border="0" width="15" height="17"/>
        </xsl:if>
      </a>

    </xsl:if>
      
    <xsl:if test="@leaf">
      <xsl:if test="position()=last()">
        <img src="/webfilesys/images/branchLast.gif" border="0" width="15" height="17"/>
      </xsl:if>
      <xsl:if test="position()!=last()">
        <img src="/webfilesys/images/branch.gif" border="0" width="15" height="17"/>
      </xsl:if>
    </xsl:if>

    <a>
      <xsl:attribute name="href">javascript:dirContextMenu('<xsl:value-of select="@id" />')</xsl:attribute>

      <xsl:if test="@icon">
        <img border="0" width="16" height="16" style="margin-right:1px">
          <xsl:attribute name="src">/webfilesys/icons/<xsl:value-of select="@icon"/></xsl:attribute>
        </img>
      </xsl:if>
      <xsl:if test="not(@icon)">
        <img src="/webfilesys/images/folder.gif" border="0" width="17" height="14"/>
      </xsl:if>
    </a>

    <img src="/webfilesys/images/space.gif" border="0" width="4" height="1"/>
    <a>
      <xsl:attribute name="href">javascript:listFiles('<xsl:value-of select="@id"/>')</xsl:attribute>
      <xsl:attribute name="oncontextmenu">dirContextMenu('<xsl:value-of select="@id" />');return false;</xsl:attribute>

      <xsl:if test="@link">
        <xsl:attribute name="class">
          <xsl:value-of select="'link'"/>
        </xsl:attribute>

        <xsl:attribute name="title">
          <xsl:value-of select="'--&gt; '"/>
          <xsl:value-of select="@linkDir"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:if test="not(@link)">
        <xsl:attribute name="class">
          <xsl:value-of select="'dirtree'"/>
        </xsl:attribute>
        <xsl:if test="@textColor">
          <xsl:attribute name="style">color:<xsl:value-of select="@textColor" /></xsl:attribute>
        </xsl:if>
      </xsl:if>
    
      <xsl:value-of select="@name"/>
    </a>

  </div>
  
</xsl:template>

</xsl:stylesheet>