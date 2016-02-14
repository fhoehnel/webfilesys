function ajaxRotate(path, degrees, domId)
{
    hideMenu();

    var xmlUrl = '/webfilesys/servlet?command=xformImage&action=rotate&degrees=' + degrees + '&imgName=' + encodeURIComponent(path) + '&domId=' + domId;

    var xslUrl = "/webfilesys/xsl/transformImageResult.xsl";

    var tdElement = document.getElementById(domId);
        
    if (!tdElement)
    {
        alert('td element with DOM id ' + domId + ' not found');
        return;
    }
    
    htmlFragmentByXslt(xmlUrl, xslUrl, tdElement);
}

function autoImgRotate()
{
    if (!confirm(resourceBundle["confirm.rotateByExif"]))
    {
        return;
    }

    showHourGlass();

    var xmlUrl = '/webfilesys/servlet?command=autoImgRotate';

    var responseXml = xmlRequest(xmlUrl, autoImgRotateResult);
}

function autoImgRotateResult()
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
             var anyRotatedItem = req.responseXML.getElementsByTagName("anyImageRotated")[0];            
             var anyRotated = anyRotatedItem.firstChild.nodeValue;
                
             hideHourGlass();
                          
             if (anyRotated == "true")
             {
                 window.location.href = '/webfilesys/servlet?command=thumbnail';
                 return
             }
             
             alert(resourceBundle["rotateByExif.noop"]);
        }
    }
}


