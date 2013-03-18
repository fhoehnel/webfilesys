<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
  <xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

  <!-- root node-->
  <xsl:template match="/">

    <html>
	  <head>
	    <title>WebFileSys: Folder Statistics</title>
        <link rel="stylesheet" type="text/css">
          <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/folderStats/css" />.css</xsl:attribute>
        </link>
	  
        <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
        <script src="/webfilesys/javascript/sunburstChart.js" type="text/javascript"></script>
        <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
	  
	    <script type="text/javascript">
	      var CLICK_TARGET = "/webfilesys/servlet?command=folderTreeStats&amp;path=";
	    
	      var windowWidth = getWinWidth();
          var windowHeight = getWinHeight();	
          
          centerX = (windowHeight - 100) / 2 + 35;
          centerY = (windowHeight - 100) / 2 + 35;
	    
		  var innerCircleRadius = Math.round(((windowHeight - 100) / 2) / (1.2 * <xsl:value-of select="/folderStats/treeDepth" />)) + 40;
		  var shellWidth = Math.round(((windowHeight - 100) - (innerCircleRadius * 2))  / (1.8 *(<xsl:value-of select="/folderStats/treeDepth" /> - 1)));
		  
		  var colorCounter = 0;
		  
		  function createChart()
		  {
	          document.getElementById("svgChartCont").style.width = (windowWidth - 30) + "px";
	          document.getElementById("svgChartCont").style.height = (windowHeight - 30) + "px";
	    
	          document.getElementById("svgChart").style.width = (windowWidth - 30) + "px";
	          document.getElementById("svgChart").style.height = (windowHeight - 30) + "px";
	    
	          document.getElementById("titleBox").style.left = (windowWidth - 200) + "px";
	          document.getElementById("legendBox").style.left = (windowWidth - 250) + "px";
	          document.getElementById("legendBox").style.height = (windowHeight - 180) + "px";

              document.getElementById("treeSizeCont").innerHTML = '<xsl:value-of select="/folderStats/@formattedTreeSize" />' + ' ' + '<xsl:value-of select="/folderStats/resources/msg[@key='label.treebytes']/@value" />';
              document.getElementById("treeFileNumCont").innerHTML = '<xsl:value-of select="/folderStats/@formattedTreeFileNum" />' + ' ' + '<xsl:value-of select="/folderStats/resources/msg[@key='label.treefiles']/@value" />';
    	     
    	      var pathText = document.createElementNS(svgNS, "text");
		      pathText.setAttribute("x", 20);
		      pathText.setAttribute("y", 20);
		      pathText.setAttribute("style", "font-family:Arial,Helvetica;font-size:12px;font-weight:bold");
		      var pathTextNode = document.createTextNode('<xsl:value-of select="/folderStats/relativePath" />');
		      pathText.appendChild(pathTextNode);
		      document.getElementById("svgChart").appendChild(pathText);

		      <xsl:if test="not(/folderStats/folder)">
    	        var infoText = document.createElementNS(svgNS, "text");
		        infoText.setAttribute("x", 200);
		        infoText.setAttribute("y", 300);
		        infoText.setAttribute("style", "font-family:Arial,Helvetica;font-size:12px;font-weight:bold;color:red");
		        var infoTextNode = document.createTextNode('<xsl:value-of select="/folderStats/resources/msg[@key='sunburst.noSubFolders']/@value" />');
		        infoText.appendChild(infoTextNode);
		        document.getElementById("svgChart").appendChild(infoText);
		      </xsl:if>
		  
		      // var folderCount = <xsl:value-of select="count(/folderStats/folder)" />;
			  
			  var startAngle = 0;
			  
			  var index = 0;
			  
		      <xsl:for-each select="/folderStats/folder">
                index = <xsl:value-of select="position()" /> - 1;
			  
			    var folderTreeSize = <xsl:value-of select="@treeSize" />
			  
			    var sectorSize = folderTreeSize * 360 / <xsl:value-of select="/folderStats/@treeSize" />
			    
				var endAngle = startAngle + sectorSize;
				
				var shortName = '<xsl:value-of select="@shortName" />';
				
				var tooltipText = shortName + ':&lt;br/&gt;<xsl:value-of select="@formattedTreeSize" /> bytes';
				
				var pathForScript = "";
				<xsl:if test="folder">
				    pathForScript = '<xsl:value-of select="@pathForScript" />';
				</xsl:if>
				
  	            pieChartSector(startAngle, endAngle, chartColors[colorCounter % chartColors.length], 
  	                           innerCircleRadius, tooltipText, pathForScript, CLICK_TARGET); 
                
                var shortNameLength = shortName.length;
                
                if (((innerCircleRadius >= (10 * shortNameLength)) &amp;&amp; (endAngle - startAngle > 20)) ||
                    ((innerCircleRadius >= (15 * shortNameLength)) &amp;&amp; (endAngle - startAngle > 10)))
                {
                    var innerLabelRadius = innerCircleRadius - (8 * shortNameLength) - 20;
                    if (innerLabelRadius &lt; 20)
                    {
                        innerLabelRadius = 20;
                    }
	                sectorLabel(startAngle, endAngle, innerLabelRadius, innerCircleRadius, "#000000", '<xsl:value-of select="@shortName" />');
                }
                
                var legendEntry = document.createElement("div");
                legendEntry.style.height = "18px";
                var legendBox = document.createElement("div");
                legendBox.className = "legendBox";
                legendBox.style.backgroundColor = chartColors[colorCounter % chartColors.length];
				legendBox.setAttribute("title", "<xsl:value-of select="@name" />" + ": <xsl:value-of select="@formattedTreeSize" /> bytes");
                legendEntry.appendChild(legendBox);
                var legendText = document.createElement("div");
                legendText.className = "legendText";
				var textNode = document.createTextNode('<xsl:value-of select="@shortName" />');
				legendText.appendChild(textNode);
                legendEntry.appendChild(legendText);
		        document.getElementById("legendBox").appendChild(legendEntry);

			    colorCounter++;
			  
			    var shellStartAngle1 = startAngle;
			  
                <xsl:if test="folder">
   			      var subFolderColor1 = (colorCounter + 3) % chartColors.length;
				  var sectorSize1 = sectorSize;
			    </xsl:if>
			   
 			    <xsl:for-each select="folder">
				  <xsl:call-template name="subfolder">
				    <xsl:with-param name="level" select="'1'"/>
				  </xsl:call-template>
				</xsl:for-each>
			  
			    startAngle = endAngle;
				
			  </xsl:for-each>
			  
			  var rootFileSize = <xsl:value-of select="/folderStats/@rootFileSize" />;
 		      var sectorSize = rootFileSize * 360 / <xsl:value-of select="/folderStats/@treeSize" />;
		      var endAngle = startAngle + sectorSize;
		      var tooltipText = "files in root: <xsl:value-of select="/folderStats/@rootFileNum" />&lt;br/&gt;<xsl:value-of select="/folderStats/@formattedRootFileSize" /> bytes";
			  
			  pieChartSector(startAngle, endAngle, ROOT_FILES_COLOR, innerCircleRadius, tooltipText); 
			  
	          <xsl:if test="/folderStats/parentFolder">
	            document.getElementById("parentFolderLink").setAttribute("href", "/webfilesys/servlet?command=folderTreeStats&amp;path=" + encodeURIComponent('<xsl:value-of select="/folderStats/parentFolder" />') + "&amp;random=" + (new Date()).getTime());
	          </xsl:if>
		  }
		</script>
	  
	  </head>
	  <body onload="createChart()">
	  
        <div id="svgChartCont" style="width:970px;height:670px;background-color:ivory;border:1px solid black">
          <svg id="svgChart" xmlns="http://www.w3.org/2000/svg" version="1.1" width="970" height="670">
	      </svg>
	    </div>
		
	    <div id="titleBox" style="position:absolute;left:800px;top:60px;width:150px;height:30px;color:#000000;font-family:Arial,Helvetica;font-size:12px;border:1px solid #a0a0a0;padding:10px;visibility:hidden"></div>
	  
	    <div id="legendBox" style="position:absolute;left:750px;top:120px;width:200px;height:530px;font-family:Arial,Helvetica;font-size:12px;border:1px solid #a0a0a0;padding:10px;overflow-x:hidden;overflow-y:auto;float:left"></div>
	  
	    <div id="treeSizeCont" class="sunburstTreeSize sunburstText"></div>

	    <div id="treeFileNumCont" class="sunburstTreeFileNum sunburstText"></div>
	  
	    <xsl:if test="/folderStats/parentFolder">
	      <a id="parentFolderLink" class="sunburstParentLink"><xsl:value-of select="/folderStats/resources/msg[@key='sunburst.parentFolderLink']/@value" /></a>
	    </xsl:if>
	  </body>
    </html>
	
  </xsl:template>
  
  <xsl:template name="subfolder">
    <xsl:param name="level" />
	var shellStartAngle<xsl:value-of select="$level + 1" /> = shellStartAngle<xsl:value-of select="$level" />;
	
	var sectorSize<xsl:value-of select="$level + 1" />;
	
    var level = <xsl:value-of select="$level" />;
	
	var parentTreeSize = <xsl:value-of select="../@treeSize" />;
	
	var shellSectorSize;
	
	if (parentTreeSize == 0)
	{
	    shellSectorSize = 0;
	}
	else 
	{
        shellSectorSize = <xsl:value-of select="@treeSize" /> * sectorSize<xsl:value-of select="$level" /> / parentTreeSize;
	}
	
    var shellSectorEndAngle = shellStartAngle<xsl:value-of select="$level" /> + shellSectorSize;
	
	shellSector(shellStartAngle<xsl:value-of select="$level" />, shellSectorEndAngle, 
	            chartColors[subFolderColor<xsl:value-of select="$level" />], 
	            innerCircleRadius + ((level - 1) * shellWidth), 
	    	    innerCircleRadius + (level * shellWidth),
       	        '<xsl:value-of select="@shortName" />:&lt;br/&gt;<xsl:value-of select="@formattedTreeSize" /> bytes');
				
	shellStartAngle<xsl:value-of select="$level" /> = shellSectorEndAngle;

	sectorSize<xsl:value-of select="$level + 1" /> = shellSectorSize;
	
	var subFolderColor<xsl:value-of select="$level + 1" /> = Math.floor(Math.random() * chartColors.length);
    if (subFolderColor<xsl:value-of select="$level + 1" /> == subFolderColor<xsl:value-of select="$level" />)
    {
        subFolderColor<xsl:value-of select="$level + 1" /> = (subFolderColor<xsl:value-of select="$level + 1" /> + 1) % chartColors.length;
    }
	
	<xsl:for-each select="folder">
	  <xsl:call-template name="subfolder">
	    <xsl:with-param name="level" select="$level + 1"/>
	  </xsl:call-template>
	</xsl:for-each>

  </xsl:template>

</xsl:stylesheet>
