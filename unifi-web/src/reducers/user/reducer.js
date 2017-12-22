import { REHYDRATE } from 'redux-persist'

const initialState = {
  currentUser: null,
}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case 'TEST_USER':
      return {
        ...state,
        currentUser: action.currentUser,
      }

    case REHYDRATE:
      console.log(REHYDRATE, action)
      if (action.payload && action.payload.user) {
        return {
          ...state,
          ...action.payload.user,
        }
      }
      return state

    default:
      return state
  }
}

export default reducer
