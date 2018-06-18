function Log() {}

Log.debug = function (message) {
    // console.debug(`<7> [DEBUG] ${message}`);
};

Log.error = function (message) {
    console.error(`<3> [ERROR] ${message}`);
};

Log.info = function (message) {
    console.info(`<6> [INFO] ${message}`);
};

Log.warning = function (message) {
    console.warn(`<4> [WARNING] ${message}`);
};

export default Log;
