import moment from 'moment'
import find from 'lodash/find'
import unionBy from 'lodash/unionBy'
import {
  ZONE_ENTITIES_SUBSCRIBE,
  ZONE_LIST_FETCH,
  ZONE_ENTITIES_CLEAR_INACTIVE
} from './types'

import { ZONE_ENTITIES_INACTIVE_THRESHOLD } from 'config/constants'
import { referenceMap } from 'utils/helpers'

const initialState = {
  holdersInfo: {},
  zonesInfo: {},
  liveDiscovery: [],
}

const filterOutInactiveEntities = (liveDiscovery) =>
  liveDiscovery.filter(item => {
    return moment().diff(moment(item.detectionTime)) < ZONE_ENTITIES_INACTIVE_THRESHOLD
  })

const mergeDiscoveryUpdate = (currentDiscoveries, newDiscoveries) =>
  unionBy(
    newDiscoveries.map(item => {
      const foundItem = find(currentDiscoveries, { clientReference: item.clientReference })
      return foundItem && item.zoneId === foundItem.zoneId ? {
        ...item,
        firstDetectionTime: foundItem.firstDetectionTime
      } : {
        ...item,
        firstDetectionTime: item.detectionTime
      }
    }),
    currentDiscoveries,
    'clientReference'
  )

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${ZONE_LIST_FETCH}_FULFILLED`:
      return {
        ...state,
        zonesInfo: referenceMap(action.payload.payload, 'zoneId'),
      }

    case `${ZONE_ENTITIES_SUBSCRIBE}_UPDATE`:
      return {
        ...state,
        liveDiscoveryUpdate: new Date().getTime(),
        liveDiscovery: mergeDiscoveryUpdate(state.liveDiscovery, action.data.payload)
      }

    case ZONE_ENTITIES_CLEAR_INACTIVE:
      return {
        ...state,
        liveDiscovery: filterOutInactiveEntities(state.liveDiscovery)
      }
    default:
      return state
  }
}

export default reducer
