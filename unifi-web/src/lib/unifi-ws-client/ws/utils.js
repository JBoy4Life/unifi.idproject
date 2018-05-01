import map from 'lodash/map'

export const sleep = time => new Promise(resolve => (
  setTimeout(resolve, time)
))

export const base64EncodeUint8Array = (unit8Array) =>
  btoa(map(unit8Array, char => String.fromCharCode(char)).join(''))
