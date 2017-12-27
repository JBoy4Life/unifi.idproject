import { REHYDRATE } from 'redux-persist'
import { USER_LOGOUT, USER_LOGIN } from './types'

const initialState = {
  currentUser: null,
  initialising: true,
}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${USER_LOGIN}_FULFILLED`:
      console.log(action)
      return {
        ...state,
        currentUser: action.currentUser,
      }

    case USER_LOGOUT:
      return {
        ...state,
        currentUser: null,
      }

    case REHYDRATE:
      if (action.payload && action.payload.user) {
        return {
          ...state,
          ...action.payload.user,
          initialising: false,
        }
      }
      return {
        ...state,
        initialising: false,
      }

    default:
      return state
  }
}

export default reducer
