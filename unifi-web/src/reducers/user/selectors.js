export const getReducer = state => state.user

export const currentUserSelector = state => getReducer(state).currentUser

export const loginStatusSelector = state => getReducer(state).isLoggingIn
