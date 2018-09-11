/**
 * Middleware allowing dispatching socket calls to backend api via
 * the socketRequest key.
 *
 * All actions that use socketRequest will be handled by this middleware.
 *
 * @param {Object} socketClient - implementor of the socket client
 */

import {
  API_FAIL,
  API_PENDING,
  API_SUCCESS,
  API_SUBSCRIBE_UPDATE,
  API_UNSUBSCRIBE
} from 'redux/api/constants'

const socketApiMiddleware = socketClient => store => next => (action) => {
  if (action.socketRequest) {
    const metaFields = {
      actionType: action.type,
      formSubmit: action.socketRequest.formSubmit,
      messageType: action.socketRequest.messageType,
      selectorKey: action.selectorKey
    }

    store.dispatch({
      type: API_PENDING,
      payload: metaFields
    })

    const promiseResource = new Promise((resolve, reject) => {
      socketClient.request(action.socketRequest, (res) => {
        const data = {
          ...res,
          formSubmit: action.formSubmit
        }
        res.messageType.startsWith('core.error')
        ? reject(data)
        : resolve(data)
      }).catch(ex => reject(ex))
    })

    promiseResource
      .then(res => {
        const payload = {
          data: res.payload,
          ...metaFields
        }
        store.dispatch({
          type: API_SUCCESS,
          payload: action.payloadOnSuccess ? action.payloadOnSuccess(payload, store.getState) : payload
        })
      })
      .catch(ex => {
        const payload = {
          data: ex.payload,
          ...metaFields
        }
        store.dispatch({
          type: API_FAIL,
          payload: action.payloadOnFail ? action.payloadOnFail(payload, store.getState) : payload
        })
      })
    return promiseResource
  }

  if (action.socketSubscribe) {
    socketClient.subscribe(action.socketSubscribe, (data) => {
      const payload = {
        data: data.payload,
        actionType: action.type,
        messageType: action.socketSubscribe.messageType,
        selectorKey: action.selectorKey,
        correlationId: data.correlationId
      }

      store.dispatch({
        type: API_SUBSCRIBE_UPDATE,
        payload: action.payloadOnSuccess ? action.payloadOnSuccess(payload, store.getState) : payload
      })
    })
  }

  if (action.socketUnsubscribe) {
    socketClient.unsubscribe(action.socketUnsubscribe)
  }

  return next(action)
}

export default socketApiMiddleware
