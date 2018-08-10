import Console from "console";

function Log() {}

Log.debug = function (message) {
    // Console.debug(`<7>[DEBUG] ${message}`);
};

Log.error = function (message) {
    Console.error(`<3>[ERROR] ${message}`);
};

Log.info = function (message) {
    Console.info(`<6>[INFO] ${message}`);
};

Log.notice = function (message) {
    Console.log(`<5>[NOTICE] ${message}`);
};

Log.warning = function (message) {
    Console.warn(`<4>[WARNING] ${message}`);
};

export default Log;
