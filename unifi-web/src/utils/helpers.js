export const sleep = time => new Promise(resolve => (
  setTimeout(resolve, time)
))

export const noop = () => Promise.resolve()

export default null
