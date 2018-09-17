import _ from 'lodash'

const correlationIdsDictionary = {}

export const addCorrelationId = (correlationId, component) => {
  if (!_.isEmpty(correlationIdsDictionary))
    correlationIdsDictionary[component] = [...correlationIdsDictionary[component], correlationId]
  else
    correlationIdsDictionary[component] = [correlationId]
}

export const getCorrelationId = (component) => correlationIdsDictionary[component]

export const removeCorrelationId = (component, correlationId) => {
  const index = correlationIdsDictionary[component].indexOf(correlationId)
  if (index > -1 )
    correlationIdsDictionary[component].splice(index, 1)
}
