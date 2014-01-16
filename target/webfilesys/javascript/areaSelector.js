var dragObject;
var resizeObject;

var editPictureId;

// Position, an der das Objekt angeklickt wurde.
var dragx;
var dragy;

var resizeLastX;
var resizeLastY;

// Mausposition
var posx;
var posy;

var areaSelectorWidth;
var areaSelectorHeight;

var pictureLeftBorderPos;
var pictureRightBorderPos;
var pictureTopBorderPos;
var pictureBottomBorderPos;

var pictureWidth;
var pictureHeight;
  
function initAreaSelector(pictureId) 
{
    dragObject = null;
    resizeObject = null;

    editPictureId = null;

    dragx = 0;
    dragy = 0;

    resizeLastX = 0;
    resizeLastY= 0;

    posx = 0;
    posy = 0;

    var picture = document.getElementById(pictureId);
    
    picturePos = findPos(picture);
    
    pictureWidth = picture.width;
    pictureHeight = picture.height;
    
    areaSelectorWidth = pictureWidth - 40;
    areaSelectorHeight = pictureHeight - 40;
  
    // position of the borders of the picture in the document
    pictureLeftBorderPos = picturePos[0];
    pictureTopBorderPos = picturePos[1];
    pictureRightBorderPos = pictureLeftBorderPos + pictureWidth;
    pictureBottomBorderPos = pictureTopBorderPos + pictureHeight;
    
    editPictureId = pictureId;
  
    var areaSelector = document.getElementById('areaSelector');
  
    areaSelector.style.left = (picturePos[0] + 20) + 'px';
    areaSelector.style.top = (picturePos[1] + 20) + 'px';
    areaSelector.style.width = areaSelectorWidth + 'px';
    areaSelector.style.height = areaSelectorHeight + 'px';
      
    areaSelector.style.visibility = 'visible';

    var selectedAreaDiv = document.getElementById('selectedArea');
      
    selectedAreaDiv.style.width = (areaSelectorWidth - 2) + 'px';
    selectedAreaDiv.style.height = (areaSelectorHeight - 2) + 'px';
    
    selectedAreaDiv.style.visibility = 'visible';
      
    draginit();  
}

function draginit() 
{
    document.onmousemove = drag;
    document.onmouseup = dragstop;
}

function startDrag(element) 
{
    resizeObject = null;
    dragObject = document.getElementById('areaSelector');
    dragx = posx - dragObject.offsetLeft;
    dragy = posy - dragObject.offsetTop;
  
    return true;
}

function dragstop() 
{
    dragObject=null;
    resizeObject=null;
}

function drag(ereignis) 
{
    posx = document.all ? window.event.clientX : ereignis.pageX;
    posy = document.all ? window.event.clientY : ereignis.pageY;
        
    if (dragObject != null) 
    {
        var selNewXpos = posx - dragx;
        var selNewYpos = posy - dragy;
        if ((selNewXpos >= pictureLeftBorderPos) && (selNewYpos >= pictureTopBorderPos) &&
            (selNewXpos + parseInt(dragObject.style.width) < pictureRightBorderPos) &&
            (selNewYpos + parseInt(dragObject.style.height) < pictureBottomBorderPos))
        {
            dragObject.style.left = (posx - dragx) + "px";
            dragObject.style.top = (posy - dragy) + "px";
        }
    } 
    else 
    {
        if (resizeObject != null) 
        {
            resize(ereignis);
        }
    }
}

function startResize(element, event) 
{
    dragObject = null;
    resizeObject = element;
    resizeLastX = document.all ? window.event.clientX : event.pageX;
    resizeLastY = document.all ? window.event.clientY : event.pageY;
}

