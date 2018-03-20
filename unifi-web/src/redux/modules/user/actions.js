import createAction from 'utils/create-ws-action'

import {
  USER_LOGOUT,
  USER_LOGIN,
  USER_REAUTHENTICATE,
  USER_SET_INITIALIZED,
  PASSWORD_RESET_INFO_FETCH,
  CHANGE_PASSWORD,
  SET_PASSWORD,
  REQUEST_PASSWORD_RESET,
  CANCEL_PASSWORD_RESET
} from './types'

export const loginRequest = createAction({
  type: USER_LOGIN,
  messageType: 'core.operator.auth-password',
  fields: ['username', 'password', 'clientId']
})

export const reauthenticateRequest = createAction({
  type: USER_REAUTHENTICATE,
  messageType: 'core.operator.auth-token',
  fields: ['clientId', 'sessionToken']
})

export const logoutRequest = () => ({
  type: USER_LOGOUT
})

export const setInitialized = () => ({
  type: USER_SET_INITIALIZED
})

export const getPasswordResetInfo = createAction({
  type: PASSWORD_RESET_INFO_FETCH,
  messageType: 'core.operator.get-password-reset',
  fields: ['clientId', 'username', 'token']
})

export const setPassword = createAction({
  type: SET_PASSWORD,
  messageType: 'core.operator.set-password',
  fields: ['clientId', 'username', 'password', 'token']
})

export const requestPasswordReset = createAction({
  type: REQUEST_PASSWORD_RESET,
  messageType: 'core.operator.request-password-reset',
  fields: ['clientId', 'username']
})

export const cancelPasswordReset = createAction({
  type: CANCEL_PASSWORD_RESET,
  messageType: 'core.operator.cancel-password-reset',
  fields: ['clientId', 'username', 'token']
})

export const changePassword = createAction({
  type: CHANGE_PASSWORD,
  messageType: 'core.operator.change-password',
  fields: ['currentPassword', 'password']
})

export default null
