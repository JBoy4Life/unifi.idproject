import createAction from 'utils/create-ws-action'

import { CLIENT_CREATE, CLIENT_GET_FETCH, CLIENT_LIST_FETCH } from './types'

export const createClient = createAction({
  type: CLIENT_CREATE,
  messageType: 'core.client.register-client',
  fields: ['clientId', 'displayName', 'logo']
})

export const listClients = createAction({
  type: CLIENT_LIST_FETCH,
  messageType: 'core.client.list-clients',
  fields: []
})

export const getClient = createAction({
  type: CLIENT_GET_FETCH,
  messageType: 'core.client.get-client',
  fields: ['clientId', 'with'],
  defaultParams: {
    with: ['image']
  }
})

export default null
