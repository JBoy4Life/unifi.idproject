import unionBy from 'lodash/unionBy'
import { ZONE_ENTITIES_SUBSCRIBE, ZONE_LIST_FETCH, ZONE_LIST_HOLDERS_FETCH } from './types'

const initialState = {
  holdersInfo: {},
  zonesInfo: {},
  liveDiscovery: [],
}

const referenceMap = (array, targetkey) =>
  Array.isArray(array) ? array.reduce((acc, item) => {
    acc[item[targetkey]] = item
    return acc
  }, {}) : {}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${ZONE_LIST_HOLDERS_FETCH}_FULFILLED`:
      return {
        ...state,
        holdersInfo: referenceMap(action.payload.payload, 'clientReference'),
      }

    case `${ZONE_LIST_FETCH}_FULFILLED`:
      return {
        ...state,
        zonesInfo: referenceMap(action.payload.payload, 'zoneId'),
      }

    case `${ZONE_ENTITIES_SUBSCRIBE}_UPDATE`:
      return {
        ...state,
        liveDiscoveryUpdate: new Date().getTime(),
        liveDiscovery: unionBy(
          action.data.payload,
          state.liveDiscovery,
          'clientReference'
        ),
      }
    default:
      return state
  }
}

export default reducer
