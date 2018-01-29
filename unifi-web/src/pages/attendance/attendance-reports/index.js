import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import ResultsList from './components/results-list'
import ReportFilterForm from './components/report-filter-form'
import withClientId from 'hocs/with-client-id'
import { listHolders, listProgrammes } from 'reducers/settings/actions'
import { listScheduleStats } from 'reducers/attendance/actions'
import { parseQueryString, jsonToQueryString } from 'utils/helpers'
import { holdersSelector, programmesSelector } from 'reducers/settings/selectors'
import { schedulesSelector } from 'reducers/attendance/selectors'

import { Col, Row, Select } from 'elements'

import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'attendance-reports'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`
const Option = Select.Option;

export class AttendanceReports extends Component {
  static propTypes = {
    clientId: PropTypes.string,
    listProgrammes: PropTypes.func,
    location: PropTypes.object
  }

  componentWillMount() {
    const { clientId, listHolders, listProgrammes, listScheduleStats } = this.props
    listHolders(clientId)
    listProgrammes(clientId)
    listScheduleStats()
  }

  handleProgrammeChange = (programme) => {
    const { history, location } = this.props
    history.push({
      pathname: location.pathname,
      search: jsonToQueryString({ programme })
    })
  }

  handleFilterOption = (input, option) => {
    return option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
  }

  render() {
    const { clientId, holdersList, programmesList, schedules } = this.props
    const { programme, startDate, endDate } = parseQueryString(location.search)

    return (
      <div className={COMPONENT_CSS_CLASSNAME}>
        <Row type="flex" justify="center" align="middle">
          <Col sm={18}>
            <h1>Attendance Alert Report</h1>
          </Col>
          <Col sm={6}>
            <Select
              showSearch
              value={programme}
              style={{ width: '100%' }}
              placeholder="Select a programme"
              optionFilterProp="children"
              onChange={this.handleProgrammeChange}
              filterOption={this.handleFilterOption}
            >
              {programmesList.map((programme, index) => (
                <Option value={programme} key={index}>{programme}</Option>
              ))}
            </Select>
          </Col>
        </Row>
        {programme && (
          <Fragment>
            <h2 className={bemE('programme')}>{programme}</h2>
            <ReportFilterForm programme={programme} />
            {programme && (
              <ResultsList
                key={`${programme}-${startDate}-${endDate}`}
                clientId={clientId}
                programme={programme}
                holdersList={holdersList}
                schedules={schedules}
                startDate={startDate}
                endDate={endDate}
              />
            )}
          </Fragment>
        )}
      </div>
    )
  }
}

const selector = createStructuredSelector({
  holdersList: holdersSelector,
  schedules: schedulesSelector,
  programmesList: programmesSelector
})

const actions = {
  listHolders,
  listProgrammes,
  listScheduleStats
}

export default compose(
  withClientId,
  connect(selector, actions)
)(AttendanceReports)
