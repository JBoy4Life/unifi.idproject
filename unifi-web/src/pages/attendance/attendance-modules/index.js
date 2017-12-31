import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import EvacuationProgressBar from "../../../components/evacuation-progress-bar";

import AttendanceModule from "./attendance-module";

export default class AttendanceModules extends Component {
  constructor() {
    super();
    this.state = {
      modules: [
        {
          "scheduleId": "MSING007",
          "runsFrom": "2017-10-08T12:00:00Z",
          "runsTo": "2018-06-21T12:00:00Z",
          "attendeeCount": 5,
          "blockCount": 0,
          "overallAttendance": 64.5
        },
        {
          "scheduleId": "MSING022",
          "runsFrom": "2017-10-08T12:00:00Z",
          "runsTo": "2018-06-21T12:00:00Z",
          "attendeeCount": 4,
          "blockCount": 0,
          "overallAttendance": 5.4
        },
        {
          "scheduleId": "MSING025",
          "runsFrom": "2017-10-08T12:00:00Z",
          "runsTo": "2018-06-21T12:00:00Z",
          "attendeeCount": 4,
          "blockCount": 0,
          "overallAttendance": 63.4
        },
        {
          "scheduleId": "MSING028-B1",
          "runsFrom": "2017-10-08T12:00:00Z",
          "runsTo": "2018-06-21T12:00:00Z",
          "attendeeCount": 2,
          "blockCount": 0,
          "overallAttendance": 97.8
        },
        {
          "scheduleId": "MSING028-B2",
          "runsFrom": "2017-10-08T12:00:00Z",
          "runsTo": "2018-06-21T12:00:00Z",
          "attendeeCount": 2,
          "blockCount": 0,
          "overallAttendance": 10.3
        },
        {
          "scheduleId": "MSING052",
          "runsFrom": "2017-10-08T12:00:00Z",
          "runsTo": "2018-06-21T12:00:00Z",
          "attendeeCount": 3,
          "blockCount": 0,
          "overallAttendance": 1
        }
      ]
    };
  }
  render() {
    return (
      <div>
        <h1>Modules</h1>
        {this.state.modules.map((module) => {
          return <AttendanceModule key={module.scheduleId}
                                   scheduleId={module.scheduleId}
                                   title={module.scheduleId + " NEED TITLE"}
                                   attendance={module.overallAttendance}
                                   startDate={module.runsFrom}
                                   endDate={module.runsTo}
                                   studentCount={module.attendeeCount}
                                   lectureCount={module.blockCount} />
        })}
      </div>
    )
  }
}
