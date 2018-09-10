import pick from 'lodash/pick'

// Function to generate action creator to handle websocket action
export default ({
  type, // Action type
  messageType, // WebSocket message type
  fields, // accepted action parameter field names that will be picked from payload
  defaultParams, // default paramater values in case fields are missing in payload
  selectorKey,
  subscribe = false,
  unsubscribe = false,
  payloadOnSuccess,
  payloadOnFail
}) => (payload, formSubmit) => {
  // payload: action parameter that are passed to action creator
  let wsType = 'socketRequest'
  if (subscribe)
    wsType = 'socketSubscribe';
  else if (unsubscribe)
    wsType = 'socketUnsubscribe'

  const requestPayload = {
    messageType,
    payload: {
      ...(fields ? pick(defaultParams, fields) : defaultParams),
      ...(fields ? pick(payload, fields) : payload),
    }
  }

  return {
    type,
    [wsType]: requestPayload,
    selectorKey: selectorKey || messageType,
    payloadOnSuccess,
    payloadOnFail,
    formSubmit
  }
}
