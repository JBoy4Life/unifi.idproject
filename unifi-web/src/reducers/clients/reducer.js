import {
  CLIENT_CREATE,
  CLIENT_LIST_UPDATE,
} from './types'

const initialState = {
  clients: [],
}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${CLIENT_CREATE}_FULFILLED`:
      return {
        ...state,
        clientRequestPending: false,
      }

    case CLIENT_LIST_UPDATE:
      return {
        ...state,
        clients: action.clients,
      }
    default:
      return state
  }
}

export default reducer
