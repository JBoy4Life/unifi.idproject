import React from 'react'
import fp from 'lodash/fp'

import { getAttendanceRate } from 'pages/attendance/helpers'
import './index.scss'

const getHolderName = (holdersList, clientReference) =>
  fp.compose(
    fp.get('name'),
    fp.defaultTo({}),
    fp.find({ clientReference })
  )(holdersList)

export default ({ schedule, report, holdersList }) => (
  <table className="unifi-table">
    <thead>
      <tr>
        <th>Students</th>
        <th>Present</th>
        <th>Absent</th>
        <th>Attendance</th>
      </tr>
    </thead>
    <tbody>
      {fp.sortBy('presentCount')(report).map((item) => (
        <tr key={item.clientReference}>
          <td>{getHolderName(holdersList, item.clientReference)}</td>
          <td>{item.presentCount}</td>
          <td>{item.absentCount}</td>
          <td>{getAttendanceRate(item)}%</td>
        </tr>
      ))}
    </tbody>
  </table>
)
