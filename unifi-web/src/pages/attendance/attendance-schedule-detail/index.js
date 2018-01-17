import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'

import EvacuationProgressBar from "../../../components/evacuation-progress-bar";

import * as attendanceActions from "../../../reducers/attendance/actions";
import moment from "moment";
import DialogBox from "../../../components/dialog-box";
import SearchableSelectField from "../../../components/searchable-select-field";
import ModuleCalendar from './components/module-calendar'

export class AttendanceScheduleDetail extends Component {
  constructor(props) {
    super(props);
    let now = moment();
    this.state = {
      mode: "schedule",
      addCommitterDialogVisible: false,
      addCommitterSelectedKey: null,
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
  }

  addCommitterClick() {
    this.setState({
      addCommitterDialogVisible: true
    });
  }

  addCommitterDialogAdd() {
    this.setState({
      addCommitterDialogVisible: false
    });
    // console.log(`Would have invoked API to add ${this.state.addCommitterSelectedKey}.`);
  }

  addCommitterDialogCancel() {
    this.setState({
      addCommitterDialogVisible: false
    });
  }

  onCommitterClear() {
    this.setState({
      addCommitterSelectedKey: null
    });
  }

  onCommitterSelect(key) {
    this.setState({
      addCommitterSelectedKey: key
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
    const { blocks } = this.props
    let scheduleId = this.props.match.params.scheduleId;
    let startDate  = moment(this.state.schedule.startDate).format("DD/MM/Y");
    let endDate    = moment(this.state.schedule.endDate).format("DD/MM/Y");
    let committerMap = mapObject(
      this.props.contactAttendance.attendance || [],
      "clientReference",
      "name");
    return (
      <div className="attendanceScheduleDetail">
        {this.state.addCommitterDialogVisible ?
        <DialogBox>
          <h1>Add a student</h1>
          <SearchableSelectField inputId="committerSearch"
                                 inputClassName="unifi-input"
                                 data={committerMap}
                                 onItemSelect={(key) => this.onCommitterSelect(key) }
                                 onSelectionClear={() => this.onCommitterClear() }/>
          <div className="buttons">
            <button className="unifi-button primary"
                    disabled={this.addCommitterSelectedKey ? "disabled" : ""}
                    onClick={() => this.addCommitterDialogAdd()}>Add</button>
            <button className="unifi-button"
                    onClick={() => this.addCommitterDialogCancel()}>Cancel</button>
          </div>
        </DialogBox> : ""}
        <h1>{this.state.schedule.name}</h1>
        <div className="schedule-stats-summary">
          <EvacuationProgressBar percentage={Math.floor(this.state.schedule.attendance)} warningThreshold={80} criticalThreshold={50} />
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
          <ModuleCalendar blocks={blocks} />
          :
          <div className="committers">
            <div className="controls">
              <input className="unifi-input" type="text" placeholder="Search" onChange={(event) => this.searchChange(event)} />
              <button className="addCommitter" onClick={() => this.addCommitterClick()}>⊕ Add a student</button>
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
                  <th>No.</th>
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

function mapObject(objectArray, keyField, dataField) {
  return objectArray.reduce((objectSoFar, newObject) => {
    objectSoFar[`${newObject[keyField]}`] = newObject[dataField];
    return objectSoFar;
  }, {})
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
