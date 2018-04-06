import createWsAction from 'utils/create-ws-action'

// ------------------------------------
// Action Types
// ------------------------------------
export const CLIENT_CREATE = 'unifi.CLIENT_CREATE'
export const CLIENT_GET_FETCH = 'unifi.CLIENT_GET_FETCH'
export const CLIENT_LIST_FETCH = 'unifi.CLIENT_LIST_FETCH'

// ------------------------------------
// Action Creators
// ------------------------------------
export const createClient = createWsAction({
  type: CLIENT_CREATE,
  messageType: 'core.client.register-client',
  selectorKey: 'clientDetail',
  fields: ['clientId', 'displayName', 'logo']
})

export const listClients = createWsAction({
  type: CLIENT_LIST_FETCH,
  messageType: 'core.client.list-clients',
  selectorKey: 'clientList',
  fields: []
})

export const getClient = createWsAction({
  type: CLIENT_GET_FETCH,
  messageType: 'core.client.get-client',
  selectorKey: 'clientDetail',
  fields: ['clientId', 'with'],
  defaultParams: {
    with: ['image']
  }
})

export default null
