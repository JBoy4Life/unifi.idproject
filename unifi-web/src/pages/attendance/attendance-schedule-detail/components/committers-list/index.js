import React, { Component } from 'react'
import moment from 'moment'
import { connect } from 'react-redux'
import { Link } from 'react-router-dom'

import DialogBox from 'components/dialog-box'
import SearchableSelectField from 'components/searchable-select-field'
import { Button, Icon, TextInput } from 'elements'

const mapObject = (objectArray, keyField, dataField) =>
  objectArray.reduce((objectSoFar, newObject) => {
    objectSoFar[`${newObject[keyField]}`] = newObject[dataField]
    return objectSoFar
  }, {})

export class CommittersList extends Component {
  constructor(props) {
    super(props)

    this.state = {
      addCommitterDialogVisible: false,
      search: ''
    }
  }

  handleAddCommitterClick = () => {
    this.setState({
      addCommitterDialogVisible: true
    })
  }

  handleCommitterDialogAdd = () => {
    this.setState({
      addCommitterDialogVisible: false
    })
    // console.log(`Would have invoked API to add ${this.state.addCommitterSelectedKey}.`)
  }

  handleCommitterDialogCancel = () => {
    this.setState({
      addCommitterDialogVisible: false
    })
  }

  handleSearchChange = (event) => {
    this.setState({
      search: event.target.value.toLowerCase()
    })
  }

  handleCommitterClear = () => {
    this.setState({
      addCommitterSelectedKey: null
    })
  }

  handleCommitterSelect = (key) => {
    this.setState({
      addCommitterSelectedKey: key
    })
  }

  render() {
    const { contactAttendance, scheduleId } = this.props
    const committerMap = mapObject(
      contactAttendance.attendance || [],
      'clientReference',
      'name')
    const filteredStudents = contactAttendance.attendance.filter((c) => c.name.toLowerCase().indexOf(this.state.search) > -1)
    const numStudents = filteredStudents.length

    return (
      <div className="committers">
        {this.state.addCommitterDialogVisible &&
          <DialogBox>
            <h1>Add a student</h1>
            <SearchableSelectField
              inputId="committerSearch"
              inputClassName="unifi-input"
              data={committerMap}
              onItemSelect={this.handleCommitterSelect}
              onSelectionClear={this.handleCommitterClear}
            />
            <div className="buttons">
              <Button
                className="unifi-button primary"
                disabled={this.addCommitterSelectedKey ? "disabled" : ""}
                onClick={this.handleCommitterDialogAdd}
              >
                Add
              </Button>
              <Button
                className="unifi-button"
                onClick={this.handleCommitterDialogCancel}
              >
                Cancel
              </Button>
            </div>
          </DialogBox>
        }
        <div className="controls">
          <TextInput className="unifi-input" type="text" placeholder="Search" onChange={this.handleSearchChange} />
          <Button className="addCommitter" onClick={this.handleAddCommitterClick}>
            <Icon type="plus-circle-o" /> Add a student
          </Button>
        </div>
        <div className="views">
          <p>Showing {numStudents} students</p>
          <div className="buttons">
            <button className="table-view"></button>
            <button className="tile-view"></button>
            <button className="grid-view"></button>
          </div>
        </div>
        <table className="unifi-table">
          <thead>
            <tr>
              <th>Student</th>
              <th>No.</th>
              <th>Present</th>
              <th>Absent</th>
              <th>Attendance</th>
            </tr>
          </thead>
          <tbody>
            {filteredStudents.map((committer) => {
              return (
                <tr key={committer.clientReference}>
                  <td><Link to={`/attendance/schedules/${scheduleId}/${committer.clientReference}`}>{committer.name}</Link></td>
                  <td>{committer.clientReference}</td>
                  <td>{committer.attendedCount}</td>
                  <td>{contactAttendance.blockCount - committer.attendedCount}</td>
                  <td>{Math.floor((committer.attendedCount / contactAttendance.blockCount) * 100)}%</td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    )
  }
}

export default CommittersList
