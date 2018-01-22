import React, { Component } from 'react'
import moment from "moment"
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'

import EvacuationProgressBar from "../../../components/evacuation-progress-bar"

import './attendance-schedule.scss'

export default class AttendanceSchedule extends Component {
  static propTypes = {
    attendance: PropTypes.number,
    blockCount: PropTypes.number,
    committerCount: PropTypes.number,
    endDate: PropTypes.string,
    processedBlockCount: PropTypes.number,
    scheduleId: PropTypes.number,
    startDate: PropTypes.string,
    title: PropTypes.string
  }

  render() {
    const { attendance, blockCount, committerCount, processedBlockCount,
      scheduleId, startDate, endDate, title } = this.props
    const processedCount = committerCount * processedBlockCount
    const percentage = Math.round((attendance / (processedCount || 1)) * 100)

    const startDateF = moment(startDate).format('DD/MM/Y')
    const endDateF = moment(endDate).format('DD/MM/Y')

    return (
      <div className="attendanceSchedule">
        <Link to={`/attendance/schedules/${scheduleId}`}>
          <div className="title">
            <h2>{title}</h2>
          </div>
          <div className="schedule-stats-summary">
            <EvacuationProgressBar
              percentage={percentage}
              warningThreshold={90}
              criticalThreshold={70}
              tbd={!processedCount}
            />
            <p className="label">Overall Attendance to Date</p>
            <div className="stats">
              {(startDate === null) ? (
                <p className="stat"><span>Dates: </span>Unscheduled</p>
              ) : (
                <p className="stat"><span>Dates: </span>{startDateF} – {endDateF}</p>
              )}
              <br />
              <p className="stat"><span>Students: </span>{committerCount}</p>
              <p className="stat"><span>Lectures: </span>{blockCount}</p>
            </div>
          </div>
        </Link>
      </div>
    )
  }
}
