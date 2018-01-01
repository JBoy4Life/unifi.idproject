import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'

import EvacuationProgressBar from "../../../components/evacuation-progress-bar";

import AttendanceModule from "./attendance-module";
import * as attendanceActions from "../../../reducers/attendance/actions";

export class AttendanceModules extends Component {
  componentDidMount() {
    this.props.listScheduleStatsRequest();
  }
  render() {
    return (
      <div>
        <h1>Modules</h1>
        {this.props.modules.map((module) => {
          let attendance = module.overallAttendance / (module.committerCount * module.blockCount) * 100;
          console.log(attendance);
          return <AttendanceModule key={module.scheduleId}
                                   scheduleId={module.scheduleId}
                                   title={module.name}
                                   attendance={attendance}
                                   startDate={module.startTime}
                                   endDate={module.endTime}
                                   studentCount={module.attendeeCount}
                                   lectureCount={module.blockCount} />
        })}
      </div>
    )
  }
}

export function mapStateToProps(state) {
  return {
    modules: state.attendance.scheduleStats || []
  };
}

export const mapDispatch = dispatch => (bindActionCreators({
  listScheduleStatsRequest: attendanceActions.listScheduleStats,
}, dispatch));

export default connect(mapStateToProps, mapDispatch)(AttendanceModules);

export class AttendanceModuleDetail extends Component {
  render() {
    return (
      <div className="attendanceModuleDetail">
        <h1>MSIN1001 Foundations of Management</h1>
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
            <h2>Jan 2018</h2>
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
              <tr>
                <td>
                  <p className="numeral">1</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture blub preconditioning potato coagulation water</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">2</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture blub preconditioning potato coagulation water</p>
                </td>
                <td>
                  <p className="numeral">3</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">4</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">5</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">6</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">7</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
              </tr>
              <tr>
                <td>
                  <p className="numeral">8</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">9</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">10</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">11</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">12</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">13</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">14</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
              </tr>
              <tr>
                <td>
                  <p className="numeral">15</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">16</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">17</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">18</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">19</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">20</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">21</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
              </tr>
              <tr>
                <td>
                  <p className="numeral">22</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">23</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">24</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">25</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">26</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">27</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">28</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
              </tr>
              <tr>
                <td>
                  <p className="numeral">29</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">30</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                  <p className="numeral">31</p>
                  <p className="block"><span className="time">09:00–10:00</span><br />Lecture N</p>
                </td>
                <td>
                </td>
                <td>
                </td>
                <td>
                </td>
                <td>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    );
  }
}
