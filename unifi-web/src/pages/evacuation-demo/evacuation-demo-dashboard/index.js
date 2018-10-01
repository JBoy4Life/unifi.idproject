import React from 'react'
import { AWS_RESOURCES_ROOT } from '../../../config/constants'

const EvacuationDemoDashboard = ({refCallback}) => (
  <video
      ref={refCallback}
      muted
      poster={`${AWS_RESOURCES_ROOT}/evacuation-demo-poster.png`} >
    <source src={`${AWS_RESOURCES_ROOT}/evacuation-demo.mp4`} type="video/mp4" />
  </video>
)

export default EvacuationDemoDashboard
