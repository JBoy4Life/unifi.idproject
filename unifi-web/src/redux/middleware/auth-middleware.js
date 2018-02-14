import { logoutRequest } from 'redux/user/actions'

const authMiddleware = store => next => action => {
  const { payload } = action
  if (payload && (
    payload.messageType === 'core.error.unauthorized' ||
    payload.messageType === 'core.error.authentication-failed'
  )) {
    store.dispatch(logoutRequest())
  }
  return next(action)
}

export default authMiddleware
