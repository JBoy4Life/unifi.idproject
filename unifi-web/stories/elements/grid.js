/*
  eslint-disable
  jsx-a11y/accessible-emoji,
  import/no-extraneous-dependencies,
  import/first,
  no-unused-vars
*/
import React from 'react'

import { storiesOf } from '@storybook/react'
import { action } from '@storybook/addon-actions'
import { linkTo } from '@storybook/addon-links'

import { Aux, Row, Col } from '../../src/elements'

import './grid.scss'

storiesOf('Elements/Gird Layout', module)
  .add('Basic grid', () => (
    <Aux>
      <Row>
        <Col className="outline-col" span={6}><div className="marked-content">1</div></Col>
        <Col className="outline-col" span={6}><div className="marked-content">2</div></Col>
        <Col className="outline-col" span={6}><div className="marked-content">3</div></Col>
        <Col className="outline-col" span={6}><div className="marked-content">4</div></Col>
      </Row>
    </Aux>
  ))
  .add('Gutter', () => (
    <Aux>
      <Row gutter={16}>
        <Col className="outline-col" span={6}><div className="marked-content">1</div></Col>
        <Col className="outline-col" span={6}><div className="marked-content">2</div></Col>
        <Col className="outline-col" span={6}><div className="marked-content">3</div></Col>
        <Col className="outline-col" span={6}><div className="marked-content">4</div></Col>
      </Row>
      <br />
      <Row gutter={16}>
        <Col className="outline-col" span={3}><div className="marked-content">1</div></Col>
        <Col className="outline-col" span={3}><div className="marked-content">2</div></Col>
        <Col className="outline-col" span={6}><div className="marked-content">3</div></Col>
        <Col className="outline-col" span={6}><div className="marked-content">4</div></Col>
        <Col className="outline-col" span={6}><div className="marked-content">5</div></Col>
      </Row>
      <br />
      <Row gutter={16}>
        <Col className="outline-col" span={12}><div className="marked-content">1</div></Col>
        <Col className="outline-col" span={4}><div className="marked-content">2</div></Col>
        <Col className="outline-col" span={2}><div className="marked-content">3</div></Col>
        <Col className="outline-col" span={6}><div className="marked-content">4</div></Col>
      </Row>
    </Aux>
  ))
