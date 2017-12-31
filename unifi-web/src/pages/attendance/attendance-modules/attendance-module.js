import React, { Component } from 'react';
import { Link } from 'react-router-dom';

import './attendance-module.scss';

import EvacuationProgressBar from "../../../components/evacuation-progress-bar";
import moment from "moment";

export default class AttendanceModule extends Component {
  render() {
    let startDate = moment(this.props.startDate).format('L');
    let endDate   = moment(this.props.endDate).format('L');
    return (
        <div className="attendanceModule">
          <Link to={`/attendance/modules/${this.props.scheduleId}`}>
            <div className="title">
              <h2>{this.props.title}</h2>
            </div>
            <div className="body">
              <EvacuationProgressBar percentage={this.props.attendance} warningThreshold={80} criticalThreshold={50} />
              <p className="label">Overall Attendance to Date</p>
              <p className="dates"><span>Dates:</span>&nbsp;{startDate} – {endDate}</p>
              <p className="studentCount"><span>Students:</span>&nbsp;{this.props.studentCount}</p>
              <p className="lectureCount"><span>Lectures:</span>&nbsp;{this.props.lectureCount}</p>
            </div>
          </Link>
        </div>
    );
  }
}
