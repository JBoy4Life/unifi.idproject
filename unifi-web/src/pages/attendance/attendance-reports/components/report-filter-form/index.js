import React, { Component } from 'react'
import PropTypes from 'prop-types'
import moment from 'moment'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Field, reduxForm } from 'redux-form'
import { Link } from 'react-router-dom'
import { withRouter } from 'react-router'

import { Button, Form, FormItem } from 'elements'
import { DateRangeField } from 'components'
import { lowAttendanceReportSelector } from 'redux/selectors'
import { parseQueryString, jsonToQueryString } from 'utils/helpers'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'ar-report-filter-form'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

const getDateRangeParams = (dateRange) =>
  dateRange && dateRange.length > 0 ? {
    startDate: dateRange[0].format('YYYY-MM-DD'),
    endDate: dateRange[1].format('YYYY-MM-DD')
  } : {}

export class ReportFilterForm extends Component {
  static propTypes = {
    handleSubmit: PropTypes.func,
    history: PropTypes.object,
    location: PropTypes.object,
    onPrint: PropTypes.func
  }

  doSubmit = (values) => {
    const { history, location, programme, reset } = this.props
    history.push({
      pathname: location.pathname,
      search: jsonToQueryString({
        programme,
        ...getDateRangeParams(values.dateRange)
      })
    })
    if (!values.dateRange || values.dateRange.length === 0) {
      reset()
    }
  }

  render() {
    const { handleSubmit, location, onPrint } = this.props

    return (
      <Form
        className={COMPONENT_CSS_CLASSNAME}
        layout="inline"
        onSubmit={handleSubmit(this.doSubmit)}
      >
        <Field
          name="dateRange"
          component={DateRangeField}
        />
        <FormItem>
          <Button type="primary" htmlType="submit" className="no-print">Go</Button>
        </FormItem>
        <FormItem>
          <Button onClick={onPrint} className="no-print">Print</Button>
        </FormItem>
      </Form>
    )
  }
}

const formInitialValuesSelector = (state, props) => {
  const report = lowAttendanceReportSelector(state)
  const params = parseQueryString(props.location.search)
  const startDate = params.startDate || (report && report.startTime) || params.endDate
  const endDate = params.endDate || moment().format('YYYY-MM-DD')
  const dateRange = startDate ? [moment(startDate), moment(endDate)] : undefined
  return {
    programme: params.programme,
    dateRange
  }
}

const selector = createStructuredSelector({
  initialValues: formInitialValuesSelector
})

export default compose(
  withRouter,
  connect(selector),
  reduxForm({
    form: 'lowAttendanceReportFilterForm',
    enableReinitialize: true
  })
)(ReportFilterForm)
