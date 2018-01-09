import React from 'react'

import { Button } from '../../../elements'

const SiteWizardFloorPlan = props => (
  <div>
    <h2>Floor plan TBD</h2>
    <Button onClick={props.onSubmit} type="primary">Proceed</Button>
  </div>
)

export default SiteWizardFloorPlan
