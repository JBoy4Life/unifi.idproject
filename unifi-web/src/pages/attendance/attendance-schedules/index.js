import React, { Component } from 'react'
import fp from 'lodash/fp'
import trimStart from 'lodash/trimStart'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import * as attendanceActions from 'reducers/attendance/actions'
import { schedulesSelector } from 'reducers/attendance/selectors'

import AttendanceSchedule from './attendance-schedule'

const sortSchedules = fp.sortBy((item) =>
  fp.compose(
    (str) => trimStart(str, '- '),
    fp.replace(item.scheduleId, '')
  )(item.name)
)

export class AttendanceSchedules extends Component {
  componentDidMount() {
    this.props.listScheduleStatsRequest();
  }

  render() {
    const { schedules } = this.props

    return (
      <div>
        <h1>Modules</h1>
        {sortSchedules(schedules).map((schedule) => (
          <AttendanceSchedule
            key={schedule.scheduleId}
            scheduleId={schedule.scheduleId}
            title={schedule.name}
            attendance={schedule.overallAttendance}
            startDate={schedule.startTime}
            endDate={schedule.endTime}
            committerCount={schedule.committerCount}
            blockCount={schedule.blockCount}
          />
        ))}
      </div>
    )
  }
}

const selector = createStructuredSelector({
  schedules: schedulesSelector
})

export const actions = {
  listScheduleStatsRequest: attendanceActions.listScheduleStats
}

export default connect(selector, actions)(AttendanceSchedules);
