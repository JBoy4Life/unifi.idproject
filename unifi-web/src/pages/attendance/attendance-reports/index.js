import React, { Component } from 'react'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'
import sumBy from 'lodash/sumBy'

import {
  listScheduleStats,
  reportContactScheduleAttendance
} from 'reducers/attendance/actions'

import {
  contactScheduleReportSelector,
  schedulesSelector
} from 'reducers/attendance/selectors'

import { Table } from 'elements'

import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'attendance-reports'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

const getCommittersTableData = ({ attendance, schedules }) =>
  attendance ? attendance.map((c) => {
    const attendanceData = c.attendance.map((a) => {
      const schedule = schedules.find((s) => s.scheduleId === a.scheduleId);
      return {
        schedule: a.scheduleId,
        blockCount: schedule.blockCount,
        processedBlockCount: schedule.processedBlockCount,
        presentCount: a.count,
        absentCount: schedule.processedBlockCount - a.count,
        attendanceRate: Math.round(a.count / (schedule.processedBlockCount || 1) * 100),
        key: `${c.clientReference}-${a.scheduleId}`
      }
    })

    const blockCount = sumBy(attendanceData, 'blockCount')
    const presentCount = sumBy(attendanceData, 'presentCount')
    const absentCount = sumBy(attendanceData, 'absentCount')

    return {
      name: c.name,
      blockCount,
      presentCount,
      absentCount,
      attendanceRate: Math.round(presentCount / (blockCount || 1) * 100),
      attendance: attendanceData,
      key: c.clientReference
    }
  }) : []

export class AttendanceReports extends Component {
  constructor(props) {
    super(props);
    this.state = {
      mode: "schedules"
    };
  }

  componentWillMount() {
    this.props.listScheduleStats()
    this.props.reportContactScheduleAttendance()
  }

  handleSwitchMode = (newMode) => () => {
    this.setState({
      mode: newMode
    });
  }

  renderBlocksTable() {
    const { scheduleStats } = this.props
    const totalCommitters = scheduleStats.reduce((acc, v) => acc + v.committerCount, 0)
    const totalBlocks     = scheduleStats.reduce((acc, v) => acc + v.blockCount, 0)
    const totalProcessedBlocks = scheduleStats.reduce((acc, v) => acc + v.processedBlockCount, 0)
    const totalPresent    = scheduleStats.reduce((acc, v) => acc + v.overallAttendance, 0)

    return (
      <table className="unifi-table">
        <thead>
          <tr>
            <th>Modules</th>
            <th>Students</th>
            <th>Lectures</th>
            <th>Present</th>
            <th>Absent</th>
            <th>Attendance</th>
          </tr>
        </thead>
        <tbody>
          <tr className="summary">
            <td>Total</td>
            <td>{totalCommitters}</td>
            <td>{totalProcessedBlocks} / {totalBlocks}</td>
            <td>{totalPresent}</td>
            <td>{(totalCommitters * totalProcessedBlocks) - totalPresent}</td>
            <td>—</td>
          </tr>
          {scheduleStats.map((schedule) => {
            const percentage = (schedule.processedBlockCount === 0 || schedule.committerCount === 0) ? 0 :
              (schedule.overallAttendance / (schedule.committerCount * schedule.processedBlockCount)) * 100;
            return <tr key={schedule.scheduleId}>
              <td><Link className="unifi-link" to={`/attendance/schedules/${schedule.scheduleId}`}>{schedule.name}</Link></td>
              <td>{schedule.committerCount}</td>
              <td>{schedule.processedBlockCount} / {schedule.blockCount}</td>
              <td>{schedule.overallAttendance}</td>
              <td>{(schedule.committerCount * schedule.processedBlockCount) - schedule.overallAttendance}</td>
              <td>{Math.round(percentage)}%</td>
            </tr>;
          })}
        </tbody>
      </table>
    )
  }

  renderCommittersTable() {
    const { committersTableData } = this.props

    const processedBlockCount = sumBy(committersTableData, 'processedBlockCount')
    const blockCount = sumBy(committersTableData, 'blockCount')
    const presentCount = sumBy(committersTableData, 'presentCount')
    const absentCount = sumBy(committersTableData, 'absentCount')
    const attendanceRate = Math.round(presentCount / (processedBlockCount || 1) * 100)

    return (
      <table className="unifi-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Modules</th>
            <th className="text-right">Lectures</th>
            <th className="text-right">Present</th>
            <th className="text-right">Absent</th>
            <th className="text-right">Attendance</th>
          </tr>
        </thead>
        <tbody>
          <tr className="summary">
            <td>Total</td>
            <td>–</td>
            <td className="text-right">{blockCount}</td>
            <td className="text-right">{presentCount}</td>
            <td className="text-right">{absentCount}</td>
            <td className="text-right">{attendanceRate}%</td>
          </tr>
          {[].concat.apply([], committersTableData.map((c) =>
            c.attendance.map((a, index) => (
              <tr key={a.key}>
                {index === 0 &&
                  <td className="valign-top" rowSpan={c.attendance.length}>{c.name}</td>
                }
                <td>
                  <Link to={`/attendance/schedules/${a.schedule}/${c.clientReference}`}>
                    {a.schedule}
                  </Link>
                </td>
                <td className="text-right">{a.blockCount}</td>
                <td className="text-right">{a.presentCount}</td>
                <td className="text-right">{a.absentCount}</td>
                <td className="text-right">{a.attendanceRate}%</td>
              </tr>
            ))
          ))}
        </tbody>
      </table>
    )
  }

  render() {
    const { scheduleStats } = this.props
    const { mode } = this.state

    return (
      <div className={COMPONENT_CSS_CLASSNAME}>
        <h1>Reports</h1>
        <div className="tabs">
          <Link className={mode === "schedules" ? "current" : ""} onClick={this.handleSwitchMode("schedules")} to={`/attendance/reports`}>
            All Modules
          </Link>
          <Link className={mode === "committers" ? "current" : ""} onClick={this.handleSwitchMode("committers")} to={`/attendance/reports`}>
            All Students
          </Link>
        </div>
        <div className="downloads">
        </div>
        <div className="views">
          <p>Showing {scheduleStats.length} results</p>
        </div>
        {mode === "schedules" ? (
          this.renderBlocksTable()
        ) : (
          this.renderCommittersTable()
        )}
      </div>
    )
  }
}

const selector = createStructuredSelector({
  scheduleStats: schedulesSelector,
  committersTableData: compose(
    getCommittersTableData,
    contactScheduleReportSelector
  )
})

const actions = {
  listScheduleStats,
  reportContactScheduleAttendance
}

export default connect(selector, actions)(AttendanceReports)
