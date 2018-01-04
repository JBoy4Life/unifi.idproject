import React, { Component } from 'react';
import {connect} from "react-redux";
import {Link} from "react-router-dom";
import {bindActionCreators} from "redux";
import * as attendanceActions from "../../../reducers/attendance/actions";

export class AttendanceReports extends Component {
  constructor(props) {
    super(props);
    this.state = {
      mode: "modules"
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
    const totalStudents = this.props.scheduleStats.reduce((acc, v) => acc + v.committerCount, 0);
    const totalLectures = this.props.scheduleStats.reduce((acc, v) => acc + v.blockCount, 0);
    const totalPresent  = this.props.scheduleStats.reduce((acc, v) => acc + v.overallAttendance, 0);
    return (
      <div className="attendanceReports">
        <h1>Reports</h1>
        <div className="tabs">
          <Link className={this.state.mode === "modules"  ? "current" : ""} onClick={() => this.switchMode("modules")}  to={`/attendance/reports`}>All Modules</Link>
          <Link className={this.state.mode === "students" ? "current" : ""} onClick={() => this.switchMode("students")} to={`/attendance/reports`}>All Students</Link>
        </div>
        <div className="downloads">
        </div>
        <div className="views">
          <p>Showing {this.props.scheduleStats.length} results</p>
        </div>
        {this.state.mode === "modules" ?
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
                <td>{totalStudents}</td>
                <td>{totalLectures}</td>
                <td>{totalPresent}</td>
                <td>{(totalStudents * totalLectures) - totalPresent}</td>
                <td>—</td>
              </tr>
              {this.props.scheduleStats.map((module) => {
                const percentage = (module.blockCount === 0 || module.committerCount === 0) ? 0 :
                  (module.overallAttendance / (module.committerCount * module.blockCount)) * 100;
                return <tr key={module.scheduleId}>
                  <td><Link className="unifi-link" to={`/attendance/modules/${module.scheduleId}`}>{module.name}</Link></td>
                  <td>{module.committerCount}</td>
                  <td>{module.blockCount}</td>
                  <td>{module.overallAttendance}</td>
                  <td>{(module.committerCount * module.blockCount) - module.overallAttendance}</td>
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
                    <td><Link to={`/attendance/modules/${a.scheduleId}/${c.clientReference}`}>{a.scheduleId}</Link></td>
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
