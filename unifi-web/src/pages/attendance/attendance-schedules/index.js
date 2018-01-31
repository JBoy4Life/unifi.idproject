import React, { Component } from 'react'
import fp from 'lodash/fp'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import * as attendanceActions from 'reducers/attendance/actions'
import AttendanceSchedule from './attendance-schedule'
import { Button, Col, Row } from 'elements'
import { scheduleStatsSelector } from 'reducers/attendance/selectors'
import { sortSchedules } from 'utils/helpers'

export class AttendanceSchedules extends Component {
  componentDidMount() {
    this.props.listScheduleStatsRequest();
  }

  handlePrint = () => {
    window.print()
  }

  render() {
    const { schedules } = this.props

    return (
      <div className="section-to-print">
        <Row type="flex" justify="center" align="middle">
          <Col sm={20}>
            <h1>Modules</h1>
          </Col>
          <Col sm={4} className="text-right">
            <Button className="no-print" onClick={this.handlePrint}>Print</Button>
          </Col>
        </Row>
        {sortSchedules(schedules).map((schedule) => (
          <AttendanceSchedule
            key={schedule.scheduleId}
            scheduleId={schedule.scheduleId}
            title={schedule.name}
            attendance={schedule.overallAttendance}
            processedBlockCount={schedule.processedBlockCount}
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
  schedules: scheduleStatsSelector
})

export const actions = {
  listScheduleStatsRequest: attendanceActions.listScheduleStats
}

export default connect(selector, actions)(AttendanceSchedules);
