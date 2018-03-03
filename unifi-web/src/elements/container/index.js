import React from 'react'
import cx from 'classnames'

import './index.scss'

const Container = ({ className, tag, ...props }) => {
  const Tag = tag || 'div'
  return <Tag className={cx('container', className)} {...props} />
}

export default Container
