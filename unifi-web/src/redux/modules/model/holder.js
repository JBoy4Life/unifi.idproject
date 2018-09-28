import createWsAction from 'utils/create-ws-action'

// ------------------------------------
// Action Types
// ------------------------------------
export const HOLDERS_LIST_FETCH = 'unifi.HOLDERS_LIST_FETCH'
export const HOLDER_GET_FETCH = 'unifi.HOLDER_GET_FETCH'
export const HOLDER_GET_CACHE = 'unifi.HOLDER_GET_CACHE'
export const HOLDER_ADD = 'unifi.HOLDER_ADD'
export const HOLDER_UPDATE = 'unifi.HOLDER_UPDATE'
export const PROGRAMMES_LIST_FETCH = 'unifi.PROGRAMMES_LIST_FETCH'

// ------------------------------------
// Action Creators
// ------------------------------------
export const listHolders = createWsAction({
  type: HOLDERS_LIST_FETCH,
  messageType: 'core.holder.list-holders',
  selectorKey: 'holdersList',
  fields: ['clientId', 'with']
})

export const getHolder = createWsAction({
  type: HOLDER_GET_FETCH,
  messageType: 'core.holder.get-holder',
  selectorKey: 'holderDetails',
  fields: ['clientId', 'clientReference', 'with'],
  defaultParams: {
    with: ['metadata']
  }
})

export const cacheHolder = createWsAction({
  type: HOLDER_GET_CACHE,
  messageType: 'core.holder.get-holder',
  selectorKey: 'holdersCache',
  subKey: 'clientReference',
  fields: ['clientId', 'clientReference', 'with'],
  defaultParams: {
    with: ['metadata']
  }
})

export const addHolder = createWsAction({
  type: HOLDER_ADD,
  messageType: 'core.holder.add-holder',
  selectorKey: 'holderDetails',
  fields: ['clientId', 'clientReference', 'holderType', 'name', 'active', 'image'],
  defaultParams: {
    holderType: 'contact'
  }
})

export const updateHolder = createWsAction({
  type: HOLDER_UPDATE,
  messageType: 'core.holder.edit-holder',
  selectorKey: 'holderDetails',
  fields: ['clientId', 'clientReference', 'changes']
})

export const listProgrammes = createWsAction({
  type: PROGRAMMES_LIST_FETCH,
  messageType: 'core.holder.list-metadata-values',
  selectorKey: 'programmesList',
  fields: ['clientId', 'metadataKey'],
  defaultParams: {
    metadataKey: 'programme'
  }
})

export default null
