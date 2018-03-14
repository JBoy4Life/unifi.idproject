import fp from 'lodash/fp'

export const getReducer = fp.get('operator')

export const operatorListSelector = fp.compose(
  fp.get('operatorList'),
  getReducer
)

export const operatorListStatusSelector = fp.compose(
  fp.get('operatorListStatus'),
  getReducer
)

export const operatorDetailsSelector = fp.compose(
  fp.get('operatorDetails'),
  getReducer
)

export const operatorDetailsStatusSelector = fp.compose(
  fp.get('operatorDetailsStatus'),
  getReducer
)
