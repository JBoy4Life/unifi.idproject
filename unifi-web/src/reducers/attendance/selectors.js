import fp from 'lodash/fp'

export const getReducer = fp.get('attendance')

export const schedulesSelector = fp.compose(
  fp.get('scheduleStats'),
  getReducer
)
