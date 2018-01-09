import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'

import EvacuationProgressBar from "../../../components/evacuation-progress-bar";

import * as attendanceActions from "../../../reducers/attendance/actions";
import moment from "moment";

export class AttendanceScheduleDetail extends Component {
  constructor(props) {
    super(props);
    let now = moment();
    this.state = {
      mode: "schedule",
      selectedMonth: now,
      calendar: new Array(now.daysInMonth()).fill([]),
      schedule: {
        name: "",
        attendance: 0,
        startDate: null,
        endDate: null,
        committerCount: 0,
        blockCount: 0,
      },
      search: ""
    };
    this.searchChange = this.searchChange.bind(this);
  }
  componentWillMount() {
    let scheduleId = this.props.match.params.scheduleId;
    this.props.listScheduleStatsRequest(scheduleId);
    this.props.listBlocksRequest(scheduleId);
    this.props.getContactAttendanceForSchedule(scheduleId);
  }
  componentWillReceiveProps(nextProps) {

    // Schedule information.
    if (nextProps.scheduleStats.length > 0) {
      let scheduleId = nextProps.match.params.scheduleId;
      let schedule = nextProps.scheduleStats.filter((schedule) => schedule.scheduleId === scheduleId)[0];
      let percentage = (schedule.blockCount === 0 || schedule.committerCount === 0) ? 0 :
        (schedule.overallAttendance / (schedule.committerCount * schedule.blockCount)) * 100;

      this.setState({
        schedule: {
          name: schedule.name,
          attendance: percentage,
          startDate: schedule.startTime,
          endDate:   schedule.endTime,
          committerCount: schedule.committerCount,
          blockCount: schedule.blockCount
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
      search: event.target.value.toLowerCase()
    });
  }
  render() {
    let calendar = this.generateCalendar(this.props);
    let scheduleId = this.props.match.params.scheduleId;
    let startDate = moment(this.state.schedule.startDate).format("DD/MM/Y");
    let endDate   = moment(this.state.schedule.endDate).format("DD/MM/Y");
    return (
      <div className="attendanceScheduleDetail">
        <h1>{this.state.schedule.name}</h1>
        <div className="schedule-stats-summary">
          <EvacuationProgressBar percentage={Math.floor(this.state.schedule.attendance.toFixed(0))} warningThreshold={80} criticalThreshold={50} />
          <p className="label">Overall Attendance to Date</p>
          <div className="stats">
            {(this.state.schedule.startDate === null) ?
              <p className="stat"><span>Dates:</span>&nbsp;Unscheduled</p>
              :
              <p className="stat"><span>Dates:</span>&nbsp;{startDate} – {endDate}</p>
            }
            <br />
            <p className="stat"><span>Students:</span>&nbsp;{this.state.schedule.committerCount}</p>
            <p className="stat"><span>Lectures:</span>&nbsp;{this.state.schedule.blockCount}</p>
          </div>
        </div>
        <div className="tabs">
          <Link className={this.state.mode === "schedule"   ? "current" : ""} onClick={() => this.switchMode("schedule")}   to={`/attendance/schedules/${scheduleId}`}>Module</Link>
          <Link className={this.state.mode === "committers" ? "current" : ""} onClick={() => this.switchMode("committers")} to={`/attendance/schedules/${scheduleId}`}>Students</Link>
        </div>
        {this.state.mode === "schedule" ?
          <div className="schedule">
            <div className="controls">
              <h2>{this.state.selectedMonth.format("MMM Y")}</h2>
              <button className="arrow" onClick={() => this.prevMonthClick()}>&lt;</button>
              <button className="arrow" onClick={() => this.nextMonthClick()}>&gt;</button>
              <button className="addBlock">⊕ Add a lecture</button>
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
          <div className="committers">
            <div className="controls">
              <input className="unifi-input" type="text" placeholder="Search" onChange={this.searchChange} />
              {/*<button className="search" onClick={() => this.searchChange(this.state.search)}>🔍</button>*/}
              <button className="addCommitter">⊕ Add a student</button>
            </div>
            <div className="views">
              <p>Showing {this.props.contactAttendance.attendance.filter((c) => c.name.toLowerCase().indexOf(this.state.search) > -1).length} students</p>
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
              {this.props.contactAttendance.attendance.filter((c) => c.name.toLowerCase().indexOf(this.state.search) > -1).map((committer) => {
                return <tr key={committer.clientReference}>
                  <td><Link to={`/attendance/schedules/${scheduleId}/${committer.clientReference}`}>{committer.name}</Link></td>
                  <td>{committer.clientReference}</td>
                  <td>{committer.attendedCount}</td>
                  <td>{this.props.contactAttendance.blockCount - committer.attendedCount}</td>
                  <td>{Math.floor((committer.attendedCount/this.props.contactAttendance.blockCount)*100)}%</td>
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

export default connect(mapStateToProps, mapDispatch)(AttendanceScheduleDetail);
