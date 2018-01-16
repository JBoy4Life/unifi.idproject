// import fp from 'lodash/fp'
import { SITES_LIST_FETCH } from './types'

const initialState = {
  sitesList: [],
}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${SITES_LIST_FETCH}_FULFILLED`:
      return {
        ...state,
        sitesList: action.payload.payload
      }
    default:
      return state
  }
}

export default reducer
