import { WSPackage } from '../../lib/ws'

import {
  HOLDER_GET_FETCH,
  HOLDERS_LIST_FETCH,
  PROGRAMMES_LIST_FETCH,
  SITES_LIST_FETCH
} from './types'

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


export function listProgrammes(clientId) {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion:  '1.0.0',
    messageType:     'core.holder.list-metadata-values',
    payload:         { clientId, metadataKey: 'programme' }
  });

  return {
    type: PROGRAMMES_LIST_FETCH,
    socketRequest: pack.content
  };
}


export const listHolders = (clientId) => {
  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.holder.list-holders',
    payload: { clientId }
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
