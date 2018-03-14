import React, { Component } from 'react'
import fp from 'lodash/fp'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import * as attendanceActions from 'redux/modules/attendance/actions'
import AttendanceSchedule from './attendance-schedule'
import { Button, Col, Row } from 'elements'
import { scheduleStatsSelector } from 'redux/modules/attendance/selectors'
import { sortSchedules } from 'utils/helpers'
import { withClientId } from 'hocs'

export class AttendanceSchedules extends Component {
  componentDidMount() {
    const { clientId } = this.props
    this.props.listScheduleStatsRequest({ clientId });
  }

  handlePrint = () => {
    window.print()
  }

  render() {
    const { schedules } = this.props

    return (
      <div className="section-to-print">
        <Row type="flex" justify="center" align="middle">
          <Col xs={20}>
            <h1>Modules</h1>
          </Col>
          <Col xs={4} className="text-right">
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

export default compose(
  withClientId,
  connect(selector, actions)
)(AttendanceSchedules);
