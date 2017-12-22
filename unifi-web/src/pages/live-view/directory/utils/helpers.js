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
      .reduce((acc, filter) => acc || item.client.holderType === filter, filters.length === 0)
    const matchesSearch = item.client.name ? item.client.name.indexOf(search) !== -1 : true
    return matchesFilters && matchesSearch
  })

export default {}
