import React, { Component } from 'react'
import PropTypes from 'prop-types'
import groupBy from 'lodash/groupBy'
import fp from 'lodash/fp'
import moment from 'moment'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import ScheduleTable from '../schedule-table'
import { Collapse } from 'elements'
import { lowAttendanceReportSelector } from 'reducers/attendance/selectors'
import { reportLowAttendanceByMetadata } from 'reducers/attendance/actions'
import { sortSchedules } from 'utils/helpers'

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
      startTime: moment.utc(startDate).startOf('day'),
      endTime: moment.utc(endDate).endOf('day')
    })
  }

  render() {
    const { holdersList, lowAttendanceReport, programme, schedules } = this.props
    const groupedAttendance = groupBy(lowAttendanceReport.attendance, (item) => item.scheduleId)
    const fsSchedules = filterAndSort(Object.keys(groupedAttendance))(schedules)

    return (
      <div className={COMPONENT_CSS_CLASSNAME}>
        <Collapse bordered={false}>
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
      </div>
    )
  }
}

const selector = createStructuredSelector({
  lowAttendanceReport: lowAttendanceReportSelector
})

const actions = {
  reportLowAttendanceByMetadata
}

export default compose(
  connect(selector, actions)
)(ResultsList)
