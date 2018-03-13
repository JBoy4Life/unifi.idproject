import createAction from 'utils/create-ws-action'
import { WSPackage } from '../../lib/ws'

import {
  ZONE_LIST_FETCH,
  ZONE_ENTITIES_SUBSCRIBE,
  ZONE_ENTITIES_CLEAR_INACTIVE
} from './types'

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
