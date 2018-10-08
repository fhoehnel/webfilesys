<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
  <xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

  <!-- root node-->
  <xsl:template match="/">

    <html>
	  <head>
	    <title>WebFileSys: Folder Statistics</title>
	    
        <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
	    
        <link rel="stylesheet" type="text/css">
          <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/folderStats/css" />.css</xsl:attribute>
        </link>
	  
        <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
        <script src="/webfilesys/javascript/sunburstChart.js" type="text/javascript"></script>
        <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
        <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
	  
	    <script type="text/javascript">
	      var CLICK_TARGET = "/webfilesys/servlet?command=folderTreeStats&amp;path=";
	    
	      var windowWidth = getWinWidth();
          var windowHeight = getWinHeight();	
          
          centerX = (windowHeight - 100) / 2 + 35;
          centerY = (windowHeight - 100) / 2 + 35;
	    
	      var chartTreeDepth = <xsl:value-of select="/folderStats/treeDepth" />;
	      <xsl:if test="/folderStats/hideSubTrees">
	          chartTreeDepth = 1;
	      </xsl:if>
	    
		  var innerCircleRadius = Math.round(((windowHeight - 100) / 2) / (1.2 * chartTreeDepth)) + 40;
	      <xsl:if test="/folderStats/hideSubTrees">
	          innerCircleRadius += 40;
          </xsl:if>
		  var shellWidth = Math.round(((windowHeight - 100) - (innerCircleRadius * 2))  / (1.8 *(chartTreeDepth - 1)));
		  
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
				
				var tooltipText = shortName + ':&lt;br/&gt;<xsl:value-of select="@formattedTreeSize" /> bytes&lt;br/&gt;<xsl:value-of select="@formattedTreeFileNum" /> files';
				
				var pathForScript = "";
				var clickAction = "";
				<xsl:if test="folder">
				    pathForScript = '<xsl:value-of select="@pathForScript" />';
				    clickAction = CLICK_TARGET;
				</xsl:if>
				
  	            pieChartSector(startAngle, endAngle, chartColors[colorCounter % chartColors.length], 
  	                           innerCircleRadius, tooltipText, pathForScript, clickAction); 
                
                var minAngelToShowLabelInsideCircle = 15;
	            <xsl:if test="/folderStats/hideSubTrees">
	                minAngelToShowLabelInsideCircle = 8;
	            </xsl:if>
                
                if (endAngle - startAngle &gt; minAngelToShowLabelInsideCircle) {
	                textOnCircle(startAngle, endAngle,
       	                         innerCircleRadius - 20, 
					             shortName,
					             8, "12px");	
                <xsl:if test="not(/folderStats/hideSubTrees)">								 
                  <xsl:if test="not(folder)">								 
                    } else if (endAngle - startAngle &gt; 5) {
                        sectorLabel(startAngle, endAngle, innerCircleRadius, innerCircleRadius + shellWidth, "#000000", shortName, "12px");
                  </xsl:if>
                </xsl:if>
                <xsl:if test="/folderStats/hideSubTrees">								 
                  } else {
                      sectorLabel(startAngle, endAngle, innerCircleRadius / 3 * 2, innerCircleRadius, "#000000", shortName, "12px", true);
                </xsl:if>
				}
				
                var legendEntry = document.createElement("div");
                legendEntry.style.height = "18px";
                var legendBox = document.createElement("div");
                legendBox.className = "legendBox";
                legendBox.style.backgroundColor = chartColors[colorCounter % chartColors.length];
				legendBox.setAttribute("title", "<xsl:value-of select="@name" />" + ": <xsl:value-of select="@formattedTreeSize" /> bytes, <xsl:value-of select="@formattedTreeFileNum" /> files");
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
			   
	            <xsl:if test="not(/folderStats/hideSubTrees)">
			   
 			      <xsl:for-each select="folder">
				    <xsl:call-template name="subfolder">
				      <xsl:with-param name="level" select="'1'"/>
				    </xsl:call-template>
				  </xsl:for-each>
			  
			    </xsl:if>
			  
			    startAngle = endAngle;
				
			  </xsl:for-each>
			  
			  var rootFileSize = <xsl:value-of select="/folderStats/@rootFileSize" />;
 		      var sectorSize = rootFileSize * 360 / <xsl:value-of select="/folderStats/@treeSize" />;
		      var endAngle = startAngle + sectorSize;
		      var tooltipText = "files in root: <xsl:value-of select="/folderStats/@rootFileNum" />&lt;br/&gt;<xsl:value-of select="/folderStats/@formattedRootFileSize" /> bytes";
			  
			  pieChartSector(startAngle, endAngle, ROOT_FILES_COLOR, innerCircleRadius, tooltipText); 
			  
              <xsl:if test="/folderStats/hideSubTrees">								 
                sectorLabel(startAngle, endAngle, innerCircleRadius / 3 * 2, innerCircleRadius, "#000000", "files in root", "12px", true);
              </xsl:if>
              <xsl:if test="not(/folderStats/hideSubTrees)">								 
                if (endAngle - startAngle &gt; 15) {
	                textOnCircle(startAngle, endAngle,
       	                         innerCircleRadius - 20, 
				                 "files in root",
					             5, "12px");			
			    }
			  </xsl:if>
			  
	          <xsl:if test="/folderStats/parentFolder">
	            document.getElementById("parentFolderLink").setAttribute("href", "javascript:oneLevelUp()");
	          </xsl:if>
		  }
		  
		  function oneLevelUp() {
		      showHourGlass();
              window.location.href = "/webfilesys/servlet?command=folderTreeStats&amp;path=" + encodeURIComponent('<xsl:value-of select="/folderStats/parentFolder" />') + "&amp;random=" + (new Date()).getTime();
  		  }
		  
		  function showSubTrees() {
		      showHourGlass();
              window.location.href = '/webfilesys/servlet?command=folderTreeStats&amp;path=<xsl:value-of select="/folderStats/encodedPath" />';
  		  }
  		  
		  function hideSubTrees() {
		      showHourGlass();
              window.location.href = '/webfilesys/servlet?command=folderTreeStats&amp;path=<xsl:value-of select="/folderStats/encodedPath" />&amp;hideSubTrees=true';
  		  }
  		  
		</script>
	  
	  </head>
	  <body onload="createChart()">
	  
        <div id="svgChartCont" style="width:970px;height:670px;background-color:ivory;border:1px solid black">
          <svg id="svgChart" xmlns="http://www.w3.org/2000/svg" version="1.1" width="970" height="670">
	      </svg>
	    </div>
		
	    <div id="titleBox" style="position:absolute;left:800px;top:60px;width:150px;height:42px;color:#000000;font-family:Arial,Helvetica;font-size:12px;border:1px solid #a0a0a0;padding:5px 10px;visibility:hidden"></div>
	  
	    <div id="legendBox" style="position:absolute;left:750px;top:120px;width:200px;height:530px;font-family:Arial,Helvetica;font-size:12px;border:1px solid #a0a0a0;padding:10px;overflow-x:hidden;overflow-y:auto;float:left"></div>
	  
	    <div id="treeSizeCont" class="sunburstTreeSize sunburstText"></div>

	    <div id="treeFileNumCont" class="sunburstTreeFileNum sunburstText"></div>
	  
	    <xsl:if test="/folderStats/parentFolder">
	      <a id="parentFolderLink" class="sunburstParentLink"><xsl:value-of select="/folderStats/resources/msg[@key='sunburst.parentFolderLink']/@value" /></a>
	    </xsl:if>

        <a id="showHideSubLink" class="sunburstSubtreeLink">
	      <xsl:if test="/folderStats/hideSubTrees">
	        <xsl:attribute name="href">javascript:showSubTrees()</xsl:attribute>
	        <xsl:value-of select="/folderStats/resources/msg[@key='sunburst.showSubLink']/@value" />
          </xsl:if>
	      <xsl:if test="not(/folderStats/hideSubTrees)">
	        <xsl:attribute name="href">javascript:hideSubTrees()</xsl:attribute>
	        <xsl:value-of select="/folderStats/resources/msg[@key='sunburst.hideSubLink']/@value" />
          </xsl:if>
	    </a>
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
	
	var clickAction = "";
	<xsl:if test="folder">
      if (shellSectorEndAngle - shellStartAngle<xsl:value-of select="$level" /> &gt; 3) {
          clickAction = CLICK_TARGET + encodeURIComponent('<xsl:value-of select="@pathForScript" />') + "&amp;random=" + (new Date()).getTime();
      }    
    </xsl:if>
	
	shellSector(shellStartAngle<xsl:value-of select="$level" />, shellSectorEndAngle, 
	            chartColors[subFolderColor<xsl:value-of select="$level" />], 
	            innerCircleRadius + ((level - 1) * shellWidth), 
	    	    innerCircleRadius + (level * shellWidth),
       	        '<xsl:value-of select="@shortName" />:&lt;br/&gt;<xsl:value-of select="@formattedTreeSize" /> bytes&lt;br/&gt;<xsl:value-of select="@formattedTreeFileNum" /> files',
       	        clickAction);

    var minAngleToShowtext = 15 - level;
	if (minAngleToShowtext &lt; 5) {
	    minAngleToShowtext = 5;
	}
				
    if (shellSectorEndAngle - shellStartAngle<xsl:value-of select="$level" /> &gt; minAngleToShowtext) {
	    textOnCircle(shellStartAngle<xsl:value-of select="$level" />, shellSectorEndAngle,
       	             innerCircleRadius + (level * shellWidth) - 20, 
					 '<xsl:value-of select="@shortName" />',
					 5, "12px");	
    }
    <xsl:if test="not(folder)">								 
      else if (shellSectorEndAngle - shellStartAngle<xsl:value-of select="$level" /> &gt; 3) {
          sectorLabel(shellStartAngle<xsl:value-of select="$level" />, shellSectorEndAngle, 
		              innerCircleRadius + (level * shellWidth), innerCircleRadius + ((level + 2) * shellWidth),
					  "#000000", 
					  '<xsl:value-of select="@shortName" />', 
					  "12px");
      }
	</xsl:if>					 
				
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
