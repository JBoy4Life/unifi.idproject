import map from 'lodash/map'

export const sleep = time => new Promise(resolve => (
  setTimeout(resolve, time)
))

export const btoa = (str) => {
  let buffer;
  if (str instanceof Buffer) {
    buffer = str
  } else {
    buffer = Buffer.from(str.toString(), 'binary')
  }
  return buffer.toString('base64')
}

export const base64EncodeUint8Array = (unit8Array) =>
  btoa(map(unit8Array, char => String.fromCharCode(char)).join(''))
