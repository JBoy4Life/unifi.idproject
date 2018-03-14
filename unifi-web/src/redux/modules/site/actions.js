import createAction from 'utils/create-ws-action'

import {
  SITES_LIST_FETCH,
  ZONE_ENTITIES_CLEAR_INACTIVE,
  ZONE_ENTITIES_SUBSCRIBE,
  ZONE_LIST_FETCH
} from './types'

export const listSites = createAction({
  type: SITES_LIST_FETCH,
  messageType: 'core.site.list-sites',
  fields: ['clientId']
})

export const listZones = createAction({
  type: ZONE_LIST_FETCH,
  messageType: 'core.site.list-zones',
  fields: ['clientId', 'siteId']
})

export const listenToSubscriptions = createAction({
  type: ZONE_ENTITIES_SUBSCRIBE,
  messageType: 'core.site.subscribe-detections',
  fields: ['clientId', 'siteId'],
  subscribe: true
})

export const clearInactiveEntities = () => ({
  type: ZONE_ENTITIES_CLEAR_INACTIVE
})
