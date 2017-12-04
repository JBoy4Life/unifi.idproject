import React from 'react'

import { TopNavigation } from '../'

import './index.scss'

const PageContainer = ({ children }) => (
  <div className="page-contianer">
    <TopNavigation />
    {children}
  </div>
)

export default PageContainer
