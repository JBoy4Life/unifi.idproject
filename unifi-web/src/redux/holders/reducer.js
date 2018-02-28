import unionBy from 'lodash/unionBy'

import {
  HOLDER_GET_FETCH,
  HOLDERS_LIST_FETCH,
  PROGRAMMES_LIST_FETCH,
  SITES_LIST_FETCH
} from './types'

const initialState = {
  holdersList: [],
  holdersMetaList: []
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
        holdersMetaList: unionBy(state.holdersMetaList, [action.payload.payload], 'clientReference')
      }
    default:
      return state
  }
}

export default reducer
