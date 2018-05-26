import Axios from "axios";
import LinkHeader from "http-link-header";

const LOG_PREFIX_DEBUG   = "[DEBUG]",
      LOG_PREFIX_ERROR   = "[ERROR]",
      LOG_PREFIX_INFO    = "[INFO]",
      LOG_PREFIX_WARNING = "[WARNING]";

const CAPSULE_PARTIES_URI = "https://api.capsulecrm.com/api/v2/parties?perPage=100&embed=tags,fields,organisation";
const CAPSULE_FIELD_MIFARENUMBER = 216824,
      CAPSULE_FIELD_RTCONTACTID  = 371942;
export default class Capsule {
    constructor(apiKey) {
        this.apiKey = apiKey;
        this._transformPerson = this._transformPerson.bind(this);
    }
    getPersons() {
        return this.getAllParties()
            .then((parties) => parties.filter(this._isPerson))
            .then((persons) => persons.map(this._transformPerson));
    }
    getAllParties() {
        return new Promise((resolve, reject) => {
            this._getPartyPages([], CAPSULE_PARTIES_URI, resolve, reject);
        });

    }
    _getPartyPages(soFar, uri, resolve, reject) {
        console.debug(`${LOG_PREFIX_DEBUG} ${soFar.length} parties, fetching '${uri}' ...`);
        Axios.get(uri, {
            headers: {
                Authorization: "Bearer " + this.apiKey,
                Accept: "application/json"
            }
        }).then((response) => {
            let nextLink = LinkHeader.parse(response.headers.link).get("rel", "next")[0];
            let parties = soFar.concat(response.data.parties);
             if (nextLink) {
                 this._getPartyPages(parties, nextLink.uri, resolve, reject);
             } else {
                resolve(parties);
             }
        }).catch((error) => {
            console.error(`${LOG_PREFIX_ERROR} ${JSON.stringify(error)}`);
            resolve([]);
        });
    }
    _isPerson(party) {
        return party.type === "person";
    }
    _transformPerson(person) {
        return {
            ...person,
            mifareNumber: this._getFieldValue(person, CAPSULE_FIELD_MIFARENUMBER, ""),
            rtContactId:  this._getFieldValue(person, CAPSULE_FIELD_RTCONTACTID,  "")
        };
    }
    _getFieldValue(entity, definitionId, defaultValue) {
        return (entity.fields.find((f) => f.definition.id === definitionId) || {}).value || defaultValue;
    }
}