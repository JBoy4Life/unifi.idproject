/**
 * Middleware allowing dispatching socket calls to backend api via
 * the socketRequest key.
 *
 * All actions that use socketRequest will be handled by this middleware.
 *
 * @param {Object} socketClient - implementor of the socket client
 */
const socketApiMiddleware = socketClient => store => next => (action) => {
  if (action.socketRequest) {
    const promiseResource = socketClient.request(action.socketRequest, { json: true })

    store.dispatch({
      type: action.type,
      payload: promiseResource,
    });

    return promiseResource
  }

  if (action.socketSubscribe) {
    socketClient.subscribe(action.socketSubscribe, { json: true }, (data) => {
      store.dispatch({
        type: `${action.type}_UPDATE`,
        data,
      })
    })
  }

  return next(action)
};

export default socketApiMiddleware
