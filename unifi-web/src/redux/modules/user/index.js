import { createAction, handleActions } from 'redux-actions'
import { REHYDRATE } from 'redux-persist'

import createWsAction from 'utils/create-ws-action'
import { API_FAIL, API_PENDING, API_SUCCESS } from 'redux/api/constants'

// ------------------------------------
// Action Types
// ------------------------------------
export const USER_LOGOUT = 'unifi.USER_LOGOUT'
export const USER_LOGIN = 'unifi.USER_LOGIN'
export const USER_REAUTHENTICATE = 'unifi.USER_REAUTHENTICATE'
export const USER_SET_INITIALIZED = 'unifi.USER_SET_INITIALIZED'
export const PASSWORD_RESET_INFO_FETCH = 'unifi.PASSWORD_RESET_INFO_FETCH'
export const SET_PASSWORD = 'unifi.SET_PASSWORD'
export const REQUEST_PASSWORD_RESET = 'unifi.REQUEST_PASSWORD_RESET'
export const CANCEL_PASSWORD_RESET = 'unifi.CANCEL_PASSWORD_RESET'
export const CHANGE_PASSWORD = 'unifi.CHANGE_PASSWORD'

// ------------------------------------
// Action Creators
// ------------------------------------
export const loginRequest = createWsAction({
  type: USER_LOGIN,
  messageType: 'core.operator.auth-password',
  selectorKey: 'currentUser',
  fields: ['username', 'password', 'clientId']
})

export const reauthenticateRequest = createWsAction({
  type: USER_REAUTHENTICATE,
  messageType: 'core.operator.auth-token',
  selectorKey: 'currentUser',
  fields: ['clientId', 'sessionToken']
})

export const logoutRequest = createAction(USER_LOGOUT)

export const setInitialized = createAction(USER_SET_INITIALIZED)

export const getPasswordResetInfo = createWsAction({
  type: PASSWORD_RESET_INFO_FETCH,
  messageType: 'core.operator.get-password-reset',
  selectorKey: 'passwordResetInfo',
  fields: ['clientId', 'username', 'token']
})

export const setPassword = createWsAction({
  type: SET_PASSWORD,
  messageType: 'core.operator.set-password',
  selectorKey: 'setPassword',
  fields: ['clientId', 'username', 'password', 'token']
})

export const requestPasswordReset = createWsAction({
  type: REQUEST_PASSWORD_RESET,
  messageType: 'core.operator.request-password-reset',
  selectorKey: 'requestPasswordReset',
  fields: ['clientId', 'username']
})

export const cancelPasswordReset = createWsAction({
  type: CANCEL_PASSWORD_RESET,
  messageType: 'core.operator.cancel-password-reset',
  selectorKey: 'cancelPasswordReset',
  fields: ['clientId', 'username', 'token']
})

export const changePassword = createWsAction({
  type: CHANGE_PASSWORD,
  messageType: 'core.operator.change-password',
  selectorKey: 'changePassword',
  fields: ['currentPassword', 'password']
})

const initialState = {
  currentUser: null,
  initialising: true,
  isLoggingIn: false,
}

// ------------------------------------
// Helper Functions
// ------------------------------------
const handleRequestPending = (state, { payload }) => {
  switch (payload.actionType) {
    case USER_LOGIN:
      return {
        ...state,
        currentUser: null,
        isLoggingIn: true
      }
    default:
      return state
  }
}

const handleRequestSuccess = (state, { payload }) => {
  switch (payload.actionType) {
    case USER_LOGIN:
      return {
        ...state,
        currentUser: payload.data,
        isLoggingIn: false,
      }
    case USER_REAUTHENTICATE:
      return {
        ...state,
        currentUser: payload.data,
        initialising: false,
      }
    default:
      return state
  }
}

const handleRequestRejected = (state, { payload }) => {
  switch (payload.actionType) {
    case USER_LOGIN:
      return {
        ...state,
        currentUser: null,
        isLoggingIn: false,
        error: payload.message,
      }
    case USER_REAUTHENTICATE:
      return {
        ...state,
        currentUser: null,
        initialising: false,
      }
    default:
      return state
  }
}
// ------------------------------------
// Reducer
// ------------------------------------
const reducer = handleActions({
  [API_SUCCESS]: handleRequestSuccess,
  [API_PENDING]: handleRequestPending,
  [API_FAIL]: handleRequestRejected,

  [USER_LOGOUT]: (state, { payload }) => ({
    ...state,
    currentUser: null,
  }),

  [REHYDRATE]: (state, { payload }) => ({
    ...state,
    currentUser: payload ? payload.user.currentUser : state.currentUser
  }),

  [USER_SET_INITIALIZED]: (state, { payload }) => ({
    ...state,
    initialising: false
  })
}, initialState)

export default reducer
