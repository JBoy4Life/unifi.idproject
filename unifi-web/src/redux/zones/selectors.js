import fp from 'lodash/fp'

export const getReducer = state => state.liveZones

export const zonesInfoSelector = fp.compose(
  fp.get('zonesInfo'),
  getReducer
)

export const liveDiscoverySelector = fp.compose(
  fp.get('liveDiscovery'),
  getReducer
)
