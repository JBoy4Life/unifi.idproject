import fp from 'lodash/fp'

import { API_PENDING, API_SUCCESS } from '../api/request'

export const getReducer = fp.get('clients')

export const clientsSelector = fp.compose(
  fp.get('clients'),
  getReducer
)

export const currentClientSelector = fp.compose(
  fp.get('currentClient'),
  getReducer
)

export const clientIsLoadingSelector = fp.compose(
  (status) => status === 'INIT' || status === API_PENDING,
  fp.get('clientGetStatus'),
  getReducer
)

export const clientIsLoadedSelector = fp.compose(
  fp.isEqual(API_SUCCESS),
  fp.get('clientGetStatus'),
  getReducer
)
