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

export const verticalConfigSelector = fp.compose(
  fp.get('verticalConfig'),
  currentUserSelector
)

export const attendanceEnabledSelector = fp.compose(
  Boolean,
  fp.get('attendance'),
  verticalConfigSelector
)

export const liveViewEnabledSelector = fp.compose(
  fp.get('core.liveViewEnabled'),
  verticalConfigSelector
)

export const passwordResetInfoSelector = fp.compose(
  fp.get('passwordResetInfo'),
  getReducer
)

export const setPasswordStatusSelector = fp.compose(
  fp.get('setPasswordStatus'),
  getReducer
)
