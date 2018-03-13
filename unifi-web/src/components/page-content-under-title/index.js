import React from 'react'
import cx from 'classnames'

import './index.scss'

const PageContentUnderTitle = ({ className, children }) => (
  <div className={cx('page-content-under-title', className)}>{children}</div>
)

export default PageContentUnderTitle
