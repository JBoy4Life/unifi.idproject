import { REHYDRATE } from 'redux-persist'
import { USER_LOGOUT, USER_LOGIN, USER_REAUTHENTICATE, USER_SET_INITIALIZED } from './types'

const initialState = {
  isLoggingIn: false,
  currentUser: null,
  initialising: true,
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
        ...(action.payload ? action.payload.user : null),
        initialising: state.initialising
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

    default:
      return state
  }
}

export default reducer
