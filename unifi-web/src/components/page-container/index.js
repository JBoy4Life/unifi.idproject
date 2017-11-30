import React from 'react'

import { TopNavigation } from '../'

const PageContainer = ({ children }) => (
  <div className="page-contianer">
    <TopNavigation />
    {children}
  </div>
)

export default PageContainer
