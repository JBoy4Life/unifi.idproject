import React, { Component } from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'

import AttendanceSchedule from "./attendance-schedule";
import * as attendanceActions from "../../../reducers/attendance/actions";

export class AttendanceSchedules extends Component {
  componentDidMount() {
    this.props.listScheduleStatsRequest();
  }
  render() {
    return (
      <div>
        <h1>Modules</h1>
        {this.props.schedules.map((schedule) => {
          return <AttendanceSchedule key={schedule.scheduleId}
                                     scheduleId={schedule.scheduleId}
                                     title={schedule.name}
                                     attendance={schedule.overallAttendance}
                                     startDate={schedule.startTime}
                                     endDate={schedule.endTime}
                                     studentCount={schedule.committerCount}
                                     lectureCount={schedule.blockCount} />
        })}
      </div>
    )
  }
}

export function mapStateToProps(state) {
  return {
    schedules: state.attendance.scheduleStats || []
  };
}

export const mapDispatch = dispatch => (bindActionCreators({
  listScheduleStatsRequest: attendanceActions.listScheduleStats,
}, dispatch));

export default connect(mapStateToProps, mapDispatch)(AttendanceSchedules);
