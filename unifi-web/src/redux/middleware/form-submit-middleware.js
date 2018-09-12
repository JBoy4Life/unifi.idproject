/**
 * Middleware allowing dispatching socket calls to backend api via
 * the socketRequest key.
 *
 * All actions that use socketRequest will be handled by this middleware.
 *
 * @param {Object} socketClient - implementor of the socket client
 */

import pick from 'lodash/pick'

const formSubmitMiddleware = store => next => (action) => {
  if (action.payload && action.payload.formSubmit) {
    const { onSuccess, onFail } = action.payload.formSubmit
    if (onSuccess && action.type.endsWith('_SUCCESS')) {
      onSuccess(action.payload)
    } else if (onFail && action.type.endsWith('_FAIL')) {
      onFail(action.payload)
    }
  }

  return next(action)
}

export default formSubmitMiddleware
