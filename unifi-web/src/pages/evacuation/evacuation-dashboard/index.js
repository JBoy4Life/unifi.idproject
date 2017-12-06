import React, { Component } from 'react'
import { Row, Col } from '../../../elements'
import { EvacuationTable } from '../../../smart-components'

export default class EvacuationDashboard extends Component {
  render() {
    return (
      <div>
        <Row>
          <Col span={14}><EvacuationTable /></Col>
          <Col span={10}>Status</Col>
        </Row>
      </div>
    )
  }
}
