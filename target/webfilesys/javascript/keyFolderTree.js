// not used because only working with Firefox
// other browsers require very different DOM manipulation operations

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
          // + = 107
          // - = 109
          // pageup = 33
          // pagedown = 34
          // pos1 = 36
          // end = 35
          // alert('event keycode: ' + evt.keyCode + ' currentDirId=' + currentDirId);
          
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
          else if (evt.keyCode == 107)
          {
              // plus key
              handlePlusMinusKey(currentDirDiv, "plus");
              
              keyHandled = true;
          }
          else if (evt.keyCode == 109)
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
          evt.stopPropagation();
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
          console.log("handleEndKey idx " + i);
      
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
      var prevSibling = currentDirDiv.previousSibling;

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

  function handleDownKey(currentDirDiv) 
  {
      if (downToOpenChild(currentDirDiv))
      {
          return true;
      }
              
      var nextSibling = currentDirDiv.nextSibling;
     
      if (nextSibling && (nextSibling.nodeType != 1)) 
      {
          // for Chrome
          nextSibling = nextSibling.nextSibling;
      }

      if (!nextSibling) 
      {
          var parent = currentDirDiv;
          var stop = false;
          do 
          {
              parent = parent.parentNode;
                     
              if (parent.nodeName == "DIV")
              {
                  nextSibling = parent.nextSibling;
                  
                  if (nextSibling && (nextSibling.nodeType != 1)) 
                  {
                      // for Chrome
                      nextSibling = nextSibling.nextSibling;
                  }
              }
              else
              {
                  stop = true;
              }
          }
          while ((!stop) && (!nextSibling));
      }
             
      if (nextSibling) 
      {
          if (moveDirSelection(nextSibling, currentDirDiv))
          {
              return true;
          }
      }

      return false;
  }

  function handlePlusMinusKey(currentDirDiv, imgSrcStr)
  {
      var anchorChild = currentDirDiv.firstChild;
      
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

  function upToLastOpenChild(prevSibling, currentDirDiv) 
  {
      var childNum = prevSibling.childNodes.length;
      
      if (childNum > 0)
      {
          var indentDiv = prevSibling.childNodes[childNum - 1];
          
          if (indentDiv.tagName == "DIV")
          {
              var dirDiv = indentDiv.firstChild;
          
              if (dirDiv)
              {
                  if (dirDiv.id == "")
                  {
                      dirDiv = dirDiv.firstChild;
                  }
              }

              if (dirDiv)
              {
                  while (dirDiv.nextSibling)
                  {
                      dirDiv = dirDiv.nextSibling;
                  }
              
                  if (!upToLastOpenChild(dirDiv, currentDirDiv))
                  {
                      var prevFolderImg = locateFolderImage(dirDiv);
            
                      if (prevFolderImg)
                      {
                          prevFolderImg.src = "/webfilesys/images/folder1.gif";
                      
                          var currentFolderImg = locateFolderImage(currentDirDiv);
                      
                          if (currentFolderImg) 
                          {
                              currentFolderImg.src = "/webfilesys/images/folder.gif";
                              currentDirId = dirDiv.id;
                              return true;
                          }
                      }
                  }
                  else
                  {
                      return true;
                  }
              }
          }
      }   
      
      return false;
  }

  function moveDirSelection(newSelectedDiv, currentDirDiv)
  {
      var newSelectedImg = locateFolderImage(newSelectedDiv);
              
      if (newSelectedImg)
      {
          if (newSelectedImg.src.indexOf("miniDisk") > 0) 
          {
              newSelectedImg.src = "/webfilesys/images/miniDisk2.gif";
          }
          else
          {
              newSelectedImg.src = "/webfilesys/images/folder1.gif";
          }
                      
          var currentFolderImg = locateFolderImage(currentDirDiv);
                      
          if (currentFolderImg) 
          {
              if (currentFolderImg.src.indexOf("miniDisk") > 0) 
              {
                  currentFolderImg.src = "/webfilesys/images/miniDisk.gif";
              }
              else
              {
                  currentFolderImg.src = "/webfilesys/images/folder.gif";
              }
              currentDirId = newSelectedDiv.id;
              return true;
          }
      }
      return false;
  }

  function downToOpenChild(currentDirDiv) 
  {
      var childNum = currentDirDiv.childNodes.length;

      if (childNum > 0)
      {
          var indentDiv = currentDirDiv.childNodes[childNum - 1];

          var i = childNum - 2;

          while ((i >= 0) && (indentDiv.nodeType != 1))
          {
              indentDiv = currentDirDiv.childNodes[i];
              i--;
          }
          
          if (indentDiv.tagName == "DIV")
          {
              var dirDiv = indentDiv.firstChild;
          
              if (dirDiv)
              {
                  if (dirDiv.id == "")
                  {
                      dirDiv = dirDiv.firstChild;
                  
                      if (dirDiv && (dirDiv.nodeType != 1))
                      {
                          // for Chrome
                          dirDiv = dirDiv.nextSibling;
                      }
                  }
              }

              if (dirDiv)
              {
                  var nextFolderImg = locateFolderImage(dirDiv);
            
                  if (nextFolderImg)
                  {
                      nextFolderImg.src = "/webfilesys/images/folder1.gif";
                      
                      var currentFolderImg = locateFolderImage(currentDirDiv);
                      
                      if (currentFolderImg) 
                      {
                          currentFolderImg.src = "/webfilesys/images/folder.gif";
                          currentDirId = dirDiv.id;
                          return true;
                      }
                  }
              }
          }
      }   
      
      return false;
  }

  function locateFolderImage(divElem)
  {
      var child = divElem.firstChild; // first a element
                  
      if (child) 
      {
          if (child.nodeType != 1)
          {
              child = child.nextSibling;
          }
      
          var folderImg = child.firstChild;

          if (folderImg) 
          {
              var oldSrc = folderImg.src;
                          
              if ((oldSrc.indexOf("folder") > 0) || (oldSrc.indexOf("miniDisk") > 0))
              {
                  return folderImg;
              }
          }

          child = child.nextSibling;  // next a element
                              
          if (child)
          {
              folderImg = child.firstChild;
                              
              if (folderImg) 
              {
                  var oldSrc = folderImg.src;

                  if ((oldSrc.indexOf("folder") > 0) || (oldSrc.indexOf("miniDisk") > 0))
                  {
                      return folderImg;
                  }
              }
          }
      }
  }