import Console from "console";

function Log() {};

Log.debug = function debug(message) {
    Console.debug(`[DEBUG] ${message}`);
};

Log.error = function error(message) {
    Console.error(`[ERROR] ${message}`);
};

Log.info = function info(message) {
    Console.info(`[INFO] ${message}`);
};

Log.warning = function warning(message) {
    Console.warn(`[WARNING] ${message}`);
};

export default Log;
