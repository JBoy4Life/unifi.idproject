import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import groupBy from 'lodash/groupBy'
import fp from 'lodash/fp'
import moment from 'moment'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import ScheduleTable from '../schedule-table'
import { API_PENDING, API_SUCCESS, API_FAIL } from 'redux/api/request'
import { Collapse, Spinner } from 'elements'
import { lowAttendanceReportSelector, lowAttendanceReportStatusSelector } from 'redux/attendance/selectors'
import { reportLowAttendanceByMetadata } from 'redux/attendance/actions'
import { sortSchedules } from 'utils/helpers'

import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'ar-results-list'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

const filterAndSort = (keys) => fp.compose(
  sortSchedules,
  fp.filter((item) => keys.includes(item.scheduleId))
)

export class ResultsList extends Component {
  static propTypes = {
    clientId: PropTypes.string,
    endDate: PropTypes.string,
    holdersList: PropTypes.array,
    programme: PropTypes.string,
    schedules: PropTypes.array,
    startDate: PropTypes.string
  }

  componentWillMount() {
    const { clientId, endDate, programme, reportLowAttendanceByMetadata, startDate } = this.props
    reportLowAttendanceByMetadata(clientId, {
      programme,
      startTime: startDate ? moment.utc(startDate).startOf('day').format() : undefined,
      endTime: endDate ? moment.utc(endDate).endOf('day').format() : undefined
    })
  }

  render() {
    const { holdersList, lowAttendanceReport, lowAttendanceReportStatus, programme, schedules } = this.props
    const groupedAttendance = groupBy(lowAttendanceReport.attendance, (item) => item.scheduleId)
    const fsSchedules = filterAndSort(Object.keys(groupedAttendance))(schedules)

    return (
      <div className={COMPONENT_CSS_CLASSNAME}>
        {lowAttendanceReportStatus === API_PENDING && <Spinner size={36} />}
        {lowAttendanceReportStatus === API_SUCCESS && (
          <Fragment>
            <Collapse bordered={false} className={bemE('browser')}>
              {fsSchedules.map((schedule) => (
                <Collapse.Panel header={schedule.name} key={schedule.scheduleId}>
                  <ScheduleTable
                    schedule={schedule}
                    report={groupedAttendance[schedule.scheduleId]}
                    holdersList={holdersList}
                  />
                </Collapse.Panel>
              ))}
            </Collapse>
            {fsSchedules.map((schedule) => (
              <div className={bemE('print')} key={schedule.scheduleId}>
                <h3 className={bemE('print-title')}>{schedule.name}</h3>
                <ScheduleTable
                  schedule={schedule}
                  report={groupedAttendance[schedule.scheduleId]}
                  holdersList={holdersList}
                />
              </div>
            ))}
          </Fragment>
        )}
        {lowAttendanceReportStatus === API_FAIL && <h2>Failed to load data</h2>}
      </div>
    )
  }
}

const selector = createStructuredSelector({
  lowAttendanceReport: lowAttendanceReportSelector,
  lowAttendanceReportStatus: lowAttendanceReportStatusSelector
})

const actions = {
  reportLowAttendanceByMetadata
}

export default compose(
  connect(selector, actions)
)(ResultsList)
