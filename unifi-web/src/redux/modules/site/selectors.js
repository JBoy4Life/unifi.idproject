import fp from 'lodash/fp'

export const getReducer = fp.get('site')

export const siteSelector = fp.compose(
  fp.get('sitesList[0]'),
  getReducer
)

export const siteIdSelector = fp.compose(
  fp.get('siteId'),
  siteSelector
)

export const zonesInfoSelector = fp.compose(
  fp.get('zonesInfo'),
  getReducer
)

export const liveDiscoverySelector = fp.compose(
  fp.get('liveDiscovery'),
  getReducer
)
