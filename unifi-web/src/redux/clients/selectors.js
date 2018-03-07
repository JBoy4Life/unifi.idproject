import fp from 'lodash/fp'

export const getReducer = fp.get('clients')

export const clientsSelector = fp.compose(
  fp.get('clients'),
  getReducer
)

export const currentClientSelector = fp.compose(
  fp.get('currentClient'),
  getReducer
)
