import PropTypes from 'prop-types'
import React from 'react'
import { Spin, Icon } from 'antd'

import './index.less'

const Spinner = ({ size }) => (
  <Spin indicator={<Icon type="loading" style={{ fontSize: size }} className="spinner" spin />} />
)

Spinner.propTypes = {
  size: PropTypes.number
}

Spinner.defaultProps = {
  size: 24
}

export default Spinner
