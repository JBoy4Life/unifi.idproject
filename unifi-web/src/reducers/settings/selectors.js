import fp from 'lodash/fp'

export const getReducer = state => state.settings

export const siteSelector = fp.compose(
  fp.get('sitesList[0]'),
  getReducer
)

export const siteIdSelector = fp.compose(
  fp.get('siteId'),
  siteSelector
)
