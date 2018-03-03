import React from 'react'
import AntButton from 'antd/lib/button'
import cx from 'classnames'

import './index.less'

const Button = (props) => {
  const { wide, className, ...otherProps } = props
  return (
    <AntButton {...otherProps} className={cx(className, { 'ant-btn--wide': wide })} />
  )
}

export default Button
