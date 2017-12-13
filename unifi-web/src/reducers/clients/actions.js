import { WSPackage } from '../../lib/ws'

import { CLIENT_CREATE } from './types'

export const createClient = ({ clientId, displayName, logo }) => {
  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.client.register-client',
    payload: {
      clientId,
      displayName,
      logo,
    },
  })


  return {
    payload: window.wsProtocol.request(pack.content, { json: true }),
    type: CLIENT_CREATE,
  }
}

export default null
