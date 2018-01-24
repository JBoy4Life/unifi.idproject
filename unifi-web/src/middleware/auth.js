import { logoutRequest } from 'reducers/user/actions'

const authMiddleware = store => next => action => {
  if (action.payload && action.payload.messageType === 'core.error.unauthorized') {
    store.dispatch(logoutRequest())
  }
  return next(action)
}

export default authMiddleware
