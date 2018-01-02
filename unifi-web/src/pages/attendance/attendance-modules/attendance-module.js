import React, { Component } from 'react';
import { Link } from 'react-router-dom';

import EvacuationProgressBar from "../../../components/evacuation-progress-bar";
import moment from "moment";

import './attendance-module.scss';

export default class AttendanceModule extends Component {
  render() {
    let percentage = (this.props.lectureCount === 0 || this.props.studentCount === 0) ? 0 :
      (this.props.attendance / (this.props.studentCount * this.props.lectureCount)) * 100;
    let startDate = moment(this.props.startDate).format('DD/MM/Y');
    let endDate   = moment(this.props.endDate).format('DD/MM/Y');
    return (
        <div className="attendanceModule">
          <Link to={`/attendance/modules/${this.props.scheduleId}`}>
            <div className="title">
              <h2>{this.props.title}</h2>
            </div>
            <div className="body">
              <EvacuationProgressBar percentage={percentage.toPrecision(3)} warningThreshold={80} criticalThreshold={50} />
              <p className="label">Overall Attendance to Date</p>
              {(this.props.startDate === null) ?
                <p className="dates"><span>Dates:</span>&nbsp;Unscheduled</p>
                :
                <p className="dates"><span>Dates:</span>&nbsp;{startDate} – {endDate}</p>
              }
              <p className="studentCount"><span>Students:</span>&nbsp;{this.props.studentCount}</p>
              <p className="lectureCount"><span>Lectures:</span>&nbsp;{this.props.lectureCount}</p>
            </div>
          </Link>
        </div>
    );
  }
}
