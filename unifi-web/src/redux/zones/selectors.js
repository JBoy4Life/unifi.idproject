import fp from 'lodash/fp'

export const getReducer = state => state.liveZones

export const zonesInfoSelector = fp.compose(
  fp.get('zonesInfo'),
  getReducer
)

export const getDiscoveredList = (state) => {
  const liveZones = getReducer(state)

  const { holdersInfo, liveDiscovery, zonesInfo } = liveZones
  return liveDiscovery.map((discovery, idx) => ({
    ...discovery,
    client: holdersInfo[discovery.clientReference],
    zone: zonesInfo[discovery.zoneId],
    key: idx,
  }))
}
