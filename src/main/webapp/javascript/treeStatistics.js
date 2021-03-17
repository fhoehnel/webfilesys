  function getSubfolderStats() {
  
      if (folderNum == 0) {
          return;
      }
      
      const parameters = { "path": folderList[folderIdx] };
    
      folderIdx ++;

	  xmlGetRequest("ajaxFolderStats", parameters, function(responseXml) {
          let item = responseXml.getElementsByTagName("bytesInTree")[0];            
          const bytesInTree = item.firstChild.nodeValue;

          item = responseXml.getElementsByTagName("foldersInTree")[0];            
          const foldersInTree = item.firstChild.nodeValue;

          item = responseXml.getElementsByTagName("filesInTree")[0];            
          const filesInTree = item.firstChild.nodeValue;

          item = responseXml.getElementsByTagName("subdirLevels")[0];            
          const subdirLevels = item.firstChild.nodeValue;
             
          totalFileNum += parseInt(filesInTree);
          totalSubFolderNum += parseInt(foldersInTree);
          totalBytesInTree += parseInt(bytesInTree);
             
          document.getElementById("treeFiles").innerHTML = formatDecimalNumber(totalFileNum);
          document.getElementById("treeBytes").innerHTML = formatDecimalNumber(totalBytesInTree);
          document.getElementById("treeFolders").innerHTML = formatDecimalNumber(totalSubFolderNum);

          const treeDepth = parseInt(subdirLevels) + 1;
          if (treeDepth > maxSubdirLevels) {
              maxSubdirLevels = treeDepth;
              document.getElementById("subdirLevels").innerHTML = maxSubdirLevels;
          }
             
          const subdirSize = parseInt(bytesInTree);
             
          const sizeTextId = "bar-" + (folderIdx - 1);
          document.getElementById(sizeTextId).innerHTML = formatDecimalNumber(subdirSize);
             
          subTreeSize[folderIdx - 1] = subdirSize;
             
          if (subdirSize > maxSubTreeSize) {
              maxSubTreeSize = subdirSize;
          }

          if (folderIdx < folderNum) {
              setTimeout(() => getSubfolderStats(), 1);
          } else {
              paintDiagram();
          }    
      });        
  }
  
  function paintDiagram() {
      for (let i = 0; i < folderNum; i++) {
          const sizePercent = subTreeSize[i] * 100 / maxSubTreeSize;
          
          const totalPercent = Math.round(subTreeSize[i] * 100 / totalBytesInTree);
          
          let barWidth = sizePercent * 4;
          
          if (barWidth < 2) {
              barWidth = 2;
          }
          
          const barId = "bar-" + i;
          document.getElementById(barId).style.backgroundSize = barWidth + "px" + " 20px";
          
          if (sizePercent < 50) {
              document.getElementById(barId).style.paddingLeft = (barWidth + 4) + "px";
          }
          
          document.getElementById(barId).innerHTML = formatDecimalNumber(subTreeSize[i]) + ' (' + totalPercent + ' %)';
      }
  }
  
  
  