import fp from 'lodash/fp'

import { modelSelector, websocketStatusSelector } from 'redux/api/selectors'
import { API_PENDING, API_SUCCESS } from 'redux/api/constants'

// ------------------------------------
// Attendance module selectors
// ------------------------------------
export const blocksSelector = modelSelector('blocks', [])
export const blockReportSelector = modelSelector('blockReport', [])
export const contactAttendanceSelector = modelSelector('contactAttendance', { attendance: [] })
export const scheduleStatsSelector = modelSelector('scheduleStats', [])
export const schedulesSelector = modelSelector('schedules', [])
export const overrideAttendanceResultSelector = modelSelector('overrideAttendanceResult', {})
export const contactScheduleReportSelector = modelSelector('contactScheduleReport', [])
export const lowAttendanceReportSelector = modelSelector('lowAttendanceReport', {})
export const lowAttendanceReportStatusSelector = websocketStatusSelector('lowAttendanceReport')

// ------------------------------------
// Client module selectors
// ------------------------------------
export const clientsSelector = modelSelector('clientList', [])
export const currentClientSelector = modelSelector('clientDetail')
export const currentClientStatusSelector = websocketStatusSelector('clientDetail')
export const clientIsLoadingSelector = fp.compose(
  (status) => status === 'INIT' || status === API_PENDING,
  currentClientStatusSelector
)
export const clientIsLoadedSelector = fp.compose(
  fp.isEqual(API_SUCCESS),
  currentClientStatusSelector
)

// ------------------------------------
// Detectables module selectors
// ------------------------------------
export const detectablesListSelector = modelSelector('detectablesList', [])

// ------------------------------------
// Holder module selectors
// ------------------------------------
export const holdersSelector = modelSelector('holdersList', [])
export const holderDetailsSelector = modelSelector('holderDetails', null)
export const programmesSelector = modelSelector('programmesList', [])

// ------------------------------------
// Operator module selectors
// ------------------------------------
export const operatorListSelector = modelSelector('operatorList', [])
export const operatorListStatusSelector = websocketStatusSelector('operatorList')
export const operatorDetailsSelector = modelSelector('operatorDetails', null)
export const operatorDetailsStatusSelector = websocketStatusSelector('operatorDetails')

// ------------------------------------
// Site module selectors
// ------------------------------------
export const siteSelector = modelSelector('sitesList[0]', null)
export const siteIdSelector = fp.compose(
  fp.get('siteId'),
  siteSelector
)
export const zonesInfoSelector = modelSelector('zonesInfo', {})
export const liveDiscoverySelector = modelSelector('liveDiscovery', [])
export const sitesInfoSelector = modelSelector('sitesList', [])

// ------------------------------------
// User module selectors
// ------------------------------------
export const userSelector = fp.get('user')
export const currentUserSelector = fp.compose(
  fp.get('currentUser'),
  userSelector
)
export const loginStatusSelector = fp.compose(
  fp.get('isLoggingIn'),
  userSelector
)
export const verticalConfigSelector = fp.compose(
  fp.get('verticalConfig'),
  currentUserSelector
)
export const attendanceEnabledSelector = fp.compose(
  Boolean,
  fp.get('attendance'),
  verticalConfigSelector
)
export const liveViewEnabledSelector = fp.compose(
  fp.get('core.liveViewEnabled'),
  verticalConfigSelector
)
export const passwordResetInfoSelector = modelSelector('passwordResetInfo')
export const passwordResetInfoStatusSelector = websocketStatusSelector('passwordResetInfo')
export const setPasswordStatusSelector = websocketStatusSelector('setPassword')
export const requestPasswordResetStatusSelector = websocketStatusSelector('requestPasswordReset')
export const cancelPasswordResetStatusSelector = websocketStatusSelector('cancelPasswordReset')
export const changePasswordStatusSelector = websocketStatusSelector('changePassword')
