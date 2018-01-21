import React, { Component } from 'react'
import moment from 'moment'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import EvacuationProgressBar from 'components/evacuation-progress-bar'

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

export class AttendanceScheduleDetail extends Component {
  constructor(props) {
    super(props)

    this.state = {
      mode: 'schedule',
      addCommitterSelectedKey: null,
      schedule: {
        name: '',
        attendance: 0,
        startDate: null,
        endDate: null,
        committerCount: 0,
        blockCount: 0,
        processedBlockCount: 0
      }
    }
  }

  componentWillMount() {
    const { scheduleId } = this.props.match.params
    this.props.listScheduleStats(scheduleId)
    this.props.listBlocks(scheduleId)
    this.props.getContactAttendanceForSchedule(scheduleId)
  }

  componentWillReceiveProps(nextProps) {

    // Schedule information.
    if (nextProps.scheduleStats.length > 0) {
      let scheduleId = nextProps.match.params.scheduleId;
      let schedule = nextProps.scheduleStats.filter((schedule) => schedule.scheduleId === scheduleId)[0];
      let percentage = (schedule.processedBlockCount === 0 || schedule.committerCount === 0) ? 0 :
        (schedule.overallAttendance / (schedule.committerCount * schedule.processedBlockCount)) * 100;

      this.setState({
        schedule: {
          name: schedule.name,
          attendance: percentage,
          startDate: schedule.startTime,
          endDate:   schedule.endTime,
          committerCount: schedule.committerCount,
          blockCount: schedule.blockCount,
          processedBlockCount: schedule.processedBlockCount
        }
      });
    }
  }

  handleSwitchMode = (newMode) => () => {
    this.setState({
      mode: newMode
    });
  }

  render() {
    const { blocks, contactAttendance } = this.props
    const { scheduleId } = this.props.match.params
    let startDate  = moment(this.state.schedule.startDate).format('DD/MM/Y')
    let endDate    = moment(this.state.schedule.endDate).format('DD/MM/Y')

    return (
      <div className="attendanceScheduleDetail">
        <h1>{this.state.schedule.name}</h1>
        <div className="schedule-stats-summary">
          <EvacuationProgressBar percentage={Math.floor(this.state.schedule.attendance)} warningThreshold={90} criticalThreshold={70} />
          <p className="label">Overall Attendance to Date</p>
          <div className="stats">
            {(this.state.schedule.startDate === null) ?
              <p className="stat"><span>Dates:</span>{' '}Unscheduled</p>
              :
              <p className="stat"><span>Dates:</span>{' '}{startDate} – {endDate}</p>
            }
            <br />
            <p className="stat"><span>Students:</span>{' '}{this.state.schedule.committerCount}</p>
            <p className="stat"><span>Lectures:</span>{' '}{this.state.schedule.blockCount}</p>
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
  scheduleStats: schedulesSelector,
  blocks: blocksSelector,
  contactAttendance: contactAttendanceSelector
})

const actions = {
  listScheduleStats,
  listBlocks,
  getContactAttendanceForSchedule
}

export default connect(selector, actions)(AttendanceScheduleDetail);
