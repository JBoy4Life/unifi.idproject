import { liveDiscoverySelector, zonesInfoSelector } from 'redux/selectors'
import { holdersSelector } from 'redux/selectors'
import { referenceMap } from 'utils/helpers'

export const groupItems = (items, grouping) => {
  if (grouping === 'zones') {
    return items.reduce((acc, item) => {
      const { zoneId } = item

      if (!acc[zoneId]) {
        acc[zoneId] = []
      }

      acc[zoneId].push(item)
      return acc
    }, {})
  }

  return items
}

export const filterItems = (items, filters, search) =>
  items.filter((item) => {
    const matchesFilters = filters
      .reduce((acc, filter) => acc || item.client && item.client.holderType === filter, filters.length === 0)
    const matchesSearch = item.client && item.client.name ? item.client.name.indexOf(search) !== -1 : true
    return matchesFilters && matchesSearch
  })

export const getDiscoveredList = (state) => {
  const liveDiscovery = liveDiscoverySelector(state)
  const zonesInfo = zonesInfoSelector(state)
  const holdersInfo = referenceMap(holdersSelector(state), 'clientReference')
  return liveDiscovery.map((discovery, idx) => ({
    ...discovery,
    client: holdersInfo[discovery.clientReference],
    zone: zonesInfo[discovery.zoneId],
    key: idx,
  }))
}

export default {}
