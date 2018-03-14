import fp from 'lodash/fp'

export const getReducer = fp.get('holder')

export const holdersSelector = fp.compose(
  fp.get('holdersList'),
  getReducer
)

export const holdersMetaSelector = fp.compose(
  fp.get('holdersMetaList'),
  getReducer
)

export const programmesSelector = fp.compose(
  fp.get('programmesList'),
  getReducer
)
