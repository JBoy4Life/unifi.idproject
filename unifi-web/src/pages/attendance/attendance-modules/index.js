import React, { Component } from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'

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
          let attendance = (module.overallAttendance / module.attendeeCount + module.blockCount);
          return <AttendanceModule key={module.scheduleId}
                                   scheduleId={module.scheduleId}
                                   title={`${module.scheduleId} ${module.name}`}
                                   attendance={attendance}
                                   startDate={module.runsFrom}
                                   endDate={module.runsTo}
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
