import fp from 'lodash/fp'

export const getReducer = fp.get('holders')

export const holdersSelector = fp.compose(
  fp.get('holdersList'),
  getReducer
)

export const holdersMetaSelector = fp.compose(
  fp.get('holdersMetaList'),
  getReducer
)
