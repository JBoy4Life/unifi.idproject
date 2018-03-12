import fp from 'lodash/fp'

export const getReducer = fp.get('operator')

export const operatorListSelector = fp.compose(
  fp.get('operatorList'),
  getReducer
)

export const operatorDetailsSelector = fp.compose(
  fp.get('operatorDetails'),
  getReducer
)
