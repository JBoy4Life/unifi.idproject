function Log() {};

const LOG_PREFIX_DEBUG   = "[DEBUG]",
      LOG_PREFIX_ERROR   = "[ERROR]",
      LOG_PREFIX_INFO    = "[INFO]",
      LOG_PREFIX_WARNING = "[WARNING]";

Log.prototype.debug = function debug(message) {
    console.debug(`${LOG_PREFIX_DEBUG} ${message}`);
};

Log.prototype.error = function error(message) {
    console.error(`${LOG_PREFIX_ERROR} ${message}`);
};

Log.prototype.info = function info(message) {
    console.info(`${LOG_PREFIX_INFO} ${message}`);
};

Log.prototype.warning = function warning(message) {
    console.warning(`${LOG_PREFIX_WARNING} ${message}`);
};

export default new Log();