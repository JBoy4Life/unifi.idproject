import React, { Component } from 'react'
import moment from 'moment'
import fp from 'lodash/fp'
import sortBy from 'lodash/sortBy'
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
  fp.sortBy((item) => item.startTime),
  fp.map((item) => ({
    ...item,
    startTime: moment(item.startTime),
    endTime: moment(item.endTime)
  }))
)

const getPercentage = (blockReport) => {
  const presentCount = fp.compose(
    fp.size,
    fp.filter({ status: 'present' })
  )(blockReport)
  const processedCount = fp.compose(
    fp.size,
    fp.reject({ status: null })
  )(blockReport)
  return presentCount / (processedCount || 1) * 100
}

const singleScheduleSelector = (state, props) =>
  fp.compose(
    fp.get('[0]'),
    fp.filter({ scheduleId: props.match.params.scheduleId }),
    fp.defaultTo([]),
    schedulesSelector
  )(state)

const committerSelector = (state, props) =>
  fp.compose(
    fp.find({ clientReference: props.match.params.clientReference }),
    fp.defaultTo([]),
    fp.get('attendance'),
    contactAttendanceSelector
  )(state)

export class AttendanceScheduleBlockDrilldown extends Component {
  constructor(props) {
    super(props)
    this.state = {
      editAttendanceVisible: false,
      editAttendanceSelectedStatus: null,
      editAttendanceBlockId: null
    }
  }

  componentWillMount() {
    const { clientReference, scheduleId } = this.props.match.params
    this.loadData(clientReference, scheduleId)
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.overrideAttendanceResult !== this.props.overrideAttendanceResult &&
      this.props.overrideAttendanceResult !== null) {
      // Gotta refresh.
      const { clientReference, scheduleId } = this.props.match.params
      this.loadData(clientReference, scheduleId)
    }
  }

  loadData(clientReference, scheduleId) {
    this.props.listScheduleStats(scheduleId)
    this.props.reportBlockAttendance(scheduleId, clientReference)
    this.props.getContactAttendanceForSchedule(scheduleId)
  }

  handleEditAttendance = (blockId) => () => {
    this.setState({
      editAttendanceVisible: true,
      editAttendanceBlockId: blockId
    })
  }

  handleEditAttendanceCancel = () => {
    this.setState({
      editAttendanceVisible: false
    })
  }

  handleEditAttendanceSave = () => {
    this.props.overrideAttendance(
      this.props.match.params.clientReference,
      this.props.match.params.scheduleId,
      this.state.editAttendanceBlockId,
      this.state.editAttendanceSelectedStatus)
    this.setState({
      editAttendanceVisible: false
    })
  }

  handleNewAttendanceSelect = (key) => {
    this.setState({
      editAttendanceSelectedStatus: key
    })
  }

  handleNewAttendanceClear = () => {
    this.setState({
      editAttendanceSelectedStatus: null
    })
  }

  render() {
    const { blockReport, committer, schedule } = this.props
    const sortedBlockReport = sortBlockReport(blockReport)
    const { clientReference } = this.props.match.params

    return (
      <div className="attendanceScheduleBlockDrilldown">
        {this.state.editAttendanceVisible &&
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
                onClick={this.handleEditAttendanceSave}
              >
                Save
              </button>
              <button
                className="unifi-button"
                onClick={this.handleEditAttendanceCancel}
              >
                Cancel
              </button>
            </div>
          </DialogBox>
        }
        <h1>{committer && committer.name}</h1>
        <h2>{schedule && schedule.name}</h2>
        <div className="schedule-stats-summary">
          <EvacuationProgressBar
            percentage={(getPercentage(sortedBlockReport))}
            warningThreshold={80}
            criticalThreshold={50}
          />
          <p className="label">Overall Attendance</p>
          <div className="stats">
            <p className="stat"><span>Student No:</span> {clientReference}</p>
            <p className="stat"><span>Lectures:</span> {blockReport.length}</p>
            <br />
            <p className="stat">
              <span>Present:</span> {blockReport.filter((block) => block.status === "present").length}
            </p>
            <p className="stat">
              <span>Absent:</span> {blockReport.filter((block) => block.status === "absent").length}
            </p>
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
            {sortedBlockReport.map((block) => (
              <tr key={block.blockId}>
                <td className="times">
                  {block.startTime.format('DD/MM/Y')},
                  {' '}
                  {block.startTime.format('HH:mm')} – {block.endTime.format('HH:mm')}
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
  overrideAttendanceResult: overrideAttendanceResultSelector,
  scheduleStats: schedulesSelector,
  schedule: singleScheduleSelector,
  committer: committerSelector
})

const actions = {
  getContactAttendanceForSchedule,
  listScheduleStats,
  overrideAttendance,
  reportBlockAttendance
}

export default connect(selector, actions)(AttendanceScheduleBlockDrilldown)
