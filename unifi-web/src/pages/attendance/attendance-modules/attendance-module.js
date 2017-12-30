import React, { Component } from 'react';
import './attendance-module.scss';

import EvacuationProgressBar from "../../../components/evacuation-progress-bar";

export default class AttendanceModule extends Component {
  render() {
    return (
      <div className="attendanceModule">
        <div className="title">
          <h2>{this.props.title}</h2>
        </div>
        <div className="body">
          <EvacuationProgressBar percentage={this.props.attendance} warningThreshold={80} criticalThreshold={50} />
          <p className="label">Overall Attendance to Date</p>
          <p className="dates"><span>Dates:</span>&nbsp;{this.props.startDate} – {this.props.endDate}</p>
          <p className="studentCount"><span>Students:</span>&nbsp;{this.props.studentCount}</p>
          <p className="lectureCount"><span>Lectures:</span>&nbsp;{this.props.lectureCount}</p>
        </div>
      </div>
    );
  }
}
