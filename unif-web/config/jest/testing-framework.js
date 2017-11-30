import Enzyme from 'enzyme'
import Adapter from 'enzyme-adapter-react-16'
import 'enzyme-to-json'

Enzyme.configure({ adapter: new Adapter() })

global.Enzyme = Enzyme