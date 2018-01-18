import React, { Component } from 'react'
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
        schedule: a.scheduleId, // `/attendance/schedules/${a.scheduleId}/${c.clientReference}`
        blockCount: schedule.blockCount,
        presentCount: a.count,
        absentCount: schedule.blockCount - a.count,
        attendanceRate: `${Math.floor(a.count / (schedule.blockCount || 1) * 100)}%`,
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
      attendanceRate: `${Math.floor(presentCount / (blockCount || 1) * 100)}%`,
      attendance: attendanceData,
      key: c.clientReference
    }
  }) : []

const outerColumns = [
  { title: 'Name', dataIndex: 'name', key: 'date' },
  { title: 'Lectures', dataIndex: 'blockCount', key: 'blockCount' },
  { title: 'Present', dataIndex: 'presentCount', key: 'presentCount' },
  { title: 'Absent', dataIndex: 'absentCount', key: 'absentCount' },
  { title: 'Attendance', dataIndex: 'attendanceRate', key: 'attendanceRate' },
]

const innerColumns = [
  { title: 'Modules', dataIndex: 'schedule', key: 'date',
    render: (record) => {
      return 
    }
  },
  { title: 'Lectures', dataIndex: 'blockCount', key: 'blockCount' },
  { title: 'Present', dataIndex: 'presentCount', key: 'presentCount' },
  { title: 'Absent', dataIndex: 'absentCount', key: 'absentCount' },
  { title: 'Attendance', dataIndex: 'attendanceRate', key: 'attendanceRate' },
]

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

  renderExpandedRow = (record) => {
    return (
      <Table
        columns={innerColumns}
        dataSource={record.attendance}
        pagination={false}
      />
    );
  }

  render() {
    const { contactScheduleReport, scheduleStats } = this.props
    const { mode } = this.state
    const totalCommitters = scheduleStats.reduce((acc, v) => acc + v.committerCount, 0)
    const totalBlocks     = scheduleStats.reduce((acc, v) => acc + v.blockCount, 0)
    const totalPresent    = scheduleStats.reduce((acc, v) => acc + v.overallAttendance, 0)
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
        {mode === "schedules" ?
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
                <td>{totalBlocks}</td>
                <td>{totalPresent}</td>
                <td>{(totalCommitters * totalBlocks) - totalPresent}</td>
                <td>—</td>
              </tr>
              {scheduleStats.map((schedule) => {
                const percentage = (schedule.blockCount === 0 || schedule.committerCount === 0) ? 0 :
                  (schedule.overallAttendance / (schedule.committerCount * schedule.blockCount)) * 100;
                return <tr key={schedule.scheduleId}>
                  <td><Link className="unifi-link" to={`/attendance/schedules/${schedule.scheduleId}`}>{schedule.name}</Link></td>
                  <td>{schedule.committerCount}</td>
                  <td>{schedule.blockCount}</td>
                  <td>{schedule.overallAttendance}</td>
                  <td>{(schedule.committerCount * schedule.blockCount) - schedule.overallAttendance}</td>
                  <td>{Math.floor(percentage)}%</td>
                </tr>;
              })}
            </tbody>
          </table>
          :
          <Table
            className={bemE('committers-table')}
            columns={outerColumns}
            dataSource={getCommittersTableData(contactScheduleReport)}
            expandedRowRender={this.renderExpandedRow}
            pagination={false}
            defaultExpandAllRows
          />
        }
      </div>
    )
  }
}

const selector = createStructuredSelector({
  contactScheduleReport: contactScheduleReportSelector,
  scheduleStats: schedulesSelector
})

const actions = {
  listScheduleStats,
  reportContactScheduleAttendance
}

export default connect(selector, actions)(AttendanceReports)
