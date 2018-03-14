import React, { Component } from 'react'
import fp from 'lodash/fp'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import * as ROUTES from 'utils/routes'
import { Avatar, Col, Row } from 'elements'
import { detectablesListSelector } from 'redux/modules/detectable/selectors'
import { holdersSelector } from 'redux/modules/holder/selectors'
import { listDetectables } from 'redux/modules/detectable/actions'
import { PageContentTitle } from 'components'
import { withClientId } from 'hocs'
import './index.scss'

const holderSelector = (state, props) => 
  fp.compose(
    fp.find({ clientReference: props.match.params.clientReference }),
    holdersSelector
  )(state)

const COMPONENT_CSS_CLASSNAME = 'directory-details'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

class ContactDetails extends Component {
  componentDidMount() {
    const { clientId, listDetectables, match } = this.props
    listDetectables({ clientId, assignment: match.params.clientReference })
  }

  render() {
    const { holder, detectablesList } = this.props
    return holder ? (
      <Row>
        <Col xs={24} md={8}>
          <p className={bemE('back')}><Link to={ROUTES.DIRECTORY}>&laquo; Back</Link></p>
          <PageContentTitle>{holder.name}</PageContentTitle>
          <p>ID number: {holder.clientReference}</p>
          <Avatar image={holder.image} className={bemE('avatar')} />
          <h3 className={bemE('dt-title')}>Detectables</h3>
          {detectablesList.map(item => (
            <div className={bemE('dt-item')} key={item.detectableId}>
              <div className={bemE('dt-type')}>{item.detectableType}: {item.detectableId}</div>
              <div>{item.description}</div>
            </div>
          ))}
        </Col>
      </Row>
    ) : null
  }
}

export const selector = createStructuredSelector({
  detectablesList: detectablesListSelector,
  holder: holderSelector
})

export const actions = {
  listDetectables
}

export default compose(
  withClientId,
  connect(selector, actions),
)(ContactDetails)
