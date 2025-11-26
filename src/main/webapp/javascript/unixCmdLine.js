function submitCmd() {
    xmlFetchPost(getFormData(document.form1), responseXml => {
        const item = responseXml.getElementsByTagName("cmdOutput")[0];
        const stdout = item.firstChild.nodeValue;
             
        const cmdOutDiv = document.getElementById('cmdOutput');
        cmdOutDiv.innerHTML = '<pre>' + stdout + '</pre>';
    });
}
