import Log from "./../log";

var ignoreUrlContains = "facehub";

function filterPlaceholders (pictureURL) {
    if (pictureURL.includes(ignoreUrlContains)) {
        Log.info(`SKIPPING PLACEHOLDER IMAGE "${pictureURL}"`);
        return true;
    }
    else {
        return false;
    }
}

export default filterPlaceholders;
