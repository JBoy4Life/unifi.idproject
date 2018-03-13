const stringResources = {
  'bad-format': 'Bad format'
}

export default (resourceId) =>
  stringResources[resourceId] || resourceId
