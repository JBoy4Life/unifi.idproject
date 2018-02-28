import { WSPackage } from '../../lib/ws'

import {
  HOLDER_GET_FETCH,
  HOLDERS_LIST_FETCH
} from './types'


export const listHolders = (clientId, associateWith) => {
  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.holder.list-holders',
    payload: { clientId, with: associateWith }
  })

  return {
    type: HOLDERS_LIST_FETCH,
    socketRequest: pack.content
  }
}


export const getHolder = (clientId, clientReference) => {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.holder.get-holder',
    payload: {
      clientId,
      clientReference,
      with: ['metadata'],
    }
  });

  return {
    type: HOLDER_GET_FETCH,
    socketRequest: pack.content
  };
}

export default null
