import React, { Component } from 'react'
import moment from 'moment'
import fp from 'lodash/fp'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import DialogBox from '../../../components/dialog-box'
import EvacuationProgressBar from 'components/evacuation-progress-bar'
import SearchableSelectField from '../../../components/searchable-select-field'

import {
  blockReportSelector,
  contactAttendanceSelector,
  overrideAttendanceResultSelector,
  schedulesSelector
} from 'reducers/attendance/selectors'

import {
  getContactAttendanceForSchedule,
  listScheduleStats,
  overrideAttendance,
  reportBlockAttendance
} from 'reducers/attendance/actions'

const absenceLabels = {
  'present': "Present",
  'absent': "Absent",
  'auth-absent': "Absent [Authorized]"
}

const sortBlockReport = fp.compose(
  fp.sortBy((item) => item.startDate),
  fp.map((item) => ({
    ...item,
    startTime: moment(item.startTime),
    endTime: moment(item.endTime)
  }))
)

export class AttendanceScheduleBlockDrilldown extends Component {
  constructor(props) {
    super(props)
    this.state = {
      committer: {
        name: ''
      },
      schedule: {
        name: '',
        attendance: 0,
        startDate: null,
        endDate: null,
        committerCount: 0,
        blockCount: 0,
      },
      editAttendanceVisible: false,
      editAttendanceSelectedStatus: null,
      editAttendanceBlockId: null
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.scheduleStats.length > 0) {
      let scheduleId = nextProps.match.params.scheduleId
      let schedule = nextProps.scheduleStats.filter((schedule) => schedule.scheduleId === scheduleId)[0]
      let percentage = (schedule.blockCount === 0 || schedule.committerCount === 0) ? 0 :
        (schedule.overallAttendance / (schedule.committerCount * schedule.blockCount)) * 100

      this.setState({
        schedule: {
          name: schedule.name,
          attendance: percentage,
          startDate: schedule.startTime,
          endDate:   schedule.endTime,
          committerCount: schedule.committerCount,
          blockCount: schedule.blockCount
        }
      })
    }
    if (nextProps.contactAttendance.attendance) {
      let contact = nextProps.contactAttendance.attendance.find((r) => r.clientReference === this.props.match.params.clientReference)
      if (contact) {
        this.setState({
          committer: {
            name: contact.name
          }
        })
      }
    }
    if (nextProps.overrideAttendanceResult !== this.props.overrideAttendanceResult &&
      this.props.overrideAttendanceResult !== null) {
      // Gotta refresh.
      const scheduleId = this.props.match.params.scheduleId;
      const clientReference = this.props.match.params.clientReference;
      this.props.listScheduleStats(scheduleId);
      this.props.reportBlockAttendance(scheduleId, clientReference);
      this.props.getContactAttendanceForSchedule(scheduleId);
    }
  }

  componentWillMount() {
    const scheduleId = this.props.match.params.scheduleId
    const clientReference = this.props.match.params.clientReference
    this.props.listScheduleStats(scheduleId)
    this.props.reportBlockAttendance(scheduleId, clientReference)
    this.props.getContactAttendanceForSchedule(scheduleId)
  }

  handleEditAttendance = (blockId) => () => {
    this.setState({
      editAttendanceVisible: true,
      editAttendanceBlockId: blockId
    });
  }

  onEditAttendanceCancel = () => {
    this.setState({
      editAttendanceVisible: false
    });
  }

  onEditAttendanceSave = () => {
    this.props.overrideAttendance(
      this.props.match.params.clientReference,
      this.props.match.params.scheduleId,
      this.state.editAttendanceBlockId,
      this.state.editAttendanceSelectedStatus);
    this.setState({
      editAttendanceVisible: false
    });
  }

  handleNewAttendanceSelect = (key) => {
    this.setState({
      editAttendanceSelectedStatus: key
    });
  }

  handleNewAttendanceClear = () => {
    this.setState({
      editAttendanceSelectedStatus: null
    });
  }

  render() {
    const { blockReport } = this.props
    const { committer, schedule } = this.state

    return (
      <div className="attendanceScheduleBlockDrilldown">
        {this.state.editAttendanceVisible ?
          <DialogBox>
            <h1>Edit Attendance</h1>
            <SearchableSelectField 
              inputId="newAttendanceStatus"
              inputClassName="unifi-input"
              data={absenceLabels}
              onItemSelect={this.handleNewAttendanceSelect}
              onSelectionClear={this.handleNewAttendanceClear}
            />
            <div className="buttons">
              <button
                className="unifi-button primary"
                disabled={this.editAttendanceSelectedStatus ? "disabled" : ""}
                onClick={this.onEditAttendanceSave}
              >
                Save
              </button>
              <button
                className="unifi-button"
                onClick={this.onEditAttendanceCancel}
              >
                Cancel
              </button>
            </div>
          </DialogBox>
        :
          ""}
        <h1>{committer.name}</h1>
        <h2>{schedule.name}</h2>
        <div className="schedule-stats-summary">
          <EvacuationProgressBar percentage={Math.floor(schedule.attendance)} warningThreshold={80} criticalThreshold={50} />
          <p className="label">Overall Attendance</p>
          <div className="stats">
            <p className="stat"><span>Lectures:</span>{' '}{blockReport.length}</p>
            <br />
            <p className="stat"><span>Present:</span>{' '}{blockReport.filter((block) => block.status === "present").length}</p>
            <p className="stat"><span>Absent:</span>{' '}{blockReport.filter((block) => block.status === "absent").length}</p>
          </div>
        </div>
        <div className="views">
          <p>Showing {blockReport.length} lectures</p>
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
            {sortBlockReport(blockReport).map((block) => (
              <tr key={block.blockId}>
                <td className="times">
                  {block.startTime.format('DD/MM/Y')}, {block.startTime.format('HH:mm')}–{block.endTime.format('HH:mm')}
                </td>
                <td className="status">
                  {absenceLabels[block.status]}
                  {' '}
                  <Link
                    to="#"
                    onClick={this.handleEditAttendance(block.blockId)}
                  >
                    Edit
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    )
  }
}

const selector = createStructuredSelector({
  blockReport: blockReportSelector,
  contactAttendance: contactAttendanceSelector,
  overrideAttendanceResult: overrideAttendanceResultSelector,
  scheduleStats: schedulesSelector
})

const actions = {
  getContactAttendanceForSchedule,
  listScheduleStats,
  overrideAttendance,
  reportBlockAttendance
}

export default connect(selector, actions)(AttendanceScheduleBlockDrilldown)
