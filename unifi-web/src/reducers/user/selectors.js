export const getReducer = state => state.user

export const getCurrentUser = state => getReducer(state).currentUser
