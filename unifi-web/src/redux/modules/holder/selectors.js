import fp from 'lodash/fp'

export const getReducer = fp.get('holder')

export const holdersSelector = fp.compose(
  fp.get('holdersList'),
  getReducer
)

export const holderDetailsSelector = fp.compose(
  fp.get('holderDetails'),
  getReducer
)

export const programmesSelector = fp.compose(
  fp.get('programmesList'),
  getReducer
)
