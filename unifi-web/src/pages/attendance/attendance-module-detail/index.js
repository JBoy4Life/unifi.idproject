import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'

import EvacuationProgressBar from "../../../components/evacuation-progress-bar";

import * as attendanceActions from "../../../reducers/attendance/actions";
import moment from "moment";

export class AttendanceModuleDetail extends Component {
  constructor(props) {
    super(props);
    let now = moment();
    this.state = {
      mode: "module",
      selectedMonth: now,
      calendar: new Array(now.daysInMonth()).fill([]),
      module: {
        name: "",
        attendance: 0,
        startDate: null,
        endDate: null,
        studentCount: 0,
        lectureCount: 0,
      },
      search: ""
    };
  }
  componentWillMount() {
    let scheduleId = this.props.match.params.scheduleId;
    this.props.listScheduleStatsRequest(scheduleId);
    this.props.listBlocksRequest(scheduleId);
    this.props.getContactAttendanceForSchedule(scheduleId);
  }
  componentWillReceiveProps(nextProps) {

    // Module information.
    if (nextProps.scheduleStats.length > 0) {
      let scheduleId = nextProps.match.params.scheduleId;
      let module = nextProps.scheduleStats.filter((module) => module.scheduleId === scheduleId)[0];
      let percentage = (module.blockCount === 0 || module.committerCount === 0) ? 0 :
        (module.overallAttendance / (module.committerCount * module.blockCount)) * 100;

      this.setState({
        module: {
          name: module.name,
          attendance: percentage,
          startDate: module.startTime,
          endDate:   module.endTime,
          studentCount: module.committerCount,
          lectureCount: module.blockCount
        }
      });
    }

    this.setState({
      calendar: this.generateCalendar(nextProps)
    });

  }

  generateCalendar(props) {
    // Get number of days in month.
    let daysInMonth = this.state.selectedMonth.daysInMonth();

    // Generate the calendar, indexed by date of month
    let calendar = new Array(daysInMonth).fill([]);

    // Populate the calendar.
    props.blocks.filter((block) => {
      // Selected month blocks only.
      let d = moment(block.startTime);
      return d.year() === this.state.selectedMonth.year() &&
        d.month() === this.state.selectedMonth.month();
    }).forEach((block) => {
      // Insert into calendar.
      let i = moment(block.startTime).date();
      calendar[i - 1] = calendar[i - 1].concat(block);
    });

    return calendar;
  }

