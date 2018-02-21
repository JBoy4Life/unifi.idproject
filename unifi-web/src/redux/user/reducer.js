import { REHYDRATE } from 'redux-persist'

import {
  USER_LOGOUT,
  USER_LOGIN,
  USER_REAUTHENTICATE,
  USER_SET_INITIALIZED,
  SET_PASSWORD,
  PASSWORD_RESET_INFO_FETCH
} from './types'
import { API_SUCCESS, API_FAIL } from 'redux/api/request'

const initialState = {
  isLoggingIn: false,
  currentUser: null,
  initialising: true,
  passwordResetInfo: {
    status: 'INIT',
    payload: null
  },
  setPasswordStatus: 'INIT'
}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${USER_LOGIN}_PENDING`:
      return {
        ...state,
        currentUser: null,
        isLoggingIn: true,
        error: null,
      }

    case `${USER_LOGIN}_FULFILLED`:
      return {
        ...state,
        currentUser: action.payload.payload,
        isLoggingIn: false,
      }

    case `${USER_LOGIN}_REJECTED`:
      return {
        ...state,
        currentUser: null,
        isLoggingIn: false,
        error: action.payload.payload.message,
      }

    case USER_LOGOUT:
      return {
        ...state,
        currentUser: null,
      }

    case REHYDRATE:
      return {
        ...state,
        currentUser: action.payload ? action.payload.user.currentUser : state.currentUser
      }

    case USER_SET_INITIALIZED:
      return {
        ...state,
        initialising: false
      }

    case `${USER_REAUTHENTICATE}_FULFILLED`:
      return {
        ...state,
        ...action.payload.user,
        initialising: false,
      }

    case `${USER_REAUTHENTICATE}_REJECTED`:
      return {
        ...state,
        initialising: false,
      }

    case `${PASSWORD_RESET_INFO_FETCH}_FULFILLED`:
      return {
        ...state,
        passwordResetInfo: {
          ...action.payload.payload,
          status: action.payload.payload ? API_SUCCESS : API_FAIL
        }
      }

    case `${PASSWORD_RESET_INFO_FETCH}_REJECTED`:
      return {
        ...state,
        passwordResetInfo: {
          ...action.payload.payload,
          status: API_FAIL
        }
      }

    case `${SET_PASSWORD}_FULFILLED`:
      return {
        ...state,
        currentUser: action.payload.payload,
        setPasswordStatus: API_SUCCESS
      }

    case `${SET_PASSWORD}_REJECTED`:
      return {
        ...state,
        setPasswordStatus: API_FAIL
      }

    default:
      return state
  }
}

export default reducer
