import { WSPackage } from '../../lib/ws'

import { DETECTABLES_LIST_FETCH } from './types'


export const listDetectables = (clientId, filter) => {
  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.detectable.list-detectables',
    payload: { clientId, with: ['assignment'], filter }
  })

  return {
    type: DETECTABLES_LIST_FETCH,
    socketRequest: pack.content
  }
}


export default null
