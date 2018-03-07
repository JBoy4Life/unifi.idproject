import { WSPackage } from '../../lib/ws'

import { CLIENT_CREATE, CLIENT_GET_FETCH, CLIENT_LIST_FETCH } from './types'

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
    socketRequest: pack.content,
    type: CLIENT_CREATE,
  }
}


export const listClients = () => {
  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.client.list-clients',
    payload: null,
  })


  return {
    socketRequest: pack.content,
    type: CLIENT_LIST_FETCH,
  }
}


export const getClient = (clientId) => {
  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.client.get-client',
    payload: { clientId, with: ['image'] }
  })

  return {
    socketRequest: pack.content,
    type: CLIENT_GET_FETCH,
  }
}

export default null
