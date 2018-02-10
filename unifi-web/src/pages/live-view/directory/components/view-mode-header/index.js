import React from 'react'
import PropTypes from 'prop-types'
import { Col, Icon, Radio, Row, Select, Slider } from 'elements'

import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'directory-view-header'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`
const Option = Select.Option

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

const handleFilterOption = (input, option) => {
  return option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

const ViewModeHeader = props => (
  <div className={COMPONENT_CSS_CLASSNAME}>
    <Row gutter={16}>
      <Col sm={12} className={bemE('status')}>
        {props.grouping === 'zones' &&
          <Select
            showSearch
            value={props.zoneId}
            style={{ width: '100%' }}
            placeholder="Select a zone"
            optionFilterProp="children"
            onChange={props.onZoneChange}
            filterOption={handleFilterOption}
          >
            {props.zones.map((zone, index) => (
              <Option value={zone.zoneId} key={index}>{zone.name}</Option>
            ))}
          </Select>
        }
      </Col>
      <Col sm={6} className={bemE('num-items')}>
        {props.viewMode === 'grid' &&
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
        <Radio.Group onChange={props.onViewModeChange} value={props.viewMode || 'list'}>
          <Radio.Button value="list"><Icon type="bars" /></Radio.Button>
          <Radio.Button value="grid"><Icon type="appstore-o" /></Radio.Button>
        </Radio.Group>
      </Col>
    </Row>
  </div>
)

ViewModeHeader.propTypes = {
  grouping: PropTypes.string,
  itemsPerRow: PropTypes.number,
  onViewModeChange: PropTypes.func,
  onItemsPerRowChange: PropTypes.func,
  onZoneChange: PropTypes.func,
  resultCount: PropTypes.number,
  zoneId: PropTypes.string,
  viewMode: PropTypes.string,
  zones: PropTypes.array
}

export default ViewModeHeader
