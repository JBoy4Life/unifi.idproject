import React from 'react'
import PropTypes from 'prop-types'
import { Col, Icon, Radio, Row, Slider } from 'elements'

import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'directory-view-header'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

const marks = {
  2: '2',
  3: '3',
  4: '4',
  5: '5',
  6: '6',
  7: '7',
  8: '8',
};

const tipFormatter = (value) => `${value} items per row`

const ViewModeHeader = props => (
  <div className={COMPONENT_CSS_CLASSNAME}>
    <Row gutter={16}>
      <Col sm={12} className={bemE('status')}>
        Showing {props.resultCount} results
      </Col>
      <Col sm={6} className={bemE('num-items')}>
        {props.viewValue === 'grid' &&
          <Slider
            min={2} max={8}
            marks={marks}
            tipFormatter={tipFormatter}
            onChange={props.onItemsPerRowChange}
            value={props.itemsPerRow}
          />
        }
      </Col>
      <Col sm={6} className={bemE('mode')}>
        <Radio.Group onChange={props.onChange} value={props.viewValue || 'list'}>
          <Radio.Button value="list"><Icon type="bars" /></Radio.Button>
          <Radio.Button value="grid"><Icon type="appstore-o" /></Radio.Button>
        </Radio.Group>
      </Col>
    </Row>
  </div>
)

ViewModeHeader.propTypes = {
  itemsPerRow: PropTypes.number,
  onChange: PropTypes.func,
  onItemsPerRowChange: PropTypes.func,
  resultCount: PropTypes.number,
  viewValue: PropTypes.string
}

export default ViewModeHeader
