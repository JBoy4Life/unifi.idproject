import React from 'react'
import cx from 'classnames'
import PropTypes from 'prop-types'
import { Col, Row, Select } from 'elements'

import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'zone-filter'
const Option = Select.Option

const handleFilterOption = (input, option) => {
  return option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

const ZoneFilter = props => (
  <div className={COMPONENT_CSS_CLASSNAME}>
    <Row gutter={16}>
      <Col sm={12}>
        <Select
          showSearch
          disabled={props.disabled}
          value={props.zoneId}
          style={{ width: '100%' }}
          placeholder={props.placeholder}
          optionFilterProp="children"
          onChange={props.onZoneChange}
          filterOption={handleFilterOption}
        >
          {props.zones.map((zone, index) => (
            <Option value={zone[props.idKey]} key={index}>{zone[props.nameKey]}</Option>
          ))}
        </Select>
      </Col>
    </Row>
  </div>
)

ZoneFilter.propTypes = {
  onZoneChange: PropTypes.func,
  zoneId: PropTypes.string,
  zones: PropTypes.array
}

export default ZoneFilter
