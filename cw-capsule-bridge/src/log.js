function Log() {}

Log.debug = function debug(message) {
    // console.debug(`[DEBUG] ${message}`);
};

Log.error = function error(message) {
    console.error(`[ERROR] ${message}`);
};

Log.info = function info(message) {
    console.info(`[INFO] ${message}`);
};

Log.warning = function warning(message) {
    console.warn(`[WARNING] ${message}`);
};

export default Log;
