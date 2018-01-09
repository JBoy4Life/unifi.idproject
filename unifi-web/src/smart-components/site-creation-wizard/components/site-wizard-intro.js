import React from 'react'

import { Button } from '../../../elements'

const SiteWizardIntro = props => (
  <div>
    <h2>Introduction</h2>
    <Button onClick={props.onSubmit} type="primary">Proceed</Button>
  </div>
)

export default SiteWizardIntro
