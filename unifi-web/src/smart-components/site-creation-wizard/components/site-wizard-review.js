import React from 'react'

import { Button } from '../../../elements'

const SiteWizardReview = props => (
  <div>
    <h2>Review</h2>
    <Button onClick={props.onSubmit} type="primary">Done</Button>
  </div>
)

export default SiteWizardReview
