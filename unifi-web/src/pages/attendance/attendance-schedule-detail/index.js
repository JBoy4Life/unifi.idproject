import React, { Component } from 'react'
import moment from 'moment'
import fp from 'lodash/fp'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import EvacuationProgressBar from 'components/evacuation-progress-bar'
import { Breadcrumb } from 'elements'

import {
  getContactAttendanceForSchedule,
  listBlocks,
  listScheduleStats
} from 'reducers/attendance/actions'

import {
  blocksSelector,
  contactAttendanceSelector,
  schedulesSelector
} from 'reducers/attendance/selectors'

import ScheduleCalendar from './components/schedule-calendar'
import CommittersList from './components/committers-list'

const singleScheduleSelector = (state, props) =>
  fp.compose(
    fp.defaultTo({}),
    fp.get('[0]'),
    fp.filter({ scheduleId: props.match.params.scheduleId }),
    fp.defaultTo([]),
    schedulesSelector
  )(state)

export class AttendanceScheduleDetail extends Component {
  static propTypes = {
    blocks: PropTypes.array,
    schedule: PropTypes.object,
    contactAttendance: PropTypes.object,
    listScheduleStats: PropTypes.func,
    listBlocks: PropTypes.func,
    match: PropTypes.object,
    getContactAttendanceForSchedule: PropTypes.func
  }

  constructor(props) {
    super(props)

    this.state = {
      mode: 'schedule',
      addCommitterSelectedKey: null,
    }
  }

  componentWillMount() {
    const { scheduleId } = this.props.match.params
    this.props.listScheduleStats(scheduleId)
    this.props.listBlocks(scheduleId)
    this.props.getContactAttendanceForSchedule(scheduleId)
  }

  handleSwitchMode = (newMode) => () => {
    this.setState({
      mode: newMode
    });
  }

  render() {
    const { blocks, contactAttendance, location, schedule } = this.props
    const { scheduleId } = this.props.match.params

    const startDate  = moment(schedule.startDate).format('DD/MM/Y')
    const endDate    = moment(schedule.endDate).format('DD/MM/Y')
    const processedCount = schedule.committerCount * schedule.processedBlockCount
    const percentage = Math.round(schedule.overallAttendance / (processedCount || 1) * 100);

    return (
      <div className="attendanceScheduleDetail">
        <Breadcrumb data={{
          title: schedule.name,
          pathname: location.pathname
        }} />
        <h1>{schedule.name}</h1>
        <div className="schedule-stats-summary">
          <EvacuationProgressBar
            percentage={percentage}
            tbd={!processedCount}
            warningThreshold={90}
            criticalThreshold={70}
          />
          <p className="label">Overall Attendance to Date</p>
          <div className="stats">
            {(schedule.startDate === null) ? (
              <p className="stat"><span>Dates:</span>{' '}Unscheduled</p>
            ) : (
              <p className="stat"><span>Dates:</span>{' '}{startDate} – {endDate}</p>
            )}
            <br />
            <p className="stat"><span>Students:</span>{' '}{schedule.committerCount}</p>
            <p className="stat"><span>Lectures:</span>{' '}{schedule.blockCount}</p>
          </div>
        </div>
        <div className="tabs">
          <Link
            className={this.state.mode === "schedule" ? "current" : ""}
            onClick={this.handleSwitchMode("schedule")}
            to={`/attendance/schedules/${scheduleId}`}
          >
            Module
          </Link>
          <Link
            className={this.state.mode === "committers" ? "current" : ""}
            onClick={this.handleSwitchMode("committers")}
            to={`/attendance/schedules/${scheduleId}`}
          >
            Students
          </Link>
        </div>
        {this.state.mode === "schedule" ? (
          <ScheduleCalendar blocks={blocks} />
        ) : (
          <CommittersList contactAttendance={contactAttendance} scheduleId={scheduleId} />
        )}
      </div>
    );
  }
}

const selector = createStructuredSelector({
  blocks: blocksSelector,
  schedule: singleScheduleSelector,
  contactAttendance: contactAttendanceSelector
})

const actions = {
  listScheduleStats,
  listBlocks,
  getContactAttendanceForSchedule
}

export default connect(selector, actions)(AttendanceScheduleDetail);
