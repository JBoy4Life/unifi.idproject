import { logoutRequest } from 'redux/modules/user'

const authMiddleware = store => next => action => {
  const { payload } = action
  if (payload && (
    payload.messageType === 'core.error.unauthorized'
  )) {
    store.dispatch(logoutRequest())
  }
  return next(action)
}

export default authMiddleware
