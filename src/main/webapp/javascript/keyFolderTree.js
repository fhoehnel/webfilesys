  function handleFolderTreeKey(evt)
  {
      var keyHandled = false;
      
      if (!evt) 
      {
          if (window.event) 
          {
              evt = window.event;
          }
      }
      if (evt) 
      {
          // console.log('event keycode: ' + evt.keyCode + ' currentDirId=' + currentDirId);
          
    	  if (evt.keyCode == 27)
          {
              // ESC key
              handleESCKey();              
              
              keyHandled = true;
          }
    	  
          if (currentDirId == "")
          {
              return;
          }
          
          var currentDirDiv = document.getElementById(currentDirId);
          if (!currentDirDiv)
          {
              return;
          }

          if (evt.keyCode == 40)
          {
              // down key
              handleDownKey(currentDirDiv);              
              
              keyHandled = true;
          }
          else if (evt.keyCode == 38)
          {
              // up key
              handleUpKey(currentDirDiv);              
              keyHandled = true;
          }
          else if (evt.keyCode == 13)
          {
              // ENTER key
              handleEnterKey(currentDirDiv);
              
              keyHandled = true;
          }
          else if ((evt.keyCode == 107) || (evt.keyCode == 171) || (evt.keyCode == 187))
          {
              // plus key
              handlePlusMinusKey(currentDirDiv, "plus");
              
              keyHandled = true;
          }
          else if ((evt.keyCode == 109) || (evt.keyCode == 173) || (evt.keyCode == 189))  
          {
              // minus key
              handlePlusMinusKey(currentDirDiv, "minus");
              
              keyHandled = true;
          }
          else if (evt.keyCode == 34)
          {
              // page down key
              handlePageDownKey();
              
              keyHandled = true;
          }
          else if (evt.keyCode == 33)
          {
              // page up key
              handlePageUpKey();
              
              keyHandled = true;
          }
          else if (evt.keyCode == 36)
          {
              // pos 1 key
              handlePos1Key();
              
              keyHandled = true;
          }
          else if (evt.keyCode == 35)
          {
              // end key
              handleEndKey();
              
              keyHandled = true;
          }
      }
      
      if (keyHandled) 
      {
    	  evt.preventDefault();
          evt.stopPropagation();
      }
  }

  function handleESCKey() 
  {
      document.getElementById('contextMenu').style.visibility = 'hidden';

      var promptBox = document.getElementById('prompt');
      if (promptBox)
      {
          promptBox.style.visibility = 'hidden';
      }
  }
  
  function handlePos1Key()
  {
      var linePerPage = 5000;
      
      for (i = 0; i < linePerPage; i++)
      {
          var currentDirDiv = document.getElementById(currentDirId);
          
          if (!currentDirDiv)
          {
              return;
          }
          
          if (!handleUpKey(currentDirDiv))
          {
              return;
          }
      }
  }

  function handleEndKey()
  {
      var linePerPage = 5000;
      
      for (i = 0; i < linePerPage; i++)
      {
          var currentDirDiv = document.getElementById(currentDirId);
          
          if (!currentDirDiv)
          {
              return;
          }
          
          if (!handleDownKey(currentDirDiv))
          {
              return;
          }
      }
  }

  function handlePageDownKey()
  {
      var linePerPage = getWinHeight() / 19;
      
      for (i = 0; i < linePerPage; i++)
      {
          var currentDirDiv = document.getElementById(currentDirId);
          
          if (!currentDirDiv)
          {
              return;
          }
          
          if (!handleDownKey(currentDirDiv))
          {
              return;
          }
      }
  }

  function handlePageUpKey()
  {
      var linePerPage = getWinHeight() / 19;
      
      for (i = 0; i < linePerPage; i++)
      {
          var currentDirDiv = document.getElementById(currentDirId);
          
          if (!currentDirDiv)
          {
              return;
          }
          
          if (!handleUpKey(currentDirDiv))
          {
              return;
          }
      }
  }

  function handleUpKey(currentDirDiv) 
  {
      var prevSibling = getPrevSiblingElem(currentDirDiv);

      if (prevSibling) 
      {
          if (upToLastOpenChild(prevSibling, currentDirDiv)) 
          {
              return true;
          }
                  
          if (moveDirSelection(prevSibling, currentDirDiv))
          {
              return true;
          }
      }
      else
      {
          var parent = currentDirDiv;
          var stop = false;
          do 
          {
              parent = parent.parentNode;
                     
              if (parent)
              {
                  if ((parent.nodeName == "DIV") && (parent.id != ""))
                  {
                      stop = true;
                  }
              }
          }
          while ((!stop) && (parent));
                  
          if (parent) 
          {
              if (moveDirSelection(parent, currentDirDiv))
              {
                  return true;
              }
          }
      }
      
      return false;
  }

  function handleDownKey(currentDirDiv)  {
      if (downToOpenChild(currentDirDiv)) {
          return true;
      }
              
      var nextSibling = getNextSiblingElem(currentDirDiv);

      if (!nextSibling) {
          var parent = currentDirDiv;
          var stop = false;
          do {
              parent = parent.parentNode;
                     
              if (parent.nodeName == "DIV") {
                  nextSibling = getNextSiblingElem(parent);
              } else {
                  stop = true;
              }
          } while ((!stop) && (!nextSibling));
      }
             
      if (nextSibling) {
          if (moveDirSelection(nextSibling, currentDirDiv)) {
              return true;
          }
      }

      return false;
  }

  function handlePlusMinusKey(currentDirDiv, imgSrcStr)
  {
      var anchorChild = getFirstChildElem(currentDirDiv);
      
      if ((!anchorChild) || (anchorChild.tagName != "A"))
      {
          return;
      }

      var imgChild = anchorChild.firstChild;
      
      if ((!imgChild) || (imgChild.tagName != "IMG") || (imgChild.src.indexOf(imgSrcStr) < 0))
      {
          return;
      }
  
      eval(decodeURIComponent(anchorChild.href));
  }

  function handleEnterKey(currentDirDiv)
  {
      var lastAnchor;
      
      var childNum = currentDirDiv.childNodes.length;
      
      if (childNum > 0)
      {
          for (i = childNum - 1; (!lastAnchor) && (i >= 0); i--)
          {
              var childNode = currentDirDiv.childNodes[i];
              
              if (childNode.tagName == "A")
              {
                  lastAnchor = childNode;
              }
          }
      }
      
      if (!lastAnchor)
      {
          return;
      }
  
      eval(decodeURIComponent(lastAnchor.href));
  }

  function upToLastOpenChild(prevSibling, currentDirDiv) {
	  var indentDiv = getLastChildElem(prevSibling)
	  
      if (indentDiv != undefined) {
          if (indentDiv.tagName == "DIV") {
              var dirDiv = getFirstChildElem(indentDiv);
          
              if (dirDiv) {
                  if (dirDiv.id == "") {
                      dirDiv = getFirstChildElem(dirDiv);
                  }
              }

              if (dirDiv) {
                  var lastSibling = getLastSiblingElem(dirDiv);
              
                  if (lastSibling) {
                	  dirDiv = lastSibling;
                  }
                  if (!upToLastOpenChild(dirDiv, currentDirDiv)) {
                      var prevFolderImg = locateFolderImage(dirDiv);
            
                      if (prevFolderImg) {
                    	  prevFolderImg.setAttribute("savedIcon", prevFolderImg.src);
                          prevFolderImg.src = "/webfilesys/images/folder1.gif";
                      
                          var currentFolderImg = locateFolderImage(currentDirDiv);
                      
                          if (currentFolderImg) {
                        	  var savedIcon = currentFolderImg.getAttribute("savedIcon");
                        	  if (savedIcon != undefined) {
                                  currentFolderImg.src = savedIcon;
                        	  } else {
                                  currentFolderImg.src = "/webfilesys/images/folder.gif";
                        	  }
                        	  
                              currentDirId = dirDiv.id;
                              return true;
                          }
                      }
                  } else {
                      return true;
                  }
              }
          }
      }   
      
      return false;
  }

  function moveDirSelection(newSelectedDiv, currentDirDiv)  {
      var newSelectedImg = locateFolderImage(newSelectedDiv);
              
      if (newSelectedImg) {
          if (newSelectedImg.src.indexOf("miniDisk") > 0) {
              newSelectedImg.src = "/webfilesys/images/miniDisk2.gif";
          } else {
        	  newSelectedImg.setAttribute("savedIcon", newSelectedImg.src);
              newSelectedImg.src = "/webfilesys/images/folder1.gif";
          }
                      
          var currentFolderImg = locateFolderImage(currentDirDiv);
                      
          if (currentFolderImg) {
              if (currentFolderImg.src.indexOf("miniDisk") > 0) {
                  currentFolderImg.src = "/webfilesys/images/miniDisk.gif";
              } else {
            	  var savedIcon = currentFolderImg.getAttribute("savedIcon");
            	  if (savedIcon != undefined) {
                      currentFolderImg.src = savedIcon;
            	  } else {
                      currentFolderImg.src = "/webfilesys/images/folder.gif";
            	  }
              }
              currentDirId = newSelectedDiv.id;
              
              scrollToVisibility(currentFolderImg);
              
              return true;
          }
      }
      return false;
  }

  function scrollToVisibility(nodeToMakeVisible) {
	  var windowHeight = getWinHeight();
      var yScrolled;
	  if (window.ActiveXObject !== undefined) {
	      yScrolled = document.body.scrollTop;
	  } else {
	      yScrolled = window.pageYOffset;
	  }
      
      var offsetTop = getAbsolutePos(nodeToMakeVisible)[1];   
      if (offsetTop > yScrolled + windowHeight - 100) {
    	  window.scrollBy(0, 19);
      } else if (offsetTop < yScrolled + 60) {
    	  window.scrollBy(0, -19);
      }
  }
  
  function downToOpenChild(currentDirDiv)  {
      var childNum = currentDirDiv.childNodes.length;

      if (childNum > 0) {
          var indentDiv = currentDirDiv.childNodes[childNum - 1];

          var i = childNum - 2;

          while ((i >= 0) && (indentDiv.nodeType != 1)) {
              indentDiv = currentDirDiv.childNodes[i];
              i--;
          }
          
          if (indentDiv.tagName == "DIV") {
              var dirDiv = getFirstChildElem(indentDiv);
          
              if (dirDiv) {
                  if ((dirDiv.id == "") || (dirDiv.id === undefined)) {
					  dirDiv = getFirstChildElem(dirDiv);
                  }
              }

              if (dirDiv) {
                  var nextFolderImg = locateFolderImage(dirDiv);
            
                  if (nextFolderImg) {
                	  nextFolderImg.setAttribute("savedIcon", nextFolderImg.src);

                      nextFolderImg.src = "/webfilesys/images/folder1.gif";
                      
                      var currentFolderImg = locateFolderImage(currentDirDiv);
                      
                      if (currentFolderImg) {
                    	  var savedIcon = currentFolderImg.getAttribute("savedIcon");
                    	  if (savedIcon != undefined) {
                              currentFolderImg.src = savedIcon;
                    	  } else {
                              currentFolderImg.src = "/webfilesys/images/folder.gif";
                    	  }
                          currentDirId = dirDiv.id;
                          return true;
                      }
                  }
              }
          }
      }   
      
      return false;
  }

  function locateFolderImage(divElem) {
      var child = getFirstChildElem(divElem);
	  
      if (child) {
          var folderImg = child.firstChild; // first <a> element

          if (folderImg) {
              var cssClass = folderImg.className;
                          
              if ((cssClass != undefined) && ((cssClass == "folder") || (cssClass == "icon"))) {
                  return folderImg;
              }
          }

          child = child.nextSibling;  // next <a> element
                              
          if (child) {
              folderImg = child.firstChild;
                              
              if (folderImg) {
                  var cssClass = folderImg.className;
                          
                  if ((cssClass != undefined) && ((cssClass == "folder") || (cssClass == "icon"))) {
                      return folderImg;
                  }
              }
          }
      }
  }

  function getPrevSiblingElem(domNode) {
	  var prevSibling = domNode.previousSibling;
	  
	  while (prevSibling && (!isElement(prevSibling))) {
		  prevSibling = prevSibling.previousSibling;
	  }
	  
	  return prevSibling;
  }

  function getNextSiblingElem(domNode) {
	  var nextSibling = domNode.nextSibling;
	  
	  while (nextSibling && (!isElement(nextSibling))) {
		  nextSibling = nextSibling.nextSibling;
	  }
	  
	  return nextSibling;
  }
  
  function getLastSiblingElem(domNode) {
	  var nextSibling = domNode.nextSibling;
	  var lastSiblingElem;
	  
	  while (nextSibling) {
		  if (isElement(nextSibling)) {
			  lastSiblingElem = nextSibling;
		  }
		  nextSibling = nextSibling.nextSibling;
	  }
	  
	  return lastSiblingElem;
  }
  
  function getFirstChildElem(parentNode) {
	  var childNode = parentNode.firstChild;
	  
	  while (childNode && (!isElement(childNode))) {
		  childNode = childNode.nextSibling;
	  }
	  
	  return childNode;
  }
  
  function getLastChildElem(parentNode) {
      var childNum = parentNode.childNodes.length;
      
      if (childNum == 0) {
    	  return undefined;
      }
      
      var childNode = parentNode.childNodes[childNum - 1];
	  
      while (childNode && (!isElement(childNode))) {
		  childNode = childNode.previousSibling;
	  }

	  return childNode;
  }
  
  function isElement(obj) 
  {
      try 
      {
	      //Using W3 DOM2 (works for FF, Opera and Chrom)
	      return obj instanceof HTMLElement;
	  }
	  catch(e)
	  {
	      //Browsers not supporting W3 DOM2 don't have HTMLElement and
	      //an exception is thrown and we end up here. Testing some
	      //properties that all elements have. (works on IE7)
	      return (typeof obj==="object") &&
	             (obj.nodeType===1) && (typeof obj.style === "object") &&
	             (typeof obj.ownerDocument ==="object");
	  }
  }  