import fp from 'lodash/fp'

export const sleep = time => new Promise(resolve => (
  setTimeout(resolve, time)
))

export const noop = () => Promise.resolve()

export const parseQueryString = string =>
  fp.compose(
    JSON.parse,
    JSON.stringify,
    fp.reduce((acc, part) => {
      const [name, value] = part.split('=')
      acc[name] = decodeURIComponent(value || '')
      return acc
    }, {}),
    (str) => (str ? str.split('&') : []),
    fp.replace('?', '')
  )(string)


export const getQueryParams = (string) => {
  const {
    filter, view = 'list', grouping = 'all', search = '',
  } = parseQueryString(string)
  const filters = filter ? filter.split(',') : []
  return {
    filters, view, grouping, search,
  }
}

export const jsonToQueryString = (obj) => {
  const pairs = []
  obj && Object.keys(obj).forEach((key) => {
    if (obj[key]) {
      const value = encodeURIComponent(obj[key])
      value && pairs.push(`${key}=${value}`)
    }
  })

  return pairs.length ? `?${pairs.join('&')}` : ''
}

export default null
