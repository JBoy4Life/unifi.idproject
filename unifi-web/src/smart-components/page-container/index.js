import React from 'react'

import { LinkedNavigationMenu } from '../'

import './index.scss'

const PageContainer = ({ className = '', children }) => (
  <div className={`${className} page-contianer`}>
    <LinkedNavigationMenu />
    {children}
  </div>
)

export default PageContainer
