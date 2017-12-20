import React from 'react'
import { Radio, Icon } from '../../../../../elements'

import './index.scss'

const ViewModeHeader = props => (
  <div className="directory-view-header">
    <div className="directory-view-status">showing 256 results</div>
    <Radio.Group onChange={this.handleModeChange} value={props.mode || 'list'}>
      <Radio.Button value="list"><Icon type="bars" /></Radio.Button>
      <Radio.Button value="grid"><Icon type="appstore-o" /></Radio.Button>
    </Radio.Group>
  </div>
)

export default ViewModeHeader
