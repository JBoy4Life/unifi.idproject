import React, { Component } from 'react'
import pick from 'lodash/pick'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import * as ROUTES from 'utils/routes'
import { Col, Row } from 'elements'
import { detectablesListSelector } from 'redux/modules/detectable/selectors'
import { formSubmit } from 'utils/form'
import { getHolder, updateHolder } from 'redux/modules/holder/actions'
import { getImageData } from '../helpers'
import { holderDetailsSelector } from 'redux/modules/holder/selectors'
import { HolderForm } from 'smart-components'
import { listDetectables } from 'redux/modules/detectable/actions'
import { PageContentTitle } from 'components'
import { withClientId } from 'hocs'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'directory-edit'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

class ContactEdit extends Component {
  static propTypes = {
    clientId: PropTypes.string.isRequired,
    getHolder: PropTypes.func.isRequired,
    listDetectables: PropTypes.func.isRequired,
    match: PropTypes.object.isRequired,
    history: PropTypes.object.isRequired,
    updateHolder: PropTypes.func.isRequired
  };

  componentDidMount() {
    const { clientId, getHolder, listDetectables, match } = this.props
    const { clientReference } = match.params
    getHolder({ clientId, clientReference, with: ['image', 'detectable-type'] })
    listDetectables({ clientId, assignment: clientReference })
  }

  get backUrl() {
    const { match: { params: { clientReference } } } = this.props
    return ROUTES.DIRECTORY_HOLDER_DETAIL.replace(':clientReference', clientReference)
  }

  handleSubmit = (values) => {
    const { clientId, history, updateHolder, match: { params: { clientReference } } } = this.props
    const image = values.image ? getImageData(values.image.data) : null
    const changes = {
      name: values.name,
      active: !values.inactive
    }
    if (typeof image !== 'undefined') {
      changes.image = image
    }
    return formSubmit(updateHolder, { clientId, clientReference, changes })
      .then(() => history.push(this.backUrl))
  };

  render() {
    const { holder, detectablesList } = this.props
    const initialValues = holder ? {
      ...holder,
      inactive: !holder.active
    } : null

    return holder ? (
      <Row>
        <Col xs={24} md={10}>
          <p className={bemE('back')}><Link to={this.backUrl}>&laquo; Back</Link></p>
          <PageContentTitle>Edit Contact</PageContentTitle>
          <h2 className={bemE('subtitle')}>User Details</h2>
          <HolderForm initialValues={initialValues} onSubmit={this.handleSubmit} />
        </Col>
      </Row>
    ) : null
  }
}

export const selector = createStructuredSelector({
  detectablesList: detectablesListSelector,
  holder: holderDetailsSelector
})

export const actions = {
  getHolder,
  listDetectables,
  updateHolder
}

export default compose(
  withClientId,
  connect(selector, actions),
)(ContactEdit)
