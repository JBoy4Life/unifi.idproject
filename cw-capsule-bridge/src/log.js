function Log() {};

Log.prototype.debug = function debug(message) {
    console.debug(`[DEBUG] ${message}`);
};

Log.prototype.error = function error(message) {
    console.error(`[ERROR] ${message}`);
};

Log.prototype.info = function info(message) {
    console.info(`[INFO] ${message}`);
};

Log.prototype.warning = function warning(message) {
    console.warning(`[WARNING] ${message}`);
};

export default new Log();