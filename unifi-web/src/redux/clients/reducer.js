import {
  CLIENT_CREATE,
  CLIENT_LIST_FETCH,
} from './types'

const initialState = {
  clients: [],
}

const reducer = (state = initialState, action = {}) => {
  // console.log(action.type, action)
  switch (action.type) {
    case `${CLIENT_CREATE}_FULFILLED`:
      return {
        ...state,
        clientRequestPending: false,
      }

    case `${CLIENT_LIST_FETCH}_FULFILLED`:
      return {
        ...state,
        clients: action.payload.payload,
      }
    default:
      return state
  }
}

export default reducer
