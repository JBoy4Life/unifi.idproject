// import fp from 'lodash/fp'
import { PROGRAMMES_LIST_FETCH, SITES_LIST_FETCH } from './types'

const initialState = {
  programmesList: [],
  sitesList: []
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

    default:
      return state
  }
}

export default reducer
