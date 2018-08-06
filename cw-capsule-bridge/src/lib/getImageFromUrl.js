import Axios from "axios";
import btoa from "btoa";
import filterImagePlaceholders from "./filterImagePlaceholders";
import Log from "./../log";

async function getImageFromUrl (pictureURL) {
    return new Promise((resolve) => {
        if (filterImagePlaceholders(pictureURL)) {
            Axios.get(pictureURL, {
                responseType: "arraybuffer"
            }).then((response) => {
                resolve(btoa(response.data));
            }).catch((error) => {
                Log.error(`${JSON.stringify(error)}`);
                resolve(null);
            });
        }
    });
}

export default getImageFromUrl;
