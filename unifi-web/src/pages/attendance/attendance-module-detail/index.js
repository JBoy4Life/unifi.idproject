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
      selectedMonth: now.add(1, "month"),
      calendar: new Array(now.daysInMonth()).fill([])
    };
  }
  componentWillMount() {
    let scheduleId = this.props.match.params.scheduleId;
    this.props.listScheduleStatsRequest(scheduleId);
    this.props.listBlocksRequest(scheduleId);
  }
  componentWillReceiveProps(nextProps) {

    // Get number of days in month.
    let daysInMonth = this.state.selectedMonth.daysInMonth();

    // Generate the calendar, indexed by date of month
    let calendar = new Array(daysInMonth).fill([]);

    // Populate the calendar.
    nextProps.blocks.filter((block) => {
      // Selected month blocks only.
      let d = moment(block.startTime);
      return d.year() === this.state.selectedMonth.year() &&
        d.month() === this.state.selectedMonth.month();
    }).forEach((block) => {
      // Insert into calendar.
      let i = moment(block.startTime).date();
      calendar[i-1] = calendar[i-1].concat(block);
    });

    this.setState({ calendar });

  }
  generateWeekRows() {

    // Generate a “flat grid” by figuring out how many week rows we’ll have.
    // Use ISO week numbers — dividing by 7 won’t work
    let sw    = this.state.selectedMonth.startOf("month").isoWeek();
    let ew    = this.state.selectedMonth.endOf("month").isoWeek();
    let rows  = (ew-sw) + 1;
    let grid  = Array.from(Array(rows * 7).keys()).fill(<td />);

    // Offset into the grid by start-of-month weekday number.
    let offset = this.state.selectedMonth.startOf("month").day() - 1;

    // Populate the grid. Yes, it’s mutation, so what?
    for (let g = offset, i = 0; i < this.state.calendar.length; i++, g++) {
      grid[g] = <td>
        <p className="numeral">{i + 1}</p>
        {this.state.calendar[i].map((block) => {
          let st = moment(block.startTime).format("HH:mm");
          let et = moment(block.endTime).format("HH:mm");
          return <p className="block"><span className="time">{st}–{et}</span><br />{block.name}</p>
        })}
        <p className="block">&nbsp;</p>
      </td>;
    }

    // Now chunk it up into week rows.
    let calendar = Array.from(Array(rows).keys()).map((row) => {
      let s = row * 7;
      let e = s + 7;
      return <tr>
        {grid.slice(s, e)}
      </tr>
    });

    // I think we’re done.
    return calendar;

  }

  render() {
    return (
      <div className="attendanceModuleDetail">
        <h1>{this.props.match.params.scheduleId}</h1>
        <div className="stats">
          <EvacuationProgressBar percentage={94.2} warningThreshold={80} criticalThreshold={50} />
          <p className="label">Overall Attendance to Date</p>
          <p className="dates"><span>Dates:</span>&nbsp;08/10/2018 – 21/06/2019</p>
          <p className="studentCount"><span>Students:</span>&nbsp;15</p>
          <p className="lectureCount"><span>Lectures:</span>&nbsp;141</p>
        </div>
        <div className="tabs">
          <Link className="current" to={`/attendance/modules/${this.props.scheduleId}`}>Module</Link>
          <Link to={`/attendance/modules/${this.props.scheduleId}/students`}>Students</Link>
        </div>
        <div className="module">
          <div className="controls">
            <h2>{this.state.selectedMonth.format("MMM Y")}</h2>
            <button className="arrow">&lt;</button>
            <button className="arrow">&gt;</button>
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
              {this.generateWeekRows()}
            </tbody>
          </table>
        </div>
      </div>
    );
  }
}

export function mapStateToProps(state) {
  return {
    module: state.attendance.scheduleStats || [],
    blocks: state.attendance.blocks || []
  };
}

export const mapDispatch = dispatch => (bindActionCreators({
  listScheduleStatsRequest: attendanceActions.listScheduleStats,
  listBlocksRequest: attendanceActions.listBlocks,
}, dispatch));

export default connect(mapStateToProps, mapDispatch)(AttendanceModuleDetail);
