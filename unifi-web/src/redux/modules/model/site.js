import moment from 'moment'
import find from 'lodash/find'
import unionBy from 'lodash/unionBy'

import createWsAction from 'utils/create-ws-action'
import { liveDiscoverySelector } from 'redux/selectors'
import { referenceMap } from 'utils/helpers'
import { API_SUBSCRIBE_UPDATE } from 'redux/api/constants'
import { ZONE_ENTITIES_INACTIVE_THRESHOLD } from 'config/constants'

// ------------------------------------
// Action Types
// ------------------------------------
export const SITES_LIST_FETCH = 'unifi.SITES_LIST_FETCH'
export const ZONE_ENTITIES_CLEAR_INACTIVE = 'unifi.ZONE_ENTITIES_CLEAR_INACTIVE'
export const ZONE_ENTITIES_SUBSCRIBE = 'unifi.ZONE_ENTITIES_SUBSCRIBE'
export const ZONE_LIST_FETCH = 'unifi.ZONE_LIST_FETCH'

// ------------------------------------
// Helper functions
// ------------------------------------
const filterOutInactiveEntities = (liveDiscovery) =>
  liveDiscovery.filter(item => {
    return moment().diff(moment(item.detectionTime)) < ZONE_ENTITIES_INACTIVE_THRESHOLD
  })

const mergeDiscoveryUpdate = (currentDiscoveries, newDiscoveries, id) => {
  return (
    unionBy(
      newDiscoveries.map(item => {
        const foundItem = find(currentDiscoveries, { clientReference: item.clientReference })
        return foundItem && item.zoneId === foundItem.zoneId ? {
          ...item,
          firstDetectionTime: foundItem.firstDetectionTime,
          correlationId: id
        } : {
          ...item,
          firstDetectionTime: item.detectionTime,
          correlationId: id
        }
      }),
      currentDiscoveries,
      'clientReference'
    )
  )
}


// ------------------------------------
// Action Creators
// ------------------------------------
export const listSites = createWsAction({
  type: SITES_LIST_FETCH,
  messageType: 'core.site.list-sites',
  selectorKey: 'sitesList',
  fields: ['clientId']
})

export const listZones = createWsAction({
  type: ZONE_LIST_FETCH,
  messageType: 'core.site.list-zones',
  selectorKey: 'zonesInfo',
  fields: ['clientId', 'siteId'],
  payloadOnSuccess: (payload) => ({
    ...payload,
    data: referenceMap(payload.data, 'zoneId')
  })
})

export const listenToSubscriptions = createWsAction({
  type: ZONE_ENTITIES_SUBSCRIBE,
  messageType: 'core.site.subscribe-detections',
  selectorKey: 'liveDiscovery',
  fields: ['clientId', 'siteId', 'includeLastKnown'],
  defaultParams: {
    includeLastKnown: true
  },
  subscribe: true,
  payloadOnSuccess: (payload, getState) => ({
    ...payload,
    data: mergeDiscoveryUpdate(liveDiscoverySelector(getState()), payload.data, payload.correlationId)
  })
})

export const clearInactiveEntities = () => (dispatch, getState) =>
  dispatch({
    type: API_SUBSCRIBE_UPDATE,
    payload: {
      selectorKey: 'liveDiscovery',
      data: filterOutInactiveEntities(liveDiscoverySelector(getState()))
    }
  })
