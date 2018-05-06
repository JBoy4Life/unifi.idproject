import Capsule from "./capsule";
import UnifiWsClient from "./lib/unifi-ws-client";

const CAPSULE_API_KEY = "2CHDNqokGLBzmYXUa6Ce62S2WhT26OhzJnsOl7bnQZ0YWsU5J3GN4yzEHY/h6ywK";

async function fullSync() {
    console.debug("Syncing at " + (new Date()));

    // Get the Capsule data.
    const capsule = new Capsule(CAPSULE_API_KEY);
    const persons = await capsule.getPersons();

    // Connect to the Unifi.id service.
    let unifi = new UnifiWsClient({
        url: "ws://192.168.0.23:8000/service/json",
        type: "json"
    });
    await unifi.connect();
    unifi.start();

    // Authenticate as a system operator.
    unifi.request({
        "messageType": "core.operator.auth-password",
        "payload": {
            "clientId": "test-club",
            "username": "test",
            "password": "test"
        }
    }, (authResponse) => {
        console.debug(authResponse);
        persons.forEach((person) => {
            unifi.request({
                "messageType": "core.holder.add-holder",
                "payload": {
                    "clientId": "test-club",
                    "clientReference": person.id.toString(),
                    "holderType": "contact",
                    "name": person.firstName + " " + person.lastName,
                    "active": true,
                    "image": null
                }
            }, (response) => {
                console.debug(response);
            });
        });
    });

    // Queue up the next sync in 10 minutes.
    setTimeout(fullSync, 600000);
}

fullSync().catch((e) => console.error(e));