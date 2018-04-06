import fp from 'lodash/fp'

export const modelSelector = (selectorKey, defaultVal = null) => fp.compose(
  fp.defaultTo(defaultVal),
  fp.get(selectorKey),
  fp.get('model')
)

export const websocketStatusSelector = (selectorKey) => fp.compose(
  fp.defaultTo('INIT'),
  fp.get(selectorKey),
  fp.get('websocket')
)
