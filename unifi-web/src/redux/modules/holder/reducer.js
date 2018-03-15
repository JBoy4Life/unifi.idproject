import {
  HOLDER_GET_FETCH,
  HOLDERS_LIST_FETCH,
  PROGRAMMES_LIST_FETCH
} from './types'

const initialState = {
  holdersList: [],
  holderDetails: null,
  programmesList: []
}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${HOLDERS_LIST_FETCH}_FULFILLED`:
      return {
        ...state,
        holdersList: action.payload.payload
      }
    case `${HOLDER_GET_FETCH}_FULFILLED`:
      return {
        ...state,
        holderDetails: action.payload.payload
      }
    case `${PROGRAMMES_LIST_FETCH}_FULFILLED`:
      return {
        ...state,
        programmesList: action.payload.payload.filter(val => Boolean(val))
      }

    default:
      return state
  }
}

export default reducer
