//Get environment variables and define defaults:
var env = require("env-variable")({
    CAPSULE_BRIDGE_API_KEY:           "2CHDNqokGLBzmYXUa6Ce62S2WhT26OhzJnsOl7bnQZ0YWsU5J3GN4yzEHY/h6ywK",
    CAPSULE_BRIDGE_CLIENT_ID:         "test-club",
    CAPSULE_BRIDGE_OPERATOR_USERNAME: "test",
    CAPSULE_BRIDGE_OPERATOR_PASSWORD: "test",
    CAPSULE_BRIDGE_WEBSOCKET_URI:     "ws://127.0.0.1:8000/service/json",
    CAPSULE_BRIDGE_WEBSOCKET_TYPE:    "json"
});

import Capsule from "./capsule";
//import getImageFromUrl from "./getImageFromUrl";
import Log from "./log";
import UnifiWsClient from "./lib/unifi-ws-client";
import mifareUhfMappings from "./res/mifare-uhf-mappings.json";

var config = {
    apiKey:           env.CAPSULE_BRIDGE_API_KEY,
    clientId:         env.CAPSULE_BRIDGE_CLIENT_ID,
    operatorUsername: env.CAPSULE_BRIDGE_OPERATOR_USERNAME,
    operatorPassword: env.CAPSULE_BRIDGE_OPERATOR_PASSWORD,
    websocketUri:     env.CAPSULE_BRIDGE_WEBSOCKET_URI,
    websocketType:    env.CAPSULE_BRIDGE_WEBSOCKET_TYPE
};
Log.info(`Configuration: ${JSON.stringify(config)}`);


async function fullSync() {
    Log.info(`Full sync started at ${new Date()}`);

    // Get the Capsule data.
    const capsule = new Capsule(config.apiKey);
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
        Log.debug(`${JSON.stringify(authResponse)}`);
        Log.debug(`Collected ${persons.length} persons.`);

        persons.forEach((person) => {

            // Don't add person if they don't have a Mifare number.
            if (!person.mifareNumber) {
                Log.debug(`Skipping person: No Mifare. Data: ${JSON.stringify(person)}`);
                return;
            }

            // Define values here that will be sent through the unifi.id API.
            let holder = {
                clientReference: person.id.toString(),
                name: `${person.firstName} ${person.lastName}`,
                image: getImageFromUrl(person.pictureURL),
                metadata: {
                    "club": person.club,
                    "memberType": person.memberType
                }
            };
            let mifare = {
                detectableId: person.mifareNumber,
                description: ""
            };
            let uhf;
            if (person.mifareNumber in mifareUhfMappings) {
                uhf = {
                    detectableId: mifareUhfMappings[person.mifareNumber],
                    description: ""
                };
            }
            else {
                Log.warning(`Skipping person: No matching UHF found. Data: ${JSON.stringify(person)}`);
                return;
            }

            // Sanitise any values here that won't be primary keys in the
            // unifi-core database.
            (function sanitiseKeys() {

                // holder.name
                if (holder.name.length > 64) {
                    let newName = holder.name.subString(0, 64);
                    Log.warning(`Name exceeds 64 character size limit, trimming: ${holder.name} → ${newName}`);
                    holder.name = newName;
                }

            })();

            let primaryKeysInvalid = (function() {

                // Define values here that will be set as primary key values
                // in the unifi-core database.
                let unifiPrimaryKeys = {};
                unifiPrimaryKeys["holder_clientReference"] = holder.clientReference;
                unifiPrimaryKeys["mifare_detectableId"] = mifare.detectableId;
                if (uhf !== undefined) {
                    unifiPrimaryKeys["uhf_detectableId"] = uhf.detectableId;
                }

                let errorEncountered = false;
                Object.keys(unifiPrimaryKeys).forEach((key) => {
                    if (unifiPrimaryKeys[key].length > 64) {
                        Log.error(`Key '${key.replace("_", ".")}' longer than 64 characters (${unifiPrimaryKeys[key]}). Person will not be added. clientReference: ${holder.clientReference}`);
                        errorEncountered = true;
                    }
                });
                return errorEncountered;

            })();

            // End processing this person if there's an error with the
            // primary keys.
            if (primaryKeysInvalid) {
                Log.warning(`Skipping person: Primary key value invalid. Data: ${JSON.stringify(person)}`);
                return;
            }

            var assertHolder = function () {

                unifi.request({
                    "messageType": "core.holder.add-holder",
                    "payload": {
                        "clientId": config.clientId,
                        "clientReference": holder.clientReference,
                        "holderType": "contact",
                        "name": holder.name,
                        "active": true,
                        "image": holder.image,
                        "metadata": holder.metadata
                    }
                },
                (response) => {
                    Log.debug(`${JSON.stringify(response)}`);
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
                                    "image": holder.image,
                                    "metadata": holder.metadata
                                }
                            }
                        },
                        (response) => {
                            Log.debug(`${JSON.stringify(response)}`);
                            assertDetectables();
                        });

                    }
                    else {
                        assertDetectables();
                    }
                });

            };

            var assertDetectables = function () {

                unifi.request({
                    "messageType": "core.detectable.add-detectable",
                    "payload": {
                        "clientId": config.clientId,
                        "detectableId": mifare.detectableId,
                        "detectableType": "mifare-csn",
                        "description": mifare.description,
                        "active": true,
                        "assignment": holder.clientReference
                    }
                },
                (response) => {
                    Log.debug(`${JSON.stringify(response)}`);
                    if (response.messageType === "core.error.already-exists") {

                        unifi.request({
                            "messageType": "core.detectable.edit-detectable",
                            "payload": {
                                "clientId": config.clientId,
                                "detectableId": mifare.detectableId,
                                "detectableType": "mifare-csn",
                                "changes": {
                                    "description": mifare.description,
                                    "active": true,
                                    "assignment": holder.clientReference
                                }
                            }
                        },
                        (response) => {
                            Log.debug(`${JSON.stringify(response)}`);
                        });

                    }
                });

                if (uhf !== undefined) {

                    unifi.request({
                        "messageType": "core.detectable.add-detectable",
                        "payload": {
                            "clientId": config.clientId,
                            "detectableId": uhf.detectableId,
                            "detectableType": "uhf-epc",
                            "description": uhf.description,
                            "active": true,
                            "assignment": holder.clientReference
                        }
                    },
                    (response) => {
                        Log.debug(`${JSON.stringify(response)}`);
                        if (response.messageType === "core.error.already-exists") {

                            unifi.request({
                                "messageType": "core.detectable.edit-detectable",
                                "payload": {
                                    "clientId": config.clientId,
                                    "detectableId": uhf.detectableId,
                                    "detectableType": "uhf-epc",
                                    "changes": {
                                        "description": uhf.description,
                                        "active": true,
                                        "assignment": holder.clientReference
                                    }
                                }
                            },
                            (response) => {
                                Log.debug(`${JSON.stringify(response)}`);
                            });

                        }
                    });

                }

            };

            assertHolder();

        });
    });

    Log.info(`Full sync ended at ${new Date()}`);
    // Queue up the next sync in 10 minutes.
    setTimeout(fullSync, 600000);
}

fullSync().catch((e) => Log.error(e));
