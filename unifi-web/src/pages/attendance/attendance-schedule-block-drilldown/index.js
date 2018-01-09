import React, { Component } from 'react'
import EvacuationProgressBar from "../../../components/evacuation-progress-bar";
import {bindActionCreators} from "redux";
import * as attendanceActions from "../../../reducers/attendance/actions";
import {connect} from "react-redux";
import moment from "moment";

export class AttendanceScheduleBlockDrilldown extends Component {
  constructor(props) {
    super(props);
    this.state = {
      committer: {
        name: ""
      },
      schedule: {
        name: "",
        attendance: 0,
        startDate: null,
        endDate: null,
        committerCount: 0,
        blockCount: 0,
      }
    }
  }
  componentWillReceiveProps(nextProps) {
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
    if (nextProps.contactAttendance.attendance) {
      let contact = nextProps.contactAttendance.attendance.find((r) => r.clientReference === this.props.match.params.clientReference);
      this.setState({
        committer: {
          name: contact.name
        }
      });
    }
  }
  componentWillMount() {
    let scheduleId = this.props.match.params.scheduleId;
    let clientReference = this.props.match.params.clientReference;
    this.props.listScheduleStats(scheduleId);
    this.props.reportBlockAttendance(scheduleId, clientReference);
    this.props.getContactAttendanceForSchedule(scheduleId);
  }
  render() {
    return (
      <div className="attendanceScheduleBlockDrilldown">
        <h1>{this.state.committer.name}</h1>
        <h2>{this.state.schedule.name}</h2>
        <div className="schedule-stats-summary">
          <EvacuationProgressBar percentage={this.state.schedule.attendance.toFixed(0)} warningThreshold={80} criticalThreshold={50} />
          <p className="label">Overall Attendance</p>
          <div className="stats">
            <p className="stat"><span>Lectures:</span>&nbsp;{this.props.blockReport.length}</p>
            <br />
            <p className="stat"><span>Present:</span>&nbsp;{this.props.blockReport.filter((block) => block.status === "present").length}</p>
            <p className="stat"><span>Absent:</span>&nbsp;{this.props.blockReport.filter((block) => block.status === "absent").length}</p>
          </div>
        </div>
        <div className="views">
          <p>Showing {this.props.blockReport.length} lectures</p>
          <div className="buttons">
            <button />
          </div>
        </div>
        <table className="unifi-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Attendance</th>
            </tr>
          </thead>
          <tbody>
          {this.props.blockReport.map((block) => {
            let st = moment(block.startTime);
            let et = moment(block.endTime);
            let d  = st.format("DD/MM/Y");
            return <tr key={block.blockId}>
              <td className="times">{d}, {st.format("HH:MM")}–{et.format("HH:MM")}</td>
              <td className="status">{block.status}</td>
            </tr>;
          })}
          </tbody>
        </table>
      </div>
    )
  }
}

export function mapStateToProps(state) {
  return {
    scheduleStats: state.attendance.scheduleStats || [],
    blockReport: state.attendance.blockReport || [],
    contactAttendance: state.attendance.contactAttendance || {}
  };
}

export const mapDispatch = dispatch => (bindActionCreators({
  listScheduleStats: attendanceActions.listScheduleStats,
  reportBlockAttendance: attendanceActions.reportBlockAttendance,
  getContactAttendanceForSchedule: attendanceActions.getContactAttendanceForSchedule
}, dispatch));

export default connect(mapStateToProps, mapDispatch)(AttendanceScheduleBlockDrilldown);
