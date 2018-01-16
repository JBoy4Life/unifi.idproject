import { WSPackage } from '../../lib/ws'

import { SITES_LIST_FETCH } from './types'

export const listSites = (clientId) => {
  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.site.list-sites',
    payload: { clientId }
  })


  return {
    socketRequest: pack.content,
    type: SITES_LIST_FETCH,
  }
}

export default null
