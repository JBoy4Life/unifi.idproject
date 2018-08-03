import filterPlaceholders from "./filterPlaceholders";

function getImageFromUrl(pictureURL) {
    if(filterPlaceholders(pictureURL)) {
        return;
    }
    var XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest;
    var oReq = new XMLHttpRequest();

    oReq.open("GET", pictureURL, true);
    oReq.responseType = "arraybuffer";

    return oReq.response;

}

export default getImageFromUrl;
