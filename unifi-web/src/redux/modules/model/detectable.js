import createWsAction from 'utils/create-ws-action'

// ------------------------------------
// Action Types
// ------------------------------------
export const DETECTABLES_LIST_FETCH = 'unifi.DETECTABLES_LIST_FETCH'

// ------------------------------------
// Action Creators
// ------------------------------------
export const listDetectables = createWsAction({
  type: DETECTABLES_LIST_FETCH,
  messageType: 'core.detectable.list-detectables',
  selectorKey: 'detectablesList',
  fields: ['clientId', 'filter', 'with'],
  defaultParams: {
    with: ['assignment']
  }
})

export default null
