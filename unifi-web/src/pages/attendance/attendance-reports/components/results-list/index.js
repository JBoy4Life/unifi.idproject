import React, { Component } from 'react'
import PropTypes from 'prop-types'
import groupBy from 'lodash/groupBy'
import find from 'lodash/find'
import moment from 'moment'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import ScheduleTable from '../schedule-table'
import { Collapse } from 'elements'
import { lowAttendanceReportSelector } from 'reducers/attendance/selectors'
import { reportLowAttendanceByMetadata } from 'reducers/attendance/actions'

const COMPONENT_CSS_CLASSNAME = 'ar-results-list'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

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

    return (
      <div className={COMPONENT_CSS_CLASSNAME}>
        <Collapse bordered={false}>
          {Object.keys(groupedAttendance).map((scheduleId) => {
            const schedule = find(schedules, { scheduleId })
            return (
              <Collapse.Panel header={schedule.name} key={scheduleId}>
                <ScheduleTable
                  schedule={schedule}
                  report={groupedAttendance[scheduleId]}
                  holdersList={holdersList}
                />
              </Collapse.Panel>
            )
          })}
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
