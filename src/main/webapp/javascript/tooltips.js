function setTooltips() {
    $(".folderTreeIcon").attr("title", folderTip);

    const linkCount = document.links.length;
      
    for (let i = 0; i < linkCount; i++) {
        let link = document.links[i];
        if (link.href.indexOf("listFiles") >= 0) {
      	    if (link.title.length === 0) {
                link.title = listTip;
            }
      	}
    }
}
