import unionBy from 'lodash/unionBy'

import {
  HOLDER_GET_FETCH,
  HOLDERS_LIST_FETCH,
  PROGRAMMES_LIST_FETCH,
  SITES_LIST_FETCH
} from './types'

const initialState = {
  holdersList: [],
  programmesList: [],
  sitesList: [],
  holdersMetaList: []
}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${SITES_LIST_FETCH}_FULFILLED`:
      return {
        ...state,
        sitesList: action.payload.payload
      }

    case `${PROGRAMMES_LIST_FETCH}_FULFILLED`:
      return {
        ...state,
        programmesList: action.payload.payload.filter(val => Boolean(val))
      }

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