  generateWeekRows(calendar) {

    // Generate a “flat grid” by figuring out how many week rows we’ll have.
    // Use ISO week numbers — dividing by 7 won’t work
    let sw    = this.state.selectedMonth.startOf("month").isoWeek();
    let ew    = this.state.selectedMonth.endOf("month").isoWeek();
    let rows  = (ew-sw) + 1;
    let grid  = Array.from(Array(rows * 7).keys()).fill(<td />);

    // Offset into the grid by start-of-month weekday number.
    let offset = this.state.selectedMonth.startOf("month").day() - 1;

    // Populate the grid. Yes, it’s mutation, so what?
    for (let g = offset, i = 0; i < calendar.length; i++, g++) {
      grid[g] = <td key={i}>
        <p className="numeral">{i + 1}</p>
        {calendar[i].map((block) => {
          let st = moment(block.startTime).format("HH:mm");
          let et = moment(block.endTime).format("HH:mm");
          return <p key={block.blockId} className="block"><span className="time">{st}–{et}</span><br />{block.name}</p>
        })}
        <p className="block">&nbsp;</p>
      </td>;
    }

    // Now chunk it up into week rows.
    let chunked = Array.from(Array(rows).keys()).map((row) => {
      let s = row * 7;
      let e = s + 7;
      return <tr key={row}>
        {grid.slice(s, e)}
      </tr>
    });

    // I think we’re done.
    return chunked;

  }
  prevMonthClick() {
    this.setState({
      selectedMonth: this.state.selectedMonth.subtract(1, "month")
    });
  }
  nextMonthClick() {
    this.setState({
      selectedMonth: this.state.selectedMonth.add(1, "month")
    });
  }
  switchMode(newMode) {
    this.setState({
      mode: newMode
    });
  }
  searchChange(event) {
    this.setState({
      search: event.target.value
    });
  }
  render() {
    let calendar = this.generateCalendar(this.props);
    let scheduleId = this.props.match.params.scheduleId;
    let startDate = moment(this.state.module.startDate).format("DD/MM/Y");
    let endDate   = moment(this.state.module.endDate).format("DD/MM/Y");
    return (
      <div className="attendanceModuleDetail">
        <h1>{this.state.module.name}</h1>
        <div className="module-stats-summary">
          <EvacuationProgressBar percentage={this.state.module.attendance.toPrecision(2)} warningThreshold={80} criticalThreshold={50} />
          <p className="label">Overall Attendance to Date</p>
          <p className="stats">
            {(this.state.module.startDate === null) ?
              <p className="stat"><span>Dates:</span>&nbsp;Unscheduled</p>
              :
              <p className="stat"><span>Dates:</span>&nbsp;{startDate} – {endDate}</p>
            }
            <br />
            <p className="stat"><span>Students:</span>&nbsp;{this.state.module.studentCount}</p>
            <p className="stat"><span>Lectures:</span>&nbsp;{this.state.module.lectureCount}</p>
          </p>
        </div>
        <div className="tabs">
          <Link className={this.state.mode === "module"   ? "current" : ""} onClick={() => this.switchMode("module")} to={`/attendance/modules/${scheduleId}`}>Module</Link>
          <Link className={this.state.mode === "students" ? "current" : ""} onClick={() => this.switchMode("students")} to={`/attendance/modules/${scheduleId}`}>Students</Link>
        </div>
        {this.state.mode === "module" ?
          <div className="module">
            <div className="controls">
              <h2>{this.state.selectedMonth.format("MMM Y")}</h2>
              <button className="arrow" onClick={() => this.prevMonthClick()}>&lt;</button>
              <button className="arrow" onClick={() => this.nextMonthClick()}>&gt;</button>
              <button className="addLecture">⊕ Add a lecture</button>
            </div>
            <table className="timetable">
              <thead>
              <tr>
                <th>M</th>
                <th>T</th>
                <th>W</th>
                <th>T</th>
                <th>F</th>
                <th>S</th>
                <th>S</th>
              </tr>
              </thead>
              <tbody>
              {this.generateWeekRows(calendar)}
              </tbody>
            </table>
          </div>
          :
          <div className="students">
            <div className="controls">
              <input className="unifi-input" type="text" placeholder="Search" value={this.state.search} onChange={this.searchChange} />
              <button className="search" onClick={() => this.search()}>🔍</button>
              <button className="addStudent">⊕ Add a student</button>
            </div>
            <div className="views">
              <p>Showing {this.props.contactAttendance.attendance.length} students</p>
              <div className="buttons">
                <button className="table-view"></button>
                <button className="tile-view"></button>
                <button className="grid-view"></button>
              </div>
            </div>
            <table className="unifi-table">
              <thead>
                <tr>
                  <th>Student</th>
                  <th>UPI</th>
                  <th>Present</th>
                  <th>Absent</th>
                  <th>Attendance</th>
                </tr>
              </thead>
              <tbody>
              {this.props.contactAttendance.attendance.map((committer) => {
                return <tr key={committer.clientReference}>
                  <td>{committer.name}</td>
                  <td>{committer.clientReference}</td>
                  <td>{committer.attendedCount}</td>
                  <td>{this.props.contactAttendance.blockCount - committer.attendedCount}</td>
                  <td>{((committer.attendedCount/this.props.contactAttendance.blockCount)*100).toPrecision(3)}%</td>
                </tr>})}
              </tbody>
            </table>
          </div>}

      </div>
    );
  }
}

export function mapStateToProps(state) {
  return {
    scheduleStats: state.attendance.scheduleStats || [],
    blocks: state.attendance.blocks || [],
    contactAttendance: state.attendance.contactAttendance || {}
  };
}

export const mapDispatch = dispatch => (bindActionCreators({
  listScheduleStatsRequest: attendanceActions.listScheduleStats,
  listBlocksRequest: attendanceActions.listBlocks,
  getContactAttendanceForSchedule: attendanceActions.getContactAttendanceForSchedule
}, dispatch));

export default connect(mapStateToProps, mapDispatch)(AttendanceModuleDetail);
