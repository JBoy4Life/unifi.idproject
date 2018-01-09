import React, { Component } from 'react';
import {connect} from "react-redux";
import {Link} from "react-router-dom";
import {bindActionCreators} from "redux";
import * as attendanceActions from "../../../reducers/attendance/actions";

export class AttendanceReports extends Component {
  constructor(props) {
    super(props);
    this.state = {
      mode: "schedules"
    };
  }
  componentWillMount() {
    this.props.listScheduleStatsRequest();
    this.props.reportContactScheduleAttendance();
  }
  switchMode(newMode) {
    this.setState({
      mode: newMode
    });
  }
  render() {
    const totalCommitters = this.props.scheduleStats.reduce((acc, v) => acc + v.committerCount, 0);
    const totalBlocks     = this.props.scheduleStats.reduce((acc, v) => acc + v.blockCount, 0);
    const totalPresent    = this.props.scheduleStats.reduce((acc, v) => acc + v.overallAttendance, 0);
    return (
      <div className="attendanceReports">
        <h1>Reports</h1>
        <div className="tabs">
          <Link className={this.state.mode === "schedules"  ? "current" : ""} onClick={() => this.switchMode("schedules")}  to={`/attendance/reports`}>All Modules</Link>
          <Link className={this.state.mode === "committers" ? "current" : ""} onClick={() => this.switchMode("committers")} to={`/attendance/reports`}>All Students</Link>
        </div>
        <div className="downloads">
        </div>
        <div className="views">
          <p>Showing {this.props.scheduleStats.length} results</p>
        </div>
        {this.state.mode === "schedules" ?
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
              {this.props.scheduleStats.map((schedule) => {
                const percentage = (schedule.blockCount === 0 || schedule.committerCount === 0) ? 0 :
                  (schedule.overallAttendance / (schedule.committerCount * schedule.blockCount)) * 100;
                return <tr key={schedule.scheduleId}>
                  <td><Link className="unifi-link" to={`/attendance/schedules/${schedule.scheduleId}`}>{schedule.name}</Link></td>
                  <td>{schedule.committerCount}</td>
                  <td>{schedule.blockCount}</td>
                  <td>{schedule.overallAttendance}</td>
                  <td>{(schedule.committerCount * schedule.blockCount) - schedule.overallAttendance}</td>
                  <td>{percentage.toFixed(0)}%</td>
                </tr>;
              })}
            </tbody>
          </table>
          :
          <table className="unifi-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Modules</th>
                <th>Lectures</th>
                <th>Present</th>
                <th>Absent</th>
                <th>Attendance</th>
              </tr>
            </thead>
            <tbody>
              <tr className="summary">
                <td>Total</td>
                <td>–</td>
                <td>–</td>
                <td>–</td>
                <td>–</td>
                <td>–</td>
              </tr>
              {[].concat.apply([], this.props.contactScheduleReport.attendance.map((c) => {
                return c.attendance.map((a) => {
                  const schedule = this.props.contactScheduleReport.schedules.find((s) => s.scheduleId === a.scheduleId);
                  return <tr key={`${c.clientReference}-${a.scheduleId}`}>
                    <td>{c.name}</td>
                    <td><Link to={`/attendance/schedules/${a.scheduleId}/${c.clientReference}`}>{a.scheduleId}</Link></td>
                    <td>{schedule.blockCount}</td>
                    <td>{a.count}</td>
                    <td>{schedule.blockCount-a.count}</td>
                    <td>{((a.count/schedule.blockCount)*100).toFixed(0)}%</td>
                  </tr>
                });
              }))}
            </tbody>
          </table>
        }
      </div>
    )
  }
}

export function mapStateToProps(state) {
  return {
    scheduleStats: state.attendance.scheduleStats || [],
    contactScheduleReport: state.attendance.contactScheduleReport || []
  };
}

export const mapDispatch = dispatch => (bindActionCreators({
  listScheduleStatsRequest: attendanceActions.listScheduleStats,
  reportContactScheduleAttendance: attendanceActions.reportContactScheduleAttendance
}, dispatch));

export default connect(mapStateToProps, mapDispatch)(AttendanceReports);
