import React, { Component } from 'react'
import PropTypes from 'prop-types'

import { Steps } from '../../elements'
import { SiteWizardDetailsForm } from '../'
import SiteWizardIntro from './components/site-wizard-intro'
import SiteWizardFloorPlan from './components/site-wizard-floor-plan'
import SiteWizardUpload from './components/site-wizard-upload'
import SiteWizardReview from './components/site-wizard-review'

import { noop } from '../../utils/helpers'

const { Step } = Steps

export default class SiteCreationWizard extends Component {
  static propTypes = {
    onWizardDone: PropTypes.func,
    onStepChange: PropTypes.func,
  }

  static defaultProps = {
    onWizardDone: noop,
    onStepChange: noop,
  }

  state = {
    currentTab: 0,
  }

  handleNextStep = () => {
    this.setState({ currentTab: this.state.currentTab + 1 }, () => {
      this.props.onStepChange(this.state.currentTab)
    })
  }

  handleDone = () => {
    this.props.onWizardDone()
  }

  renderStepsHeader() {
    return (
      <Steps size="small" current={this.state.currentTab}>
        <Step title="Introduction" />
        <Step title="Details" />
        <Step title="Zones" />
        <Step title="Upload" />
        <Step title="Review" />
      </Steps>
    )
  }

  renderTabContent() {
    const { currentTab } = this.state
    switch (currentTab) {
      case 4:
        return <SiteWizardReview onSubmit={this.handleDone} />
      case 3:
        return <SiteWizardUpload onSubmit={this.handleNextStep} />
      case 2:
        return <SiteWizardFloorPlan onSubmit={this.handleNextStep} />
      case 1:
        return <SiteWizardDetailsForm onSubmit={this.handleNextStep} />
      case 0:
      default:
        return <SiteWizardIntro onSubmit={this.handleNextStep} />
    }
  }

  render() {
    return (
      <div className="site-creation-wizard">
        {this.renderStepsHeader()}
        {this.renderTabContent()}
      </div>
    )
  }
}
