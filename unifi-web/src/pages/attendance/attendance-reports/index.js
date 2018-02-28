import React, { Component, Fragment } from 'react'
import cx from 'classnames'
import moment from 'moment'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import ResultsList from './components/results-list'
import ReportFilterForm from './components/report-filter-form'
import withClientId from 'hocs/with-client-id'
import { holdersSelector } from 'redux/holders/selectors'
import { listHolders } from 'redux/holders/actions'
import { listProgrammes } from 'redux/settings/actions'
import { listSchedules } from 'redux/attendance/actions'
import { parseQueryString, jsonToQueryString } from 'utils/helpers'
import { programmesSelector } from 'redux/settings/selectors'
import { schedulesSelector } from 'redux/attendance/selectors'

import { Col, Row, Select } from 'elements'

import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'attendance-reports'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`
const Option = Select.Option

export class AttendanceReports extends Component {
  static propTypes = {
    clientId: PropTypes.string,
    listProgrammes: PropTypes.func,
    location: PropTypes.object
  }

  componentWillMount() {
    const { clientId, listHolders, listProgrammes, listSchedules } = this.props
    listHolders(clientId)
    listProgrammes(clientId)
    listSchedules(clientId)
  }

  handleProgrammeChange = (programme) => {
    const { history, location } = this.props
    const { startDate, endDate } = parseQueryString(location.search)
    history.push({
      pathname: location.pathname,
      search: jsonToQueryString({
        programme,
        startDate,
        endDate
      })
    })
  }

  handleFilterOption = (input, option) => {
    return option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
  }

  handlePrint = () => {
    window.print();
    return true;
  }

  render() {
    const { clientId, holdersList, programmesList, schedules } = this.props
    const { programme, startDate, endDate } = parseQueryString(location.search)

    return (
      <div className={cx(COMPONENT_CSS_CLASSNAME, 'section-to-print')}>
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
            <ReportFilterForm programme={programme} onPrint={this.handlePrint} />
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
  listSchedules
}

export default compose(
  withClientId,
  connect(selector, actions)
)(AttendanceReports)
