import fp from 'lodash/fp'

export const getReducer = fp.get('user')

export const currentUserSelector = fp.compose(
  fp.get('currentUser'),
  getReducer
)

export const loginStatusSelector = fp.compose(
  fp.get('isLoggingIn'),
  getReducer
)

export const liveViewEnabledSelector = fp.compose(
  fp.get('verticalConfig.core.liveViewEnabled'),
  currentUserSelector
)

export const passwordResetInfoSelector = fp.compose(
  fp.get('passwordResetInfo'),
  getReducer
)

export const setPasswordStatusSelector = fp.compose(
  fp.get('setPasswordStatus'),
  getReducer
)
