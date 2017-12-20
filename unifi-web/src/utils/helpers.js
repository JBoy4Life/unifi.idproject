export const sleep = time => new Promise(resolve => (
  setTimeout(resolve, time)
))

export const noop = () => Promise.resolve()

export const parseQueryString = string => string.replace('?', '').split('&').reduce((acc, part) => {
  const [name, value] = part.split('=')
  acc[name] = value
  return acc
}, {})

export default null
