import { WSPackage } from '../../lib/ws'

import { ZONE_LIST_FETCH, ZONE_LIST_HOLDERS_FETCH } from './types'

export const listZones = (clientId = 'deloitte', siteId = '1nss') => {
  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.site.list-zones',
    payload: { clientId, siteId },
  })


  return {
    socketRequest: pack.content,
    type: ZONE_LIST_FETCH,
  }
}

export const listHolder = (clientId = 'deloitte', siteId = '1nss') => {
  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.site.list-holders',
    payload: { clientId, siteId },
  })


  return {
    socketRequest: pack.content,
    type: ZONE_LIST_HOLDERS_FETCH,
  }
}

export const listenToSubscriptions = (clientId = 'deloitte', siteId = '1nss') => {
  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.site.subscribe-detections',
    payload: { clientId, siteId },
  })

  return {
    socketSubscribe: pack.content,
    type: ZONE_LIST_HOLDERS_FETCH,
  }
}
