import Capsule from "./capsule";
import UnifiWsClient from "./lib/unifi-ws-client";

const LOG_PREFIX_DEBUG   = "[DEBUG]",
      LOG_PREFIX_ERROR   = "[ERROR]",
      LOG_PREFIX_INFO    = "[INFO]",
      LOG_PREFIX_WARNING = "[WARNING]";

//Get environment variables and define defaults:
var env = require("env-variable")({
    CAPSULE_BRIDGE_CLIENT_ID:         "test-club",
    CAPSULE_BRIDGE_OPERATOR_USERNAME: "test",
    CAPSULE_BRIDGE_OPERATOR_PASSWORD: "test",
    CAPSULE_BRIDGE_WEBSOCKET_URI:     "ws://127.0.0.1:8000/service/json",
    CAPSULE_BRIDGE_WEBSOCKET_TYPE:    "json"
});

var config = {
    clientId:         env.CAPSULE_BRIDGE_CLIENT_ID,
    operatorUsername: env.CAPSULE_BRIDGE_OPERATOR_USERNAME,
    operatorPassword: env.CAPSULE_BRIDGE_OPERATOR_PASSWORD,
    websocketUri:     env.CAPSULE_BRIDGE_WEBSOCKET_URI,
    websocketType:    env.CAPSULE_BRIDGE_WEBSOCKET_TYPE
};
console.info(`${LOG_PREFIX_INFO} Configuration: ${JSON.stringify(config)}`);

const CAPSULE_API_KEY = "2CHDNqokGLBzmYXUa6Ce62S2WhT26OhzJnsOl7bnQZ0YWsU5J3GN4yzEHY/h6ywK";

async function fullSync() {
    console.info(`${LOG_PREFIX_INFO} Full sync started at ${new Date()}`);

    // Get the Capsule data.
    const capsule = new Capsule(CAPSULE_API_KEY);
    const persons = await capsule.getPersons();

    // Connect to the Unifi.id service.
    let unifi = new UnifiWsClient({
        url: config.websocketUri,
        type: config.websocketType
    });
    await unifi.connect();
    unifi.start();

    // Authenticate as a system operator.
    unifi.request({
        "messageType": "core.operator.auth-password",
        "payload": {
            "clientId": config.clientId,
            "username": config.operatorUsername,
            "password": config.operatorPassword
        }
    },
    (authResponse) => {
        console.debug(`${LOG_PREFIX_DEBUG} ${JSON.stringify(authResponse)}`);
        console.debug(`${LOG_PREFIX_DEBUG} Collected ${persons.length} persons.`);

        persons.forEach((person) => {

            // Don't add person if they don't have a Mifare number.
            if (!person.mifareNumber) {
                return;
            }

            // Define values here that will be sent through the unifi.id API.
            let holder = {
                clientReference: person.id.toString(),
                name: `${person.firstName} ${person.lastName}`
            };
            let detectable = {
                detectableId: person.mifareNumber,
                description: ""
            };

            // Sanitise any values here that won't be primary keys in the
            // unifi-core database.
            (function sanitiseKeys() {

                // holder.name
                if (holder.name.length > 64) {
                    let newName = holder.name.subString(0, 64);
                    console.warning(`${LOG_PREFIX_WARNING} Name exceeds 64 character size limit, trimming: ${holder.name} → ${newName}`);
                    holder.name = newName;
                }

            })();

            let primaryKeysInvalid = (function() {

                // Define values here that will be set as primary key values
                // in the unifi-core database.
                let unifiPrimaryKeys = {
                    "holder_clientReference": holder.clientReference,
                    "detectable_detectableId": detectable.detectableId
                };

                let errorEncountered = false;
                Object.keys(unifiPrimaryKeys).forEach((key) => {
                    if (unifiPrimaryKeys[key].length > 64) {
                        console.error(`${LOG_PREFIX_ERROR} Key '${key.replace("_", ".")}' longer than 64 characters (${unifiPrimaryKeys[key]}). Person will not be added. clientReference: ${holder.clientReference}`);
                        errorEncountered = true;
                    }
                });
                return errorEncountered;

            })();

            // End processing this person if there's an error with the
            // primary keys.
            if (primaryKeysInvalid) {
                return;
            }

            unifi.request({
                "messageType": "core.holder.add-holder",
                "payload": {
                    "clientId": config.clientId,
                    "clientReference": holder.clientReference,
                    "holderType": "contact",
                    "name": holder.name,
                    "active": true,
                    "image": null,
                    "metadata": null
                }
            },
            (response) => {
                console.debug(`${LOG_PREFIX_DEBUG} ${JSON.stringify(response)}`);
                if (response.messageType === "core.error.already-exists") {

                    unifi.request({
                        "messageType": "core.holder.edit-holder",
                        "payload": {
                            "clientId": config.clientId,
                            "clientReference": holder.clientReference,
                            "holderType": "contact",
                            "changes": {
                                "name": holder.name,
                                "active": true,
                                "image": null,
                                "metadata": null
                            }
                        }
                    },
                    (response) => {
                        console.debug(`${LOG_PREFIX_DEBUG} ${JSON.stringify(response)}`);
                    });

                }
            });

            unifi.request({
                "messageType": "core.detectable.add-detectable",
                "payload": {
                    "clientId": config.clientId,
                    "detectableId": detectable.detectableId,
                    "detectableType": "mifare-csn",
                    "description": detectable.description,
                    "active": true,
                    "assignment": null
                }
            },
            (response) => {
                console.debug(response);
                if (response.messageType === "core.error.already-exists") {

                    unifi.request({
                        "messageType": "core.detectable.edit-detectable",
                        "payload": {
                            "clientId": config.clientId,
                            "detectableId": detectable.detectableId,
                            "detectableType": "mifare-csn",
                            "changes": {
                                "description": detectable.description,
                                "active": true,
                                "assignment": null
                            }
                        }
                    },
                    (response) => {
                        console.debug(`${LOG_PREFIX_DEBUG} ${JSON.stringify(response)}`);
                    });

                }
            });
        });
    });

    console.info(`${LOG_PREFIX_INFO} Full sync ended at ${new Date()}`);
    // Queue up the next sync in 10 minutes.
    setTimeout(fullSync, 600000);
}

fullSync().catch((e) => console.error(e));