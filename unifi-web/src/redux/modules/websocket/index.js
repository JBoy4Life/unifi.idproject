import { handleActions } from 'redux-actions'

import createWsAction from 'utils/create-ws-action'

export const API_SUCCESS = 'unifi.API_REQUEST_SUCCESS'
export const API_FAIL = 'unifi.API_REQUEST_FAIL'
export const API_PENDING = 'unifi.API_REQUEST_PENDING'
export const API_SUBSCRIBE_UPDATE = 'unifi.API_SUBSCRIBE_UPDATE'

export const ZONE_ENTITIES_UNSUBSCRIBE = 'unifi.ZONE_ENTITIES_UNSUBSCRIBE'

export const unsubscribeToSubscriptions = createWsAction({
  type: ZONE_ENTITIES_UNSUBSCRIBE,
  messageType: 'core.protocol.unsubscribe',
  fields: ['correlationId'],
  unsubscribe: true
})

export default handleActions({
  [API_PENDING]: (state, { payload }) => ({
    ...state,
    [payload.selectorKey]: API_PENDING
  }),

  [API_SUCCESS]: (state, { payload }) => ({
    ...state,
    [payload.selectorKey]: API_SUCCESS
  }),

  [API_FAIL]: (state, { payload }) => ({
    ...state,
    [payload.selectorKey]: API_FAIL
  })
}, {});
