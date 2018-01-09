import React, { Component } from 'react';
import { Link } from 'react-router-dom';

import EvacuationProgressBar from "../../../components/evacuation-progress-bar";
import moment from "moment";

import './attendance-schedule.scss';

export default class AttendanceSchedule extends Component {
  render() {
    let percentage = (this.props.lectureCount === 0 || this.props.committerCount === 0) ? 0 :
      (this.props.attendance / (this.props.committerCount * this.props.lectureCount)) * 100;
    let startDate = moment(this.props.startDate).format('DD/MM/Y');
    let endDate   = moment(this.props.endDate).format('DD/MM/Y');
    return (
        <div className="attendanceSchedule">
          <Link to={`/attendance/schedules/${this.props.scheduleId}`}>
            <div className="title">
              <h2>{this.props.title}</h2>
            </div>
            <div className="schedule-stats-summary">
              <EvacuationProgressBar percentage={percentage.toFixed(0)} warningThreshold={80} criticalThreshold={50} />
              <p className="label">Overall Attendance to Date</p>
              <div className="stats">
                {(this.props.startDate === null) ?
                  <p className="stat"><span>Dates:</span>&nbsp;Unscheduled</p>
                  :
                  <p className="stat"><span>Dates:</span>&nbsp;{startDate} – {endDate}</p>
                }
                <br />
                <p className="stat"><span>Students:</span>&nbsp;{this.props.committerCount}</p>
                <p className="stat"><span>Lectures:</span>&nbsp;{this.props.lectureCount}</p>
              </div>
            </div>
          </Link>
        </div>
    );
  }
}
