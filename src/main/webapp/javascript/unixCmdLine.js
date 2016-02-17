function submitCmd()
{
    var ajaxUrl = '/webfilesys/servlet';

    xmlRequestPost(ajaxUrl, getFormData(document.form1), showCmdOutput);
}

function showCmdOutput(req)
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
            var item = req.responseXML.getElementsByTagName("cmdOutput")[0];            
            var stdout = item.firstChild.nodeValue;
             
            var cmdOutDiv = document.getElementById('cmdOutput');
            cmdOutDiv.innerHTML = '<pre>' + stdout + '</pre>';
        }
    }
}