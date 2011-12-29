<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="fileSysStat" />

<xsl:template match="/fileSysStat">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="css" />.css</xsl:attribute>
  </link>

  <style type="text/css">
    td.fileSysData {border:1px solid #808080;background-color:ivory;font-size:10pt;font-family:Arial,Helvetica;padding:5px;}
    td.fileSysDataNum {border:1px solid #808080;background-color:ivory;font-size:10pt;font-family:Arial,Helvetica;text-align:right;padding-right:10px;padding:5px;}
    th.fileSysDataHead {border:1px solid #808080;background-color:#ffffe0;font-size:10pt;font-family:Arial,Helvetica;font-weight:bold;padding:5px;}
    table.fileSysStat {border-collapse:collapse;width:100%;margin-top:10px;}
  </style>

  <title><xsl:value-of select="resources/msg[@key='label.slideshowParmsTitel']/@value" /></title>

</head>

<body>

  <div class="headline">
    <xsl:value-of select="resources/msg[@key='label.filesyshead']/@value" />
  </div>

  <table class="fileSysStat" cellspacing="1">
  
    <tr>
      <th class="fileSysDataHead">
        <xsl:value-of select="resources/msg[@key='label.filesys']/@value" />
      </th>

      <th class="fileSysDataHead">
        <xsl:value-of select="resources/msg[@key='label.usage']/@value" />
      </th>

      <th class="fileSysDataHead">
        <xsl:value-of select="resources/msg[@key='label.capacity']/@value" />
      </th>

      <th class="fileSysDataHead">
        <xsl:value-of select="resources/msg[@key='label.freespace']/@value" />
      </th>

      <th class="fileSysDataHead">
        <xsl:value-of select="resources/msg[@key='label.percentUsed']/@value" />
      </th>
    </tr>
    
    <xsl:for-each select="filesystems/filesys">
          
      <tr>
        <td class="fileSysData">
          <xsl:value-of select="mountPoint" />
        </td>

        <td class="fileSysData">
          <img src="/webfilesys/images/bar.gif" border="0" height="20" style="border-color:navy;border-style:solid;border-width:1px;border-right-width:0;">
            <xsl:attribute name="width"><xsl:value-of select="usage * 3" /></xsl:attribute>
          </img>
          <img src="/webfilesys/images/space.gif" border="0" height="20" style="border-color:navy;border-style:solid;border-width:1px;border-left-width:0;">
            <xsl:attribute name="width"><xsl:value-of select="(100 - usage) * 3" /></xsl:attribute>
          </img>
        </td>

        <td class="fileSysDataNum">
          <xsl:value-of select="capacity" />
        </td>

        <td class="fileSysDataNum">
          <xsl:value-of select="free" />
        </td>

        <td class="fileSysDataNum">
          <xsl:value-of select="usage" />
        </td>
      </tr>
      
    </xsl:for-each>
     
  </table>
  
  <div style="text-align:center;min-width:100%;margin-top:15px;">
    <input type="button" onclick="window.close()">
      <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='button.closewin']/@value" /></xsl:attribute>
    </input>
  </div>              
  
</body>

</html>

</xsl:template>

</xsl:stylesheet>
