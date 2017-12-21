export const groupItems = (items, grouping) => {
  if (grouping === 'zones') {
    return items.reduce((acc, item) => {
      const { last_seen_location } = item

      if (!acc[last_seen_location]) {
        acc[last_seen_location] = []
      }

      acc[last_seen_location].push(item)
      return acc
    }, {})
  }

  return items
}

export default {}
