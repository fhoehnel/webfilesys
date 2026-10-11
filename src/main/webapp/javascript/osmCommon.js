function createOSMBaseLayer() {
    // OpenLayers 2.13's OSM layer forces crossOriginKeyword "anonymous" on every
    // tile, which adds a crossorigin attribute to the tile <img> and makes the
    // browser enforce CORS. The OSM tile servers don't send an
    // "Access-Control-Allow-Origin" header, so the tiles are blocked. Setting
    // crossOriginKeyword to null removes the attribute and loads the tiles as
    // plain images. The a/b/c.tile.openstreetmap.org subdomains now issue a 301
    // redirect to the canonical tile.openstreetmap.org host, so that host is used
    // directly.
    return new OpenLayers.Layer.OSM("OpenStreetMap", [
        "https://tile.openstreetmap.org/${z}/${x}/${y}.png"
    ], {
        tileOptions: { crossOriginKeyword: null }
    });
}
