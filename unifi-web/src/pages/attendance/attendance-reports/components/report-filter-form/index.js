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
    location: PropTypes.object,
    history: PropTypes.object
  }

  doSubmit = (values) => {
    const { history, location, programme } = this.props
    history.push({
      pathname: location.pathname,
      search: jsonToQueryString({
        programme,
        ...getDateRangeParams(values.dateRange)
      })
    })
  }

  render() {
    const { handleSubmit, location } = this.props

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
          <Button type="primary" htmlType="submit">Go</Button>
        </FormItem>
      </Form>
    )
  }
}

const formInitialValuesSelector = (state, props) => {
  const params = parseQueryString(props.location.search)
  const startDate = params.startDate || params.endDate
  const endDate = params.endDate || params.startDate
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
    form: 'lowAttendanceReportFilterForm'
  })
)(ReportFilterForm)
