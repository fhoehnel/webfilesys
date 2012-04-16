function mobileFunctionSelected() 
{
    var functionSelect = document.getElementById("functionSelect");
    
    var idx = functionSelect.selectedIndex;

    var cmd = functionSelect.options[idx].value;

    if (cmd == '0') 
    {
        return;
    }

    if (cmd == '1')
    {
        window.location.href = "/webfilesys/servlet?command=bookmarks";
        return;
    }

    if (cmd == '2')
    {
        window.open('/webfilesys/servlet?command=versionInfo','infowindow','status=no,toolbar=no,location=no,menu=no,width=300,height=220,resizable=no,left=50,top=20,screenX=50,screenY=20');
    }
    
    if (cmd == '3')
    {
        window.location.href = "/webfilesys/servlet";
        return;
    }
    
    if (cmd == '4')
    {
        window.location.href = "/webfilesys/servlet?command=logout";
        return;
    }
    
    functionSelect.selectedIndex = 0;
}