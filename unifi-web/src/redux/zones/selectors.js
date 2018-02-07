export const getReducer = state => state.liveZones

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