function resize(event) 
{
    var areaSelector = document.getElementById('areaSelector');
    var selectedArea = document.getElementById('selectedArea');
    
    var oldLeft = parseInt(areaSelector.style.left);
    var oldRight = oldLeft + parseInt(selectedArea.style.width);
    
    var leftDist = Math.abs(posx - oldLeft);
    var rightDist = Math.abs(posx - oldRight);
    
    if (rightDist < leftDist) 
    {
        // moving right edge
        if (posx > resizeLastX)
        {
            if (posx <= pictureRightBorderPos)
            {
                areaSelector.style.width = (parseInt(areaSelector.style.width) + 1) + 'px';
                selectedArea.style.width = (parseInt(selectedArea.style.width) + 1) + 'px';
            }
        }
        if (posx < resizeLastX)
        {
            areaSelector.style.width = (parseInt(areaSelector.style.width) - 1) + 'px';
            selectedArea.style.width = (parseInt(selectedArea.style.width) - 1) + 'px';
        }
    }
    else
    {
        // moving left edge
        if (posx > resizeLastX) 
        {
            areaSelector.style.left = (parseInt(areaSelector.style.left) + 1) + 'px';
            areaSelector.style.width = (parseInt(areaSelector.style.width) - 1) + 'px';

            selectedArea.style.width = (parseInt(selectedArea.style.width) - 1) + 'px';
        }
        if (posx < resizeLastX)
        {
            if (posx >= pictureLeftBorderPos)
            {
                areaSelector.style.left = (parseInt(areaSelector.style.left) - 1) + 'px';
                areaSelector.style.width = (parseInt(areaSelector.style.width) + 1) + 'px';

                selectedArea.style.width = (parseInt(selectedArea.style.width) + 1) + 'px';
            }        
        }
    }

    var oldTop = parseInt(areaSelector.style.top);
    var oldBottom = oldTop + parseInt(selectedArea.style.height);
    
    var topDist = Math.abs(posy - oldTop);
    var bottomDist = Math.abs(posy - oldBottom);
    
    if (topDist > bottomDist) 
    {
        // moving lower edge
        if (posy > resizeLastY)
        {
            if (posy <= pictureBottomBorderPos)
            {
                areaSelector.style.height = (parseInt(areaSelector.style.height) + 1) + 'px';
                selectedArea.style.height = (parseInt(selectedArea.style.height) + 1) + 'px';
            }
        }
        if (posy < resizeLastY)
        {
            areaSelector.style.height = (parseInt(areaSelector.style.height) - 1) + 'px';
            selectedArea.style.height = (parseInt(selectedArea.style.height) - 1) + 'px';
        }
    }
    else
    {
        // moving upper edge
        if (posy > resizeLastY) 
        {
            areaSelector.style.top = (parseInt(areaSelector.style.top) + 1) + 'px';
            areaSelector.style.height = (parseInt(areaSelector.style.height) - 1) + 'px';

            selectedArea.style.height = (parseInt(selectedArea.style.height) - 1) + 'px';
        }
        if (posy < resizeLastY)
        {
            if (posy >= pictureTopBorderPos)
            {
                areaSelector.style.top = (parseInt(areaSelector.style.top) - 1) + 'px';
                areaSelector.style.height = (parseInt(areaSelector.style.height) + 1) + 'px';

                selectedArea.style.height = (parseInt(selectedArea.style.height) + 1) + 'px';
            }        
        }
    }

    resizeLastX = posx;
    resizeLastY = posy;
}

function findPos(obj) 
{
    var curleft = curtop = 0;

    if (obj.offsetParent) 
    {
        do 
        {
   	    curleft += obj.offsetLeft;
	    curtop += obj.offsetTop;
        } 
        while (obj = obj.offsetParent);
    }
	
    return [curleft,curtop];
}

function showSelectedArea()
{
    var picture = document.getElementById(editPictureId);

    picturePos = findPos(picture);
    
    var pictureXpos = picturePos[0];
    var pictureYpos = picturePos[1];
    
    var selectorLeft = parseInt(areaSelector.style.left);
    var selectorTop = parseInt(areaSelector.style.top);

    var pictureLeft = selectorLeft - pictureXpos;   
    var pictureTop = selectorTop - pictureYpos;
    var pictureWidth = parseInt(areaSelector.style.width);   
    var pictureHeight = parseInt(areaSelector.style.height);
    
    alert('picture left=' + pictureLeft + ' top=' + pictureTop + ' width=' + pictureWidth + ' height= ' + pictureHeight);
}

