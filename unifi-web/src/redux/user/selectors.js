import fp from 'lodash/fp'

export const getReducer = state => state.user

export const currentUserSelector = state => getReducer(state).currentUser

export const loginStatusSelector = state => getReducer(state).isLoggingIn

export const liveViewEnabledSelector = fp.compose(
  fp.get('verticalConfig.core.liveViewEnabled'),
  currentUserSelector
)
