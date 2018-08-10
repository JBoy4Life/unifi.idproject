import Log from "./../log";

var ignoreUrlContains = "facehub";

function filterPlaceholders (pictureURL) {
    if (pictureURL.includes(ignoreUrlContains)) {
        Log.info(`SKIPPING PLACEHOLDER IMAGE "${pictureURL}"`);
        return false;
    }
    else {
        return true;
    }
}

export default filterPlaceholders;
