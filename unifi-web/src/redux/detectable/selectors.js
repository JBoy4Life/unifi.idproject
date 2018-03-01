import fp from 'lodash/fp'

export const getReducer = fp.get('detectable')

export const detectablesListSelector = fp.compose(
  fp.get('detectablesList'),
  getReducer
)
