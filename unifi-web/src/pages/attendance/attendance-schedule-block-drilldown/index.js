import React, { Component } from 'react'
import moment from 'moment'
import fp from 'lodash/fp'
import PropTypes from 'prop-types'
import sortBy from 'lodash/sortBy'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import * as ROUTES from 'utils/routes'
import DialogBox from 'components/dialog-box'
import EvacuationProgressBar from 'components/evacuation-progress-bar'
import SearchableSelectField from 'components/searchable-select-field'
import withClientId from 'hocs/with-client-id'

import { Breadcrumb } from 'elements'

import {
  blockReportSelector,
  contactAttendanceSelector,
  overrideAttendanceResultSelector,
  scheduleStatsSelector
} from 'reducers/attendance/selectors'

import {
  getContactAttendanceForSchedule,
  listScheduleStats,
  overrideAttendance,
  reportBlockAttendance
} from 'reducers/attendance/actions'

import { getHolder } from 'reducers/settings/actions'
import { holdersMetaSelector } from 'reducers/settings/selectors'

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

const getAbsentCount = fp.compose(
  fp.size,
  fp.filter({ status: 'absent' })
)

const getPresentCount = fp.compose(
  fp.size,
  fp.filter(item => ['present', 'auth-absent'].includes(item.status))
)

const getProcessedCount = fp.compose(
  fp.size,
  fp.reject({ status: null })
)

const singleScheduleSelector = (state, props) =>
  fp.compose(
    fp.get('[0]'),
    fp.filter({ scheduleId: props.match.params.scheduleId }),
    fp.defaultTo([]),
    scheduleStatsSelector
  )(state)

const committerSelector = (state, props) =>
  fp.compose(
    fp.find({ clientReference: props.match.params.clientReference }),
    fp.defaultTo([]),
    fp.get('attendance'),
    contactAttendanceSelector
  )(state)

const getHolderProgrammeSelector = (state, props) =>
  fp.compose(
    fp.get('metadata.programme'),
    fp.defaultTo({}),
    fp.find({ clientReference: props.match.params.clientReference }),
    holdersMetaSelector
  )(state)

export class AttendanceScheduleBlockDrilldown extends Component {
  static propTypes = {
    blockReport: PropTypes.array,
    committer: PropTypes.object,
    getContactAttendanceForSchedule: PropTypes.func,
    listScheduleStats: PropTypes.func,
    match: PropTypes.object,
    overrideAttendanceResult: PropTypes.object,
    reportBlockAttendance: PropTypes.func,
    schedule: PropTypes.object,
    scheduleStats: PropTypes.array
  }

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
    const { clientId } = this.props
    this.props.listScheduleStats(scheduleId)
    this.props.reportBlockAttendance(scheduleId, clientReference)
    this.props.getContactAttendanceForSchedule(scheduleId)
    this.props.getHolder(clientId, clientReference)
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
    const { blockReport, committer, location, programme, schedule } = this.props
    const sortedBlockReport = sortBlockReport(blockReport)
    const { clientReference } = this.props.match.params
    const processedCount = getProcessedCount(blockReport)
    const percentage = Math.round(getPresentCount(blockReport) / (processedCount || 1) * 100, 10)

    return (
      <div className="attendanceScheduleBlockDrilldown">
        <Breadcrumb data={{
          title: schedule ? schedule.name : '',
          pathname: ROUTES.ATTENDANCE_SCHEDULES_DETAIL.replace(
            ':scheduleId', schedule ? schedule.scheduleId : ':scheduleId'
          )
        }} />
        <Breadcrumb data={{
          title: committer ? committer.name : '',
          pathname: location.pathname
        }} />
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
        <h3>Programme: {programme || '(None)'}</h3>
        <div className="schedule-stats-summary">
          <EvacuationProgressBar
            percentage={percentage}
            warningThreshold={90}
            criticalThreshold={70}
            tbd={!processedCount}
          />
          <p className="label">Overall Attendance</p>
          <div className="stats">
            <p className="stat"><span>Student No:</span> {clientReference}</p>
            <p className="stat"><span>Lectures:</span> {blockReport.length}</p>
            <br />
            <p className="stat">
              <span>Present:</span> {getPresentCount(blockReport)}
            </p>
            <p className="stat">
              <span>Absent:</span> {getAbsentCount(blockReport)}
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
              <th width="65%">Date</th>
              <th width="35%">Attendance</th>
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
                  {block.statusOverridden ? ' (manual) ' : ' '}
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
  scheduleStats: scheduleStatsSelector,
  schedule: singleScheduleSelector,
  committer: committerSelector,
  programme: getHolderProgrammeSelector
})

const actions = {
  getContactAttendanceForSchedule,
  getHolder,
  listScheduleStats,
  overrideAttendance,
  reportBlockAttendance
}

export default compose(
  connect(selector, actions),
  withClientId
)(AttendanceScheduleBlockDrilldown)
