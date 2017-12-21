export const sleep = time => new Promise(resolve => (
  setTimeout(resolve, time)
))

export const noop = () => Promise.resolve()

export const parseQueryString = string => string.replace('?', '').split('&').reduce((acc, part) => {
  const [name, value] = part.split('=')
  acc[name] = value
  return acc
}, {})

export const getQueryParams = (string) => {
  const {
    filter, view = 'list', grouping = 'all', search = '',
  } = parseQueryString(string)
  const filters = filter ? filter.split(',') : []
  return {
    filters, view, grouping, search,
  }
}

export default null
