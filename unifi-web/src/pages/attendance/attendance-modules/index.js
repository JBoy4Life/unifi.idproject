import React, { Component } from 'react';

import AttendanceModule from "./attendance-module";

export default class AttendanceModules extends Component {
  render() {
    return (
      <div>
        <h1>Modules</h1>
        <AttendanceModule title="MSIN1001 Foundations of Management"
                attendance={94.2}
                startDate="08/10/2018"
                endDate="21/06/2019"
                studentCount="15"
                lectureCount="141" />
        <AttendanceModule title="MSIN1007 Information Management for Business Intelligence"
                          attendance={87.6}
                          startDate="08/10/2018"
                          endDate="21/06/2019"
                          studentCount="65"
                          lectureCount="125" />
        <AttendanceModule title="INST6002 Web Technologies, Users and Management"
                          attendance={73.2}
                          startDate="08/10/2018"
                          endDate="21/06/2019"
                          studentCount="20"
                          lectureCount="141" />
        <AttendanceModule title="INST1003 Information Systems"
                          attendance={94.2}
                          startDate="08/10/2018"
                          endDate="21/06/2019"
                          studentCount="65"
                          lectureCount="125" />
      </div>
    )
  }
}
