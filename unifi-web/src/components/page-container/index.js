import React from 'react'

import { TopNavigation } from '../'

import './index.scss'

const PageContainer = ({ className = '', children }) => (
  <div className={`${className} page-contianer`}>
    <TopNavigation />
    {children}
  </div>
)

export default PageContainer
